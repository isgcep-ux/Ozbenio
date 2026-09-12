package com.example.data.model

enum class ConnectionStatus {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED
}

enum class StreamQuality(
    val label: String,
    val shortLabel: String,
    val bitrate: String,
    val description: String
) {
    AUTO("Otomatik (Ağ Uyumlu)", "Auto", "Dinamik", "Bağlantı hızına göre otomatik ayarlanır"),
    UHD_4K("4K Ultra HD (2160p)", "4K UHD", "25 Mbps", "Yüksek hızlı 5GHz/Fiber ağlar için en yüksek kalite"),
    FHD_1080P("1080p Full HD (60fps)", "1080p", "8 Mbps", "Önerilen dengeli ve akıcı yayın kalitesi"),
    HD_720P("720p HD (Hızlı Akış)", "720p", "4 Mbps", "Akıcı performans ve düşük ağ gecikmesi"),
    SD_480P("480p SD (Veri Tasarrufu)", "480p", "1.5 Mbps", "Zayıf WiFi bağlantıları ve veri tasarrufu için")
}

data class CastState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedDevice: CastDevice? = null,
    val currentMedia: MediaItem? = null,
    val isPlaying: Boolean = false,
    val currentPositionSec: Int = 0,
    val durationSec: Int = 0,
    val volumePercent: Int = 75,
    val isMuted: Boolean = false,
    val quality: StreamQuality = StreamQuality.AUTO,
    val subtitleLanguage: String = "Türkçe (Varsayılan)",
    val audioTrack: String = "Dolby Atmos 5.1 (Orijinal)",
    val isScreenMirroring: Boolean = false,
    val wifiSsid: String = "Ev-Ağı_5GHz",
    val tabletIp: String = "192.168.1.108"
) {
    val progressFraction: Float
        get() = if (durationSec > 0) (currentPositionSec.toFloat() / durationSec).coerceIn(0f, 1f) else 0f

    val formattedCurrentTime: String
        get() {
            val minutes = currentPositionSec / 60
            val seconds = currentPositionSec % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedDurationTime: String
        get() {
            val minutes = durationSec / 60
            val seconds = durationSec % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
