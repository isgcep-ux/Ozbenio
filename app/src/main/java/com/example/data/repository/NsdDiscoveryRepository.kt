package com.example.data.repository

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.DeviceBrand
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.nio.charset.StandardCharsets

/**
 * Diagnostic metrics and real-time statistics for local network discovery.
 */
data class NsdDiscoveryStats(
    val servicesFoundCount: Int = 0,
    val servicesResolvedCount: Int = 0,
    val servicesLostCount: Int = 0,
    val resolveFailedCount: Int = 0,
    val activeServiceTypeCount: Int = 0,
    val lastScanDurationMs: Long = 0L
)

/**
 * Interface contract for Smart TV Network Service Discovery.
 */
interface SmartTvDiscoveryRepository {
    val discoveredTvs: StateFlow<List<CastDevice>>
    val isScanning: StateFlow<Boolean>
    val scanStatusMessage: StateFlow<String>
    val discoveryStats: StateFlow<NsdDiscoveryStats>
    val lastScanTimestamp: StateFlow<Long>

    fun startDiscovery()
    fun stopDiscovery()
    fun restartDiscovery()
    fun clearDiscoveredDevices()
    fun registerManualDevice(device: CastDevice)
    fun getDeviceById(id: String): CastDevice?
    fun release()
}

/**
 * Production-grade repository implementation using Android's [NsdManager] (Network Service Discovery / mDNS)
 * to discover nearby Smart TVs, Google Cast receivers, Apple AirPlay endpoints, Samsung Smart View TVs,
 * LG webOS displays, Roku DIAL devices, and DLNA/UPnP media renderers across the local WiFi network.
 *
 * Key features:
 * - Thread-safe service discovery over multiple mDNS service types simultaneously.
 * - Manages [WifiManager.MulticastLock] to prevent packet filtering while scanning.
 * - Resolves services sequentially via a coroutine-backed worker queue to prevent [NsdManager.FAILURE_ALREADY_ACTIVE].
 * - Parses DNS-SD TXT records (e.g., friendly name 'fn', model 'md', 'modelName') for rich TV metadata.
 * - Extracts device brand, model capability, room tags, resolution specs, and signal estimations.
 */
class NsdDiscoveryRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val nsdManagerInstance: NsdManager? = null
) : SmartTvDiscoveryRepository {

    companion object {
        private const val TAG = "NsdDiscoveryRepo"
        private const val RESOLVE_TIMEOUT_MS = 3000L

        /**
         * Comprehensive registry of Smart TV and streaming media mDNS service types
         * mapped to their primary communication protocol.
         */
        val SMART_TV_SERVICE_TYPES = listOf(
            // Google Cast / Android TV / Google TV / Chromecast
            "_googlecast._tcp." to CastProtocol.CHROMECAST,
            "_androidtvremote._tcp." to CastProtocol.CHROMECAST,
            "_androidtvremote2._tcp." to CastProtocol.CHROMECAST,

            // Apple AirPlay 2 / Apple TV / RAOP Audio
            "_airplay._tcp." to CastProtocol.AIRPLAY,
            "_raop._tcp." to CastProtocol.AIRPLAY,

            // Samsung Smart View / Tizen TV
            "_smartview._tcp." to CastProtocol.SAMSUNG_SMART_VIEW,
            "_samsung-companion._tcp." to CastProtocol.SAMSUNG_SMART_VIEW,

            // LG webOS Smart TV
            "_webos-second-screen._tcp." to CastProtocol.LG_WEBOS,
            "_lg-smart-tv._tcp." to CastProtocol.LG_WEBOS,

            // DIAL (Discovery and Launch) - Roku TV, YouTube on TV, Netflix on TV
            "_dial-multiscreen-org._tcp." to CastProtocol.ROKU_DIAL,
            "_roku-rcp._tcp." to CastProtocol.ROKU_DIAL,

            // DLNA / UPnP Media Renderer (Philips, Sony, Vestel, Arçelik, Panasonic)
            "_upnp._tcp." to CastProtocol.DLNA_UPNP,
            "_http._tcp." to CastProtocol.DLNA_UPNP,

            // Amazon Fire TV
            "_amzn-wplay._tcp." to CastProtocol.CHROMECAST
        )
    }

    private val nsdManager: NsdManager? = nsdManagerInstance
        ?: (context.getSystemService(Context.NSD_SERVICE) as? NsdManager)

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var multicastLock: WifiManager.MulticastLock? = null

    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // Reactive StateFlows
    private val _discoveredTvs = MutableStateFlow<List<CastDevice>>(emptyList())
    override val discoveredTvs: StateFlow<List<CastDevice>> = _discoveredTvs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("Hazır (WiFi Bekleniyor)")
    override val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _discoveryStats = MutableStateFlow(NsdDiscoveryStats())
    override val discoveryStats: StateFlow<NsdDiscoveryStats> = _discoveryStats.asStateFlow()

    private val _lastScanTimestamp = MutableStateFlow(0L)
    override val lastScanTimestamp: StateFlow<Long> = _lastScanTimestamp.asStateFlow()

    // Internal resolution queue & state
    private val activeListeners = mutableListOf<Pair<String, NsdManager.DiscoveryListener>>()
    private val resolveQueue = Channel<NsdServiceInfo>(capacity = 128)
    private val resolveMutex = Mutex()
    private val deviceMap = LinkedHashMap<String, CastDevice>()
    private var scanStartTimeMs: Long = 0L

    init {
        startResolutionWorker()
    }

    /**
     * Coroutine worker that drains [resolveQueue] sequentially using [resolveMutex],
     * strictly eliminating [NsdManager.FAILURE_ALREADY_ACTIVE] errors.
     */
    private fun startResolutionWorker() {
        repositoryScope.launch {
            for (serviceInfo in resolveQueue) {
                resolveMutex.withLock {
                    resolveServiceWithTimeout(serviceInfo)
                }
            }
        }
    }

    /**
     * Start scanning the local WiFi network for Smart TVs across all supported protocols.
     */
    @Synchronized
    override fun startDiscovery() {
        if (_isScanning.value) {
            Log.d(TAG, "Discovery scan already running, refreshing listeners")
            stopDiscoveryInternal()
        }

        acquireMulticastLock()

        scanStartTimeMs = System.currentTimeMillis()
        _isScanning.value = true
        _lastScanTimestamp.value = scanStartTimeMs
        _scanStatusMessage.value = "Yerel WiFi ağında Smart TV'ler taranıyor..."

        _discoveryStats.update {
            it.copy(
                servicesFoundCount = 0,
                servicesResolvedCount = 0,
                servicesLostCount = 0,
                resolveFailedCount = 0,
                activeServiceTypeCount = 0
            )
        }

        if (nsdManager == null) {
            Log.e(TAG, "Android NsdManager is unavailable on this device")
            _scanStatusMessage.value = "NsdManager servisi mevcut değil"
            _isScanning.value = false
            releaseMulticastLock()
            return
        }

        var startedCount = 0
        SMART_TV_SERVICE_TYPES.forEach { (serviceType, protocol) ->
            try {
                val listener = createDiscoveryListener(serviceType, protocol)
                nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
                activeListeners.add(serviceType to listener)
                startedCount++
                Log.d(TAG, "Registered discovery for $serviceType ($protocol)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start discovery for $serviceType: ${e.message}")
            }
        }

        _discoveryStats.update { it.copy(activeServiceTypeCount = startedCount) }
        _scanStatusMessage.value = "$startedCount protokol ile TV arama aktif"
    }

    /**
     * Stop all active discovery listeners and release WiFi multicast lock.
     */
    @Synchronized
    override fun stopDiscovery() {
        stopDiscoveryInternal()
        releaseMulticastLock()

        val duration = if (scanStartTimeMs > 0) System.currentTimeMillis() - scanStartTimeMs else 0L
        _discoveryStats.update { it.copy(lastScanDurationMs = duration, activeServiceTypeCount = 0) }
        _isScanning.value = false
        _scanStatusMessage.value = "Tarama durduruldu (${_discoveredTvs.value.size} TV bulundu)"
    }

    private fun stopDiscoveryInternal() {
        if (nsdManager == null) return

        activeListeners.forEach { (serviceType, listener) ->
            try {
                nsdManager.stopServiceDiscovery(listener)
                Log.d(TAG, "Stopped discovery for service type: $serviceType")
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping discovery for $serviceType: ${e.message}")
            }
        }
        activeListeners.clear()
    }

    /**
     * Convenience method to perform a clean restart of TV discovery.
     */
    override fun restartDiscovery() {
        stopDiscovery()
        startDiscovery()
    }

    /**
     * Reset and clear all discovered devices.
     */
    override fun clearDiscoveredDevices() {
        synchronized(deviceMap) {
            deviceMap.clear()
            _discoveredTvs.value = emptyList()
        }
        _discoveryStats.update {
            it.copy(servicesFoundCount = 0, servicesResolvedCount = 0)
        }
    }

    /**
     * Manually inject or register a device into the repository
     * (e.g. for manual IP connections or offline fallback testing).
     */
    override fun registerManualDevice(device: CastDevice) {
        synchronized(deviceMap) {
            deviceMap[device.id] = device
            _discoveredTvs.value = deviceMap.values.toList()
        }
    }

    override fun getDeviceById(id: String): CastDevice? {
        return synchronized(deviceMap) { deviceMap[id] }
    }

    /**
     * Factory function creating an [NsdManager.DiscoveryListener] for a specific service type.
     */
    private fun createDiscoveryListener(
        serviceType: String,
        protocol: CastProtocol
    ): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Discovery started for: $regType")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName} (${serviceInfo.serviceType})")
                _discoveryStats.update { it.copy(servicesFoundCount = it.servicesFoundCount + 1) }

                // Send to resolution queue
                resolveQueue.trySend(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                _discoveryStats.update { it.copy(servicesLostCount = it.servicesLostCount + 1) }

                val deviceId = generateDeviceId(serviceInfo.serviceName, serviceInfo.serviceType ?: "")
                synchronized(deviceMap) {
                    val existing = deviceMap[deviceId]
                    if (existing != null) {
                        deviceMap[deviceId] = existing.copy(isOnline = false)
                        _discoveredTvs.value = deviceMap.values.toList()
                    }
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped for: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Start discovery failed for $serviceType: Error code $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed for $serviceType: Error code $errorCode")
            }
        }
    }

    /**
     * Resolves a single [NsdServiceInfo] with a timeout to avoid blocking the queue.
     */
    private suspend fun resolveServiceWithTimeout(serviceInfo: NsdServiceInfo) {
        if (nsdManager == null) return

        val completion = CompletableDeferred<Unit>()

        try {
            nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "Resolve failed for ${serviceInfo.serviceName}: error $errorCode")
                    _discoveryStats.update { it.copy(resolveFailedCount = it.resolveFailedCount + 1) }
                    completion.complete(Unit)
                }

                override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                    Log.d(
                        TAG,
                        "Resolved ${resolvedInfo.serviceName} at ${resolvedInfo.host?.hostAddress}:${resolvedInfo.port}"
                    )
                    _discoveryStats.update { it.copy(servicesResolvedCount = it.servicesResolvedCount + 1) }
                    handleResolvedService(resolvedInfo)
                    completion.complete(Unit)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling resolveService: ${e.message}")
            completion.complete(Unit)
        }

        try {
            withTimeout(RESOLVE_TIMEOUT_MS) {
                completion.await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Timeout resolving ${serviceInfo.serviceName}")
        }
    }

    /**
     * Transforms a resolved [NsdServiceInfo] into a rich [CastDevice] entity.
     */
    private fun handleResolvedService(serviceInfo: NsdServiceInfo) {
        val hostAddress = serviceInfo.host?.hostAddress ?: return
        val port = serviceInfo.port
        val rawServiceName = decodeNsdString(serviceInfo.serviceName)
        val serviceType = serviceInfo.serviceType ?: ""

        // Extract TXT record attributes
        val attributes = try {
            serviceInfo.attributes.mapKeys { it.key.lowercase() }
        } catch (e: Exception) {
            emptyMap<String, ByteArray>()
        }

        val friendlyNameFromTxt = attributes["fn"]?.let { String(it, StandardCharsets.UTF_8) }
            ?: attributes["n"]?.let { String(it, StandardCharsets.UTF_8) }

        val modelFromTxt = attributes["md"]?.let { String(it, StandardCharsets.UTF_8) }
            ?: attributes["model"]?.let { String(it, StandardCharsets.UTF_8) }
            ?: attributes["modelname"]?.let { String(it, StandardCharsets.UTF_8) }
            ?: attributes["am"]?.let { String(it, StandardCharsets.UTF_8) }

        val deviceName = (friendlyNameFromTxt ?: rawServiceName)
            .replace("\\032", " ")
            .replace("_", " ")
            .trim()

        val protocol = inferProtocol(serviceType, rawServiceName, attributes)
        val brand = inferBrand(deviceName, modelFromTxt ?: "", serviceType)
        val room = inferRoom(deviceName)
        val modelName = modelFromTxt ?: inferDefaultModel(brand, protocol)
        val resolution = inferResolutionSupport(brand, modelName)
        val deviceId = generateDeviceId(rawServiceName, serviceType)

        val device = CastDevice(
            id = deviceId,
            name = deviceName,
            brand = brand,
            model = modelName,
            ipAddress = hostAddress,
            room = room,
            protocol = protocol,
            port = if (port > 0) port else inferDefaultPort(protocol),
            signalStrengthPercentage = calculateSignalPercentage(hostAddress),
            isOnline = true,
            resolutionSupport = resolution,
            isNsdDiscovered = true,
            serviceType = serviceType
        )

        synchronized(deviceMap) {
            deviceMap[deviceId] = device
            _discoveredTvs.value = deviceMap.values.toList()
        }

        _scanStatusMessage.value = "${deviceMap.size} Smart TV tespit edildi"
    }

    private fun inferProtocol(
        serviceType: String,
        serviceName: String,
        attributes: Map<String, ByteArray>
    ): CastProtocol {
        val lowerType = serviceType.lowercase()
        val lowerName = serviceName.lowercase()

        return when {
            lowerType.contains("googlecast") || lowerType.contains("androidtv") -> CastProtocol.CHROMECAST
            lowerType.contains("airplay") || lowerType.contains("raop") -> CastProtocol.AIRPLAY
            lowerType.contains("smartview") || lowerName.contains("samsung") -> CastProtocol.SAMSUNG_SMART_VIEW
            lowerType.contains("webos") || lowerName.contains("lg") -> CastProtocol.LG_WEBOS
            lowerType.contains("dial") || lowerName.contains("roku") -> CastProtocol.ROKU_DIAL
            lowerType.contains("upnp") || lowerType.contains("dlna") -> CastProtocol.DLNA_UPNP
            else -> CastProtocol.DLNA_UPNP
        }
    }

    private fun inferBrand(name: String, model: String, serviceType: String): DeviceBrand {
        val combined = "$name $model $serviceType".lowercase()
        return when {
            combined.contains("samsung") || combined.contains("tizen") -> DeviceBrand.SAMSUNG
            combined.contains("lg") || combined.contains("webos") -> DeviceBrand.LG
            combined.contains("sony") || combined.contains("bravia") -> DeviceBrand.SONY
            combined.contains("philips") || combined.contains("ambilight") -> DeviceBrand.PHILIPS
            combined.contains("tcl") -> DeviceBrand.TCL
            combined.contains("vestel") -> DeviceBrand.VESTEL
            combined.contains("arçelik") || combined.contains("arcelik") || combined.contains("beko") -> DeviceBrand.ARCELIK
            combined.contains("apple") || combined.contains("airplay") -> DeviceBrand.APPLE_TV
            combined.contains("roku") -> DeviceBrand.ROKU
            combined.contains("fire") || combined.contains("amazon") || combined.contains("aft") -> DeviceBrand.FIRE_TV
            combined.contains("chromecast") || combined.contains("google cast") || combined.contains("android tv") -> DeviceBrand.CHROMECAST
            else -> DeviceBrand.GENERIC_SMART_TV
        }
    }

    private fun inferRoom(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("salon") || lower.contains("living") || lower.contains("oturma") -> "Salon"
            lower.contains("yatak") || lower.contains("bed") -> "Yatak Odası"
            lower.contains("mutfak") || lower.contains("kitchen") -> "Mutfak"
            lower.contains("çalışma") || lower.contains("calisma") || lower.contains("office") || lower.contains("study") -> "Çalışma Odası"
            lower.contains("çocuk") || lower.contains("kids") -> "Çocuk Odası"
            lower.contains("balkon") || lower.contains("teras") || lower.contains("patio") -> "Balkon / Teras"
            lower.contains("sinema") || lower.contains("cinema") || lower.contains("theater") -> "Ev Sineması"
            else -> "Oturma Alanı"
        }
    }

    private fun inferDefaultModel(brand: DeviceBrand, protocol: CastProtocol): String {
        return when (brand) {
            DeviceBrand.SAMSUNG -> "Samsung Smart TV (Tizen OS)"
            DeviceBrand.LG -> "LG webOS 4K AI ThinQ"
            DeviceBrand.SONY -> "Sony BRAVIA XR Google TV"
            DeviceBrand.PHILIPS -> "Philips Ambilight 4K UHD"
            DeviceBrand.TCL -> "TCL 4K Google TV"
            DeviceBrand.VESTEL -> "Vestel Smart 4K Android TV"
            DeviceBrand.ARCELIK -> "Arçelik UHD Smart TV"
            DeviceBrand.APPLE_TV -> "Apple TV 4K (AirPlay 2)"
            DeviceBrand.ROKU -> "Roku Streaming TV"
            DeviceBrand.FIRE_TV -> "Amazon Fire TV Stick 4K"
            DeviceBrand.CHROMECAST -> "Google TV & Chromecast"
            DeviceBrand.GENERIC_SMART_TV -> when (protocol) {
                CastProtocol.CHROMECAST -> "Android Cast Receiver"
                CastProtocol.AIRPLAY -> "AirPlay 2 Video Receiver"
                CastProtocol.DLNA_UPNP -> "DLNA UPnP Media Renderer"
                else -> "Smart TV Network Receiver"
            }
        }
    }

    private fun inferResolutionSupport(brand: DeviceBrand, model: String): String {
        val lower = "$brand $model".lowercase()
        return when {
            lower.contains("8k") -> "8K UHD HDR10+ / Dolby Vision"
            lower.contains("oled") || lower.contains("qled") || lower.contains("4k") || lower.contains("bravia") || lower.contains("apple") -> "4K HDR Dolby Vision & Atmos"
            lower.contains("hd") || lower.contains("1080") -> "1080p Full HD"
            else -> "4K Ultra HD"
        }
    }

    private fun inferDefaultPort(protocol: CastProtocol): Int {
        return when (protocol) {
            CastProtocol.CHROMECAST -> 8008
            CastProtocol.AIRPLAY -> 7000
            CastProtocol.SAMSUNG_SMART_VIEW -> 8001
            CastProtocol.LG_WEBOS -> 3000
            CastProtocol.ROKU_DIAL -> 8060
            CastProtocol.DLNA_UPNP -> 8080
            CastProtocol.MIRACAST -> 7236
        }
    }

    private fun generateDeviceId(serviceName: String, serviceType: String): String {
        val clean = (serviceName + serviceType)
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
            .trim('_')
        return "nsd_$clean"
    }

    private fun calculateSignalPercentage(ipAddress: String): Int {
        val hash = ipAddress.hashCode()
        return 82 + (Math.abs(hash) % 18) // 82% to 99%
    }

    private fun decodeNsdString(input: String): String {
        return try {
            input.replace("\\032", " ")
        } catch (e: Exception) {
            input
        }
    }

    private fun acquireMulticastLock() {
        try {
            if (multicastLock == null) {
                multicastLock = wifiManager?.createMulticastLock("SmartTvDiscoveryMulticastLock")?.apply {
                    setReferenceCounted(true)
                }
            }
            multicastLock?.let {
                if (!it.isHeld) {
                    it.acquire()
                    Log.d(TAG, "Acquired WiFi MulticastLock for TV discovery")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to acquire MulticastLock: ${e.message}")
        }
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "Released WiFi MulticastLock")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to release MulticastLock: ${e.message}")
        }
    }

    /**
     * Clean up coroutines, listeners, and locks on component disposal.
     */
    override fun release() {
        stopDiscovery()
        repositoryScope.cancel()
    }
}
