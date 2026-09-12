package com.example.data.repository

import com.example.data.local.CastDao
import com.example.data.local.CastSessionEntity
import com.example.data.local.CustomStreamEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.RecentDeviceEntity
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.DeviceBrand
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class CastRepository(
    private val castDao: CastDao,
    val nsdDiscoveryRepository: SmartTvDiscoveryRepository? = null
) {

    /**
     * Start local WiFi mDNS network discovery for Smart TVs using NsdManager.
     */
    fun startNetworkDiscovery() {
        nsdDiscoveryRepository?.startDiscovery()
    }

    /**
     * Stop active NsdManager service discovery.
     */
    fun stopNetworkDiscovery() {
        nsdDiscoveryRepository?.stopDiscovery()
    }

    val sampleDevices: List<CastDevice> = listOf(
        CastDevice(
            id = "dev_samsung_qled",
            name = "Salon Samsung Neo QLED 75\"",
            brand = DeviceBrand.SAMSUNG,
            model = "QN90C 4K Smart Hub",
            ipAddress = "192.168.1.112",
            room = "Salon (Oturma Odası)",
            protocol = CastProtocol.SAMSUNG_SMART_VIEW,
            signalStrengthPercentage = 98,
            resolutionSupport = "4K 120Hz HDR10+",
            port = 8001,
            isNsdDiscovered = false,
            serviceType = "_smartview._tcp."
        ),
        CastDevice(
            id = "dev_lg_oled",
            name = "Yatak Odası LG OLED C3 55\"",
            brand = DeviceBrand.LG,
            model = "webOS 24 ThinQ AI",
            ipAddress = "192.168.1.115",
            room = "Yatak Odası",
            protocol = CastProtocol.LG_WEBOS,
            signalStrengthPercentage = 92,
            resolutionSupport = "4K Dolby Vision / Atmos",
            port = 3000,
            isNsdDiscovered = false,
            serviceType = "_webos-second-screen._tcp."
        ),
        CastDevice(
            id = "dev_sony_bravia",
            name = "Çalışma Odası Sony BRAVIA XR",
            brand = DeviceBrand.SONY,
            model = "A80L Google TV",
            ipAddress = "192.168.1.120",
            room = "Çalışma Odası",
            protocol = CastProtocol.CHROMECAST,
            signalStrengthPercentage = 88,
            resolutionSupport = "4K IMAX Enhanced",
            port = 8008,
            isNsdDiscovered = false,
            serviceType = "_googlecast._tcp."
        ),
        CastDevice(
            id = "dev_philips_ambilight",
            name = "Misafir Odası Philips The One",
            brand = DeviceBrand.PHILIPS,
            model = "Ambilight 4K TV",
            ipAddress = "192.168.1.128",
            room = "Misafir Odası",
            protocol = CastProtocol.DLNA_UPNP,
            signalStrengthPercentage = 85,
            resolutionSupport = "4K HDR10+ Ambilight",
            port = 8080,
            isNsdDiscovered = false,
            serviceType = "_upnp._tcp."
        ),
        CastDevice(
            id = "dev_vestel_android",
            name = "Mutfak Vestel Smart Android TV",
            brand = DeviceBrand.VESTEL,
            model = "Q9900 Android 13 TV",
            ipAddress = "192.168.1.134",
            room = "Mutfak",
            protocol = CastProtocol.CHROMECAST,
            signalStrengthPercentage = 80,
            resolutionSupport = "4K Ultra HD",
            port = 8008,
            isNsdDiscovered = false,
            serviceType = "_googlecast._tcp."
        ),
        CastDevice(
            id = "dev_apple_tv",
            name = "Ev Sineması Apple TV 4K",
            brand = DeviceBrand.APPLE_TV,
            model = "A2843 AirPlay 2",
            ipAddress = "192.168.1.140",
            room = "Ev Sineması",
            protocol = CastProtocol.AIRPLAY,
            signalStrengthPercentage = 96,
            resolutionSupport = "4K HDR Dolby Vision",
            port = 7000,
            isNsdDiscovered = false,
            serviceType = "_airplay._tcp."
        ),
        CastDevice(
            id = "dev_roku_tcl",
            name = "Teras TCL 4K Roku TV",
            brand = DeviceBrand.ROKU,
            model = "TCL 6-Series Roku OS",
            ipAddress = "192.168.1.145",
            room = "Balkon / Teras",
            protocol = CastProtocol.ROKU_DIAL,
            signalStrengthPercentage = 76,
            resolutionSupport = "4K HDR Dolby Vision",
            port = 8060,
            isNsdDiscovered = false,
            serviceType = "_dial-multiscreen-org._tcp."
        )
    )

    val sampleMediaLibrary: List<MediaItem> = listOf(
        MediaItem(
            id = "med_space_odyssey",
            title = "Kozmik Yolculuk: Derin Uzay & Galaksiler",
            description = "James Webb & Hubble teleskoplarından 4K ultra yüksek çözünürlüklü derin uzay ve nebula görüntüleri.",
            category = "Belgesel & Bilim",
            durationSeconds = 640,
            resolution = "4K 60FPS HDR",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80"
        ),
        MediaItem(
            id = "med_ocean_reef",
            title = "Tropik Mercan Resifleri & Okyanus Yaşamı",
            description = "Büyük Set Resifi ve Maldivler'in büyüleyici sualtı dünyası ve deniz canlıları.",
            category = "Doğa & Vahşi Yaşam",
            durationSeconds = 720,
            resolution = "4K UHD 60FPS",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&auto=format&fit=crop&q=80"
        ),
        MediaItem(
            id = "med_cyberpunk_drone",
            title = "Gece Şehri: Neon Siberpunk Tokyo Drone Çekimi",
            description = "Shinjuku ve Shibuya caddeleri üzerinde 4K gece ışıkları ve fütüristik atmosfer.",
            category = "Sinematik & Şehir",
            durationSeconds = 480,
            resolution = "4K Dolby Vision",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800&auto=format&fit=crop&q=80"
        ),
        MediaItem(
            id = "med_alpine_peaks",
            title = "İsviçre Alpleri & Zirvelerde Sonbahar",
            description = "Matterhorn ve kristal dağ göllerinin epik drone görüntüleri ve 8K detaylar.",
            category = "Seyahat & Manzara",
            durationSeconds = 540,
            resolution = "4K UHD HDR",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80"
        ),
        MediaItem(
            id = "med_cozy_fireplace",
            title = "Huzurlu Şömine & Yağmur Ambiyansı",
            description = "Rahatlatıcı ahşap çıtırtıları, şömine ateşi ve arkada hafif yağmur sesi (Doğal Meditasyon).",
            category = "Ambiyans & Rahatlama",
            durationSeconds = 1200,
            resolution = "4K HDR Atmos",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1542332213-9b5a5a3fad35?w=800&auto=format&fit=crop&q=80"
        ),
        MediaItem(
            id = "med_aurora_borealis",
            title = "Kuzey Işıkları: Aurora Borealis Norveç",
            description = "Tromsø ve Lofoten adalarında dans eden yeşil ve mor kuzey ışıkları zaman atlaması.",
            category = "Gökyüzü & Fenomen",
            durationSeconds = 420,
            resolution = "4K 60FPS HDR",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=800&auto=format&fit=crop&q=80"
        ),
        MediaItem(
            id = "med_supercar_track",
            title = "Pist Günü: Hypercar Hız & Motor Sesi 4K",
            description = "Nürburgring Nordschleife pistinde yüksek performanslı süper otomobiller ve 5.1 motor sesi.",
            category = "Aksiyon & Motor Sporu",
            durationSeconds = 360,
            resolution = "4K 60FPS HDR",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80"
        )
    )

    fun getAllSessions(): Flow<List<CastSessionEntity>> = castDao.getAllSessions()

    suspend fun logSession(deviceName: String, deviceModel: String, mediaTitle: String, duration: String) {
        castDao.insertSession(
            CastSessionEntity(
                deviceName = deviceName,
                deviceModel = deviceModel,
                mediaTitle = mediaTitle,
                durationFormatted = duration
            )
        )
    }

    suspend fun clearHistory() {
        castDao.clearAllSessions()
    }

    fun getCustomStreams(): Flow<List<CustomStreamEntity>> = castDao.getAllCustomStreams()

    suspend fun addCustomStream(title: String, url: String, category: String, resolution: String) {
        val entity = CustomStreamEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            url = url,
            category = category,
            resolution = resolution
        )
        castDao.insertCustomStream(entity)
    }

    suspend fun deleteCustomStream(id: String) {
        castDao.deleteCustomStream(id)
    }

    // --- Playlist Operations ---
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = castDao.getAllPlaylists()

    fun getPlaylistItems(playlistId: String): Flow<List<PlaylistItemEntity>> =
        castDao.getPlaylistItems(playlistId)

    fun getAllPlaylistItems(): Flow<List<PlaylistItemEntity>> = castDao.getAllPlaylistItems()

    suspend fun createPlaylist(
        name: String,
        description: String = "",
        colorHex: String = "#06B6D4",
        iconName: String = "playlist_play"
    ): String {
        val id = "pl_" + UUID.randomUUID().toString().take(8)
        val entity = PlaylistEntity(
            id = id,
            name = name.trim(),
            description = description.trim(),
            colorHex = colorHex,
            iconName = iconName
        )
        castDao.insertPlaylist(entity)
        return id
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) {
        castDao.updatePlaylist(playlist)
    }

    suspend fun deletePlaylist(playlistId: String) {
        castDao.deletePlaylistWithItems(playlistId)
    }

    suspend fun addMediaToPlaylist(playlistId: String, media: MediaItem): Boolean {
        val currentMaxOrder = castDao.getMaxOrderIndex(playlistId) ?: -1
        val newItem = PlaylistItemEntity(
            playlistId = playlistId,
            mediaId = media.id,
            mediaTitle = media.title,
            mediaDescription = media.description,
            mediaUrl = media.videoUrl,
            mediaThumbnailUrl = media.thumbnailUrl,
            mediaDurationSeconds = media.durationSeconds,
            mediaResolution = media.resolution,
            mediaCategory = media.category,
            orderIndex = currentMaxOrder + 1
        )
        castDao.insertPlaylistItem(newItem)
        return true
    }

    suspend fun removePlaylistItemById(itemId: Long) {
        castDao.deletePlaylistItemById(itemId)
    }

    suspend fun removeMediaFromPlaylist(playlistId: String, mediaId: String) {
        castDao.deletePlaylistItem(playlistId, mediaId)
    }

    suspend fun movePlaylistItemUp(playlistId: String, item: PlaylistItemEntity) {
        val items = castDao.getPlaylistItemsSync(playlistId).toMutableList()
        val index = items.indexOfFirst { it.id == item.id }
        if (index > 0) {
            val prev = items[index - 1]
            val curr = items[index]
            val updatedCurr = curr.copy(orderIndex = prev.orderIndex)
            val updatedPrev = prev.copy(orderIndex = curr.orderIndex)
            castDao.updatePlaylistItems(listOf(updatedCurr, updatedPrev))
        }
    }

    suspend fun movePlaylistItemDown(playlistId: String, item: PlaylistItemEntity) {
        val items = castDao.getPlaylistItemsSync(playlistId).toMutableList()
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0 && index < items.size - 1) {
            val next = items[index + 1]
            val curr = items[index]
            val updatedCurr = curr.copy(orderIndex = next.orderIndex)
            val updatedNext = next.copy(orderIndex = curr.orderIndex)
            castDao.updatePlaylistItems(listOf(updatedCurr, updatedNext))
        }
    }

    suspend fun seedDefaultPlaylistsIfEmpty() {
        val currentPlaylists = castDao.getAllPlaylists().firstOrNull()
        if (currentPlaylists.isNullOrEmpty()) {
            val cinemaId = createPlaylist(
                name = "Favori 4K Sinema",
                description = "Büyük ekran TV için yüksek çözünürlüklü sinematik ve gece manzaraları.",
                colorHex = "#3B82F6",
                iconName = "movie"
            )
            sampleMediaLibrary.filter {
                it.id == "med_cyberpunk_drone" || it.id == "med_space_odyssey" || it.id == "med_supercar_track"
            }.forEach { media ->
                addMediaToPlaylist(cinemaId, media)
            }

            val relaxId = createPlaylist(
                name = "Doğa & Rahatlama",
                description = "Rahatlatıcı doğa sesleri, huzurlu şömine ve kutup ışıkları.",
                colorHex = "#10B981",
                iconName = "nature"
            )
            sampleMediaLibrary.filter {
                it.id == "med_ocean_reef" || it.id == "med_cozy_fireplace" || it.id == "med_aurora_borealis"
            }.forEach { media ->
                addMediaToPlaylist(relaxId, media)
            }
        }
    }

    // --- Recent Devices Operations (Last 5 TVs) ---
    fun getRecentDevices(): Flow<List<RecentDeviceEntity>> = castDao.getRecentDevices()

    suspend fun recordSuccessfulConnection(device: CastDevice) {
        val entity = RecentDeviceEntity(
            id = device.id,
            name = device.name,
            brandName = device.brand.name,
            model = device.model,
            ipAddress = device.ipAddress,
            room = device.room,
            protocolName = device.protocol.name,
            signalStrengthPercentage = device.signalStrengthPercentage,
            resolutionSupport = device.resolutionSupport,
            port = device.port,
            serviceType = device.serviceType,
            lastConnectedTimestamp = System.currentTimeMillis()
        )
        castDao.insertOrUpdateRecentDevice(entity)
    }

    suspend fun removeRecentDevice(deviceId: String) {
        castDao.deleteRecentDevice(deviceId)
    }

    suspend fun clearRecentDevices() {
        castDao.clearRecentDevices()
    }

    suspend fun seedDefaultRecentDevicesIfEmpty() {
        val currentRecent = castDao.getRecentDevices().firstOrNull()
        if (currentRecent.isNullOrEmpty()) {
            // Seed top 3 popular devices as initial recently connected TVs
            sampleDevices.take(3).forEach { device ->
                recordSuccessfulConnection(device)
            }
        }
    }
}

