package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CastDatabase
import com.example.data.local.CastSessionEntity
import com.example.data.local.CustomStreamEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.RecentDeviceEntity
import com.example.data.model.AppScreen
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.CastState
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceBrand
import com.example.data.model.MediaItem
import com.example.data.model.Playlist
import com.example.data.model.RecentTvDevice
import com.example.data.model.StreamQuality
import com.example.data.network.NsdCastScanner
import com.example.data.repository.CastRepository
import com.example.data.repository.NsdDiscoveryRepository
import com.example.data.repository.NsdDiscoveryStats
import com.example.data.repository.SmartTvDiscoveryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CastViewModel(application: Application) : AndroidViewModel(application) {

    val nsdDiscoveryRepository: SmartTvDiscoveryRepository = NsdDiscoveryRepository(application)
    private val repository: CastRepository
    private val nsdScanner: NsdCastScanner = NsdCastScanner(application)

    private val _castState = MutableStateFlow(CastState())
    val castState: StateFlow<CastState> = _castState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<CastDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<CastDevice>> = _discoveredDevices.asStateFlow()

    val isScanningNsd: StateFlow<Boolean> = nsdScanner.isScanning
    val nsdFoundCount: StateFlow<Int> = nsdScanner.activeServicesFoundCount
    val nsdDiscoveryStats: StateFlow<NsdDiscoveryStats> = nsdDiscoveryRepository.discoveryStats
    val nsdScanStatusMessage: StateFlow<String> = nsdDiscoveryRepository.scanStatusMessage

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Screen Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Dialog & UI Visibility states
    private val _showCastDialog = MutableStateFlow(false)
    val showCastDialog: StateFlow<Boolean> = _showCastDialog.asStateFlow()

    private val _showRemoteSheet = MutableStateFlow(false)
    val showRemoteSheet: StateFlow<Boolean> = _showRemoteSheet.asStateFlow()

    private val _showAddUrlDialog = MutableStateFlow(false)
    val showAddUrlDialog: StateFlow<Boolean> = _showAddUrlDialog.asStateFlow()

    private val _isTvPreviewFullscreen = MutableStateFlow(false)
    val isTvPreviewFullscreen: StateFlow<Boolean> = _isTvPreviewFullscreen.asStateFlow()

    // Playlist Dialogs & State
    private val _showPlaylistsSheet = MutableStateFlow(false)
    val showPlaylistsSheet: StateFlow<Boolean> = _showPlaylistsSheet.asStateFlow()

    private val _selectedPlaylistForDetail = MutableStateFlow<Playlist?>(null)
    val selectedPlaylistForDetail: StateFlow<Playlist?> = _selectedPlaylistForDetail.asStateFlow()

    private val _mediaForAddToPlaylist = MutableStateFlow<MediaItem?>(null)
    val mediaForAddToPlaylist: StateFlow<MediaItem?> = _mediaForAddToPlaylist.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    // Active playlist queue for sequential playback
    private var activePlaylistQueue: List<PlaylistItemEntity> = emptyList()
    private var currentPlaylistQueueIndex: Int = -1

    private var playbackJob: Job? = null
    private var scanJob: Job? = null

    val historySessions: StateFlow<List<CastSessionEntity>>
    val customStreams: StateFlow<List<CustomStreamEntity>>
    val allMediaItems: StateFlow<List<MediaItem>>
    val recentDevices: StateFlow<List<RecentTvDevice>>

    val rawPlaylists: StateFlow<List<PlaylistEntity>>
    val allPlaylistItems: StateFlow<List<PlaylistItemEntity>>
    val detailedPlaylists: StateFlow<List<Playlist>>

    init {
        val db = CastDatabase.getDatabase(application)
        repository = CastRepository(db.castDao(), nsdDiscoveryRepository)

        // Seed default starter playlists & recent devices if empty
        viewModelScope.launch {
            repository.seedDefaultPlaylistsIfEmpty()
            repository.seedDefaultRecentDevicesIfEmpty()
        }

        historySessions = repository.getAllSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        customStreams = repository.getCustomStreams()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        recentDevices = repository.getRecentDevices().map { entities ->
            entities.map { entity ->
                val brand = try {
                    DeviceBrand.valueOf(entity.brandName)
                } catch (e: Exception) {
                    DeviceBrand.GENERIC_SMART_TV
                }
                val protocol = try {
                    CastProtocol.valueOf(entity.protocolName)
                } catch (e: Exception) {
                    CastProtocol.CHROMECAST
                }
                RecentTvDevice(
                    device = CastDevice(
                        id = entity.id,
                        name = entity.name,
                        brand = brand,
                        model = entity.model,
                        ipAddress = entity.ipAddress,
                        room = entity.room,
                        protocol = protocol,
                        signalStrengthPercentage = entity.signalStrengthPercentage,
                        resolutionSupport = entity.resolutionSupport,
                        port = entity.port,
                        serviceType = entity.serviceType
                    ),
                    lastConnectedTimestamp = entity.lastConnectedTimestamp
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        rawPlaylists = repository.getAllPlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allPlaylistItems = repository.getAllPlaylistItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Combine Playlists with their respective items
        detailedPlaylists = combine(rawPlaylists, allPlaylistItems) { playlists, items ->
            playlists.map { pl ->
                val plItems = items.filter { it.playlistId == pl.id }
                    .sortedBy { it.orderIndex }
                val totalSec = plItems.sumOf { it.mediaDurationSeconds }
                Playlist(
                    id = pl.id,
                    name = pl.name,
                    description = pl.description,
                    colorHex = pl.colorHex,
                    iconName = pl.iconName,
                    itemCount = plItems.size,
                    totalDurationSeconds = totalSec,
                    items = plItems,
                    createdAt = pl.createdAt
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Keep selected playlist in sync when items update
        viewModelScope.launch {
            detailedPlaylists.collect { list ->
                val current = _selectedPlaylistForDetail.value
                if (current != null) {
                    val updated = list.find { it.id == current.id }
                    _selectedPlaylistForDetail.value = updated
                }
            }
        }

        // Combine NSD discovered devices with preset local network devices
        viewModelScope.launch {
            combine(
                nsdScanner.discoveredDevices,
                nsdDiscoveryRepository.discoveredTvs
            ) { listA, listB ->
                val baseList = repository.sampleDevices
                val combinedMap = LinkedHashMap<String, CastDevice>()

                // NSD dynamically discovered devices take highest priority
                listB.forEach { dev -> combinedMap[dev.id] = dev }
                listA.forEach { dev -> combinedMap[dev.id] = dev }

                // Add base list if not already present by IP
                val existingIps = combinedMap.values.map { it.ipAddress }.toSet()
                baseList.forEach { dev ->
                    if (dev.ipAddress !in existingIps && dev.id !in combinedMap) {
                        combinedMap[dev.id] = dev
                    }
                }

                combinedMap.values.toList()
            }.collect { mergedList ->
                _discoveredDevices.value = mergedList
            }
        }

        // Combine sample media with custom streams from Room
        allMediaItems = combine(
            _selectedCategory,
            _searchQuery,
            customStreams
        ) { category, query, customList ->
            val customMedia = customList.map { entity ->
                MediaItem(
                    id = entity.id,
                    title = entity.title,
                    description = "Özel eklenen akış kaynağı (${entity.category})",
                    category = entity.category,
                    durationSeconds = 600,
                    resolution = entity.resolution,
                    videoUrl = entity.url,
                    thumbnailUrl = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=800&auto=format&fit=crop&q=80",
                    isCustom = true
                )
            }
            val fullList = repository.sampleMediaLibrary + customMedia
            fullList.filter { item ->
                val matchesCategory = (category == "Tümü") || (item.category.contains(category, ignoreCase = true))
                val matchesQuery = query.isBlank() || item.title.contains(query, ignoreCase = true) || item.category.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.sampleMediaLibrary)

        // Set default media for rich tablet showcase
        val defaultMedia = repository.sampleMediaLibrary.first()
        _castState.update {
            it.copy(
                currentMedia = defaultMedia,
                durationSec = defaultMedia.durationSeconds,
                currentPositionSec = 42
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.DEVICE_DISCOVERY || screen == AppScreen.CAST) {
            startDeviceScan()
        }
    }

    fun navigateToCast() {
        navigateTo(AppScreen.CAST)
    }

    fun navigateToDeviceDiscovery() {
        navigateTo(AppScreen.DEVICE_DISCOVERY)
    }

    fun navigateBack() {
        _currentScreen.value = AppScreen.HOME
    }

    fun openCastDialog() {
        _showCastDialog.value = true
        startDeviceScan()
    }

    fun closeCastDialog() {
        _showCastDialog.value = false
        stopDeviceScan()
    }

    fun openRemoteSheet() {
        _showRemoteSheet.value = true
    }

    fun closeRemoteSheet() {
        _showRemoteSheet.value = false
    }

    fun openAddUrlDialog() {
        _showAddUrlDialog.value = true
    }

    fun closeAddUrlDialog() {
        _showAddUrlDialog.value = false
    }

    // --- Playlist Dialog Controls ---
    fun openPlaylistsSheet() {
        _showPlaylistsSheet.value = true
    }

    fun closePlaylistsSheet() {
        _showPlaylistsSheet.value = false
        _selectedPlaylistForDetail.value = null
    }

    fun selectPlaylistForDetail(playlist: Playlist?) {
        _selectedPlaylistForDetail.value = playlist
    }

    fun openAddToPlaylistDialog(media: MediaItem) {
        _mediaForAddToPlaylist.value = media
    }

    fun closeAddToPlaylistDialog() {
        _mediaForAddToPlaylist.value = null
    }

    fun openCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = true
    }

    fun closeCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = false
    }

    fun createPlaylist(name: String, description: String, colorHex: String, iconName: String) {
        if (name.isBlank()) {
            emitToast("Lütfen bir çalma listesi adı girin!")
            return
        }
        viewModelScope.launch {
            val id = repository.createPlaylist(name, description, colorHex, iconName)
            _showCreatePlaylistDialog.value = false
            emitToast("📁 \"$name\" çalma listesi oluşturuldu!")

            // If user was trying to add a video, add it automatically
            val pendingMedia = _mediaForAddToPlaylist.value
            if (pendingMedia != null) {
                repository.addMediaToPlaylist(id, pendingMedia)
                _mediaForAddToPlaylist.value = null
                emitToast("✅ \"${pendingMedia.title}\" listeye eklendi!")
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylistForDetail.value?.id == playlistId) {
                _selectedPlaylistForDetail.value = null
            }
            emitToast("🗑️ Çalma listesi silindi")
        }
    }

    fun addMediaToPlaylist(playlistId: String, media: MediaItem) {
        viewModelScope.launch {
            repository.addMediaToPlaylist(playlistId, media)
            _mediaForAddToPlaylist.value = null
            emitToast("✅ \"${media.title}\" çalma listesine eklendi!")
        }
    }

    fun removePlaylistItem(itemId: Long) {
        viewModelScope.launch {
            repository.removePlaylistItemById(itemId)
            emitToast("🗑️ Video listeden çıkarıldı")
        }
    }

    fun movePlaylistItemUp(playlistId: String, item: PlaylistItemEntity) {
        viewModelScope.launch {
            repository.movePlaylistItemUp(playlistId, item)
            emitToast("⬆️ Video sırası yukarı taşındı")
        }
    }

    fun movePlaylistItemDown(playlistId: String, item: PlaylistItemEntity) {
        viewModelScope.launch {
            repository.movePlaylistItemDown(playlistId, item)
            emitToast("⬇️ Video sırası aşağı taşındı")
        }
    }

    fun playPlaylist(playlist: Playlist, startIndex: Int = 0) {
        if (playlist.items.isEmpty()) {
            emitToast("Bu çalma listesinde henüz video yok!")
            return
        }

        activePlaylistQueue = playlist.items
        currentPlaylistQueueIndex = if (startIndex in playlist.items.indices) startIndex else 0

        val itemToPlay = activePlaylistQueue[currentPlaylistQueueIndex]
        val media = MediaItem(
            id = itemToPlay.mediaId,
            title = itemToPlay.mediaTitle,
            description = itemToPlay.mediaDescription,
            category = itemToPlay.mediaCategory,
            durationSeconds = itemToPlay.mediaDurationSeconds,
            resolution = itemToPlay.mediaResolution,
            videoUrl = itemToPlay.mediaUrl,
            thumbnailUrl = itemToPlay.mediaThumbnailUrl
        )

        playMedia(media)
        emitToast("▶️ \"${playlist.name}\" oynatılıyor (${currentPlaylistQueueIndex + 1}/${activePlaylistQueue.size})")
    }

    fun playNextInPlaylistQueue() {
        if (activePlaylistQueue.isNotEmpty() && currentPlaylistQueueIndex + 1 < activePlaylistQueue.size) {
            currentPlaylistQueueIndex++
            val itemToPlay = activePlaylistQueue[currentPlaylistQueueIndex]
            val media = MediaItem(
                id = itemToPlay.mediaId,
                title = itemToPlay.mediaTitle,
                description = itemToPlay.mediaDescription,
                category = itemToPlay.mediaCategory,
                durationSeconds = itemToPlay.mediaDurationSeconds,
                resolution = itemToPlay.mediaResolution,
                videoUrl = itemToPlay.mediaUrl,
                thumbnailUrl = itemToPlay.mediaThumbnailUrl
            )
            playMedia(media)
            emitToast("⏭️ Sonraki video: ${media.title}")
        }
    }

    fun toggleTvPreviewFullscreen() {
        _isTvPreviewFullscreen.value = !_isTvPreviewFullscreen.value
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startDeviceScan() {
        scanJob?.cancel()
        _castState.update { it.copy(status = ConnectionStatus.SCANNING) }
        nsdScanner.startScan()
        nsdDiscoveryRepository.startDiscovery()

        scanJob = viewModelScope.launch {
            emitToast("🔍 NsdManager ile WiFi ağındaki Smart TV'ler taranıyor...")
            delay(1200) // Initial scanning period
            _castState.update {
                it.copy(
                    status = if (it.connectedDevice != null) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
                )
            }
        }
    }

    fun stopDeviceScan() {
        scanJob?.cancel()
        nsdScanner.stopScan()
        nsdDiscoveryRepository.stopDiscovery()
        _castState.update {
            it.copy(
                status = if (it.connectedDevice != null) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
            )
        }
    }

    fun addManualDevice(name: String, ipAddress: String, port: Int, protocol: CastProtocol) {
        if (name.isBlank() || ipAddress.isBlank()) {
            emitToast("Lütfen TV adı ve IP adresini girin!")
            return
        }

        val brand = when (protocol) {
            CastProtocol.SAMSUNG_SMART_VIEW -> DeviceBrand.SAMSUNG
            CastProtocol.LG_WEBOS -> DeviceBrand.LG
            CastProtocol.AIRPLAY -> DeviceBrand.APPLE_TV
            CastProtocol.CHROMECAST -> DeviceBrand.CHROMECAST
            CastProtocol.ROKU_DIAL -> DeviceBrand.ROKU
            else -> DeviceBrand.GENERIC_SMART_TV
        }

        val manualDevice = CastDevice(
            id = "manual_${ipAddress.replace(".", "_")}_$port",
            name = name.trim(),
            brand = brand,
            model = "Manuel IP (${protocol.name})",
            ipAddress = ipAddress.trim(),
            room = "Özel Konum",
            protocol = protocol,
            port = if (port > 0) port else 8008,
            signalStrengthPercentage = 95,
            isOnline = true,
            resolutionSupport = "4K HDR UHD",
            isNsdDiscovered = true,
            serviceType = "_manual._tcp."
        )

        nsdScanner.addOrUpdateDevice(manualDevice)
        nsdDiscoveryRepository.registerManualDevice(manualDevice)
        emitToast("✅ $name ($ipAddress) listeye eklendi!")
    }

    fun connectToDevice(device: CastDevice) {
        viewModelScope.launch {
            _castState.update { it.copy(status = ConnectionStatus.CONNECTING) }
            emitToast("📡 ${device.name} bağlantısı kuruluyor (${device.protocol.name})...")
            delay(1100)

            _castState.update {
                it.copy(
                    status = ConnectionStatus.CONNECTED,
                    connectedDevice = device,
                    quality = StreamQuality.UHD_4K
                )
            }
            _showCastDialog.value = false
            emitToast("✅ ${device.name} TV'ye bağlandı! Yayına hazır.")

            // Log session in Room database
            val mediaTitle = _castState.value.currentMedia?.title ?: "Canlı Akış"
            val duration = _castState.value.currentMedia?.formattedDuration ?: "00:00"
            repository.logSession(
                deviceName = device.name,
                deviceModel = "${device.brand.name} - ${device.model}",
                mediaTitle = mediaTitle,
                duration = duration
            )

            // Record as recent successfully connected TV
            repository.recordSuccessfulConnection(device)
        }
    }

    fun removeRecentDevice(deviceId: String) {
        viewModelScope.launch {
            repository.removeRecentDevice(deviceId)
            emitToast("🗑️ TV son kullanılanlar listesinden kaldırıldı")
        }
    }

    fun clearRecentDevices() {
        viewModelScope.launch {
            repository.clearRecentDevices()
            emitToast("🧹 Son TV listesi temizlendi")
        }
    }

    fun disconnectDevice() {
        val devName = _castState.value.connectedDevice?.name ?: "TV"
        _castState.update {
            it.copy(
                status = ConnectionStatus.DISCONNECTED,
                connectedDevice = null,
                isScreenMirroring = false
            )
        }
        emitToast("🔌 $devName bağlantısı kesildi")
    }

    fun playMedia(media: MediaItem) {
        val targetDevice = _castState.value.connectedDevice
        val status = _castState.value.status

        _castState.update {
            it.copy(
                currentMedia = media,
                isPlaying = true,
                currentPositionSec = 0,
                durationSec = media.durationSeconds,
                quality = StreamQuality.UHD_4K
            )
        }

        startPlaybackTicker()

        if (status == ConnectionStatus.CONNECTED && targetDevice != null) {
            emitToast("📺 ${targetDevice.name} üzerinde oynatılıyor: ${media.title}")
            viewModelScope.launch {
                repository.logSession(
                    deviceName = targetDevice.name,
                    deviceModel = "${targetDevice.brand.name} - ${targetDevice.model}",
                    mediaTitle = media.title,
                    duration = media.formattedDuration
                )
            }
        } else {
            emitToast("▶️ Oynatılıyor: ${media.title}")
        }
    }

    fun togglePlayPause() {
        val isCurrentlyPlaying = _castState.value.isPlaying
        if (isCurrentlyPlaying) {
            _castState.update { it.copy(isPlaying = false) }
            stopPlaybackTicker()
            emitToast("⏸️ Duraklatıldı")
        } else {
            _castState.update { it.copy(isPlaying = true) }
            startPlaybackTicker()
            emitToast("▶️ Oynatılıyor")
        }
    }

    fun seekToFraction(fraction: Float) {
        val dur = _castState.value.durationSec
        val targetSec = (dur * fraction).toInt().coerceIn(0, dur)
        _castState.update { it.copy(currentPositionSec = targetSec) }
    }

    fun seekRelative(seconds: Int) {
        val current = _castState.value.currentPositionSec
        val dur = _castState.value.durationSec
        val newPos = (current + seconds).coerceIn(0, dur)
        _castState.update { it.copy(currentPositionSec = newPos) }
        val direction = if (seconds > 0) "+${seconds}sn İleri" else "${seconds}sn Geri"
        emitToast("⏩ $direction sarıldı")
    }

    fun setVolume(volume: Int) {
        val clamped = volume.coerceIn(0, 100)
        _castState.update { it.copy(volumePercent = clamped, isMuted = false) }
    }

    fun toggleMute() {
        val newMute = !_castState.value.isMuted
        _castState.update { it.copy(isMuted = newMute) }
        emitToast(if (newMute) "🔇 TV Sesi Kapatıldı" else "🔊 TV Sesi Açıldı")
    }

    fun setQuality(quality: StreamQuality) {
        _castState.update { it.copy(quality = quality) }
        emitToast("⚙️ Yayın Çözünürlüğü: ${quality.label}")
    }

    fun setSubtitle(subtitle: String) {
        _castState.update { it.copy(subtitleLanguage = subtitle) }
        emitToast("💬 Altyazı: $subtitle")
    }

    fun toggleScreenMirroring() {
        val newState = !_castState.value.isScreenMirroring
        _castState.update { it.copy(isScreenMirroring = newState) }
        if (newState) {
            emitToast("📱 Tablet Ekranı TV'ye Canlı Yansıtılıyor (Miracast/AirPlay Mirroring)")
        } else {
            emitToast("⏹️ Ekran yansıtma sonlandırıldı")
        }
    }

    // Remote Control commands
    fun sendRemoteCommand(command: String) {
        val device = _castState.value.connectedDevice?.name ?: "TV"
        when (command) {
            "POWER" -> emitToast("⚡ $device Güç Sinyali Gönderildi")
            "VOL_UP" -> {
                setVolume(_castState.value.volumePercent + 5)
                emitToast("🔊 TV Sesi: %${_castState.value.volumePercent}")
            }
            "VOL_DOWN" -> {
                setVolume(_castState.value.volumePercent - 5)
                emitToast("🔉 TV Sesi: %${_castState.value.volumePercent}")
            }
            "MUTE" -> toggleMute()
            "HOME" -> emitToast("🏠 $device Ana Menüye Dönüldü")
            "BACK" -> emitToast("↩️ $device Geri")
            "SOURCE" -> emitToast("🔌 $device Giriş Kaynağı (HDMI/TV) Değiştirildi")
            "UP" -> emitToast("⬆️ Yukarı")
            "DOWN" -> emitToast("⬇️ Aşağı")
            "LEFT" -> emitToast("⬅️ Sol")
            "RIGHT" -> emitToast("➡️ Sağ")
            "OK" -> emitToast("🔘 Seçildi (OK)")
        }
    }

    fun addCustomStream(title: String, url: String, category: String, resolution: String) {
        if (title.isBlank() || url.isBlank()) {
            emitToast("Lütfen başlık ve geçerli bir URL girin!")
            return
        }
        viewModelScope.launch {
            repository.addCustomStream(
                title = title.trim(),
                url = url.trim(),
                category = category.ifBlank { "Özel Yayın" },
                resolution = resolution.ifBlank { "1080p HD" }
            )
            _showAddUrlDialog.value = false
            emitToast("✅ Yeni video akışı başarıyla eklendi!")
        }
    }

    fun deleteCustomStream(id: String) {
        viewModelScope.launch {
            repository.deleteCustomStream(id)
            emitToast("🗑️ Akış kaldırıldı")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            emitToast("🧹 Yayın geçmişi temizlendi")
        }
    }

    private fun startPlaybackTicker() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_castState.value.isPlaying) {
                delay(1000)
                val current = _castState.value
                if (current.currentPositionSec >= current.durationSec && current.durationSec > 0) {
                    // Auto-advance in playlist if queue is active
                    if (activePlaylistQueue.isNotEmpty() && currentPlaylistQueueIndex + 1 < activePlaylistQueue.size) {
                        playNextInPlaylistQueue()
                    } else {
                        _castState.update { it.copy(currentPositionSec = 0, isPlaying = false) }
                        break
                    }
                } else {
                    _castState.update { it.copy(currentPositionSec = it.currentPositionSec + 1) }
                }
            }
        }
    }

    private fun stopPlaybackTicker() {
        playbackJob?.cancel()
    }

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        nsdScanner.onDestroy()
        nsdDiscoveryRepository.release()
    }
}
