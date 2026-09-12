package com.example.data.model

enum class DeviceBrand(val displayName: String) {
    SAMSUNG("Samsung"),
    LG("LG"),
    SONY("Sony Bravia"),
    PHILIPS("Philips"),
    TCL("TCL"),
    VESTEL("Vestel"),
    ARCELIK("Arçelik"),
    CHROMECAST("Chromecast"),
    ROKU("Roku"),
    FIRE_TV("Fire TV"),
    APPLE_TV("Apple TV"),
    GENERIC_SMART_TV("Smart TV")
}

enum class CastProtocol(val displayName: String) {
    CHROMECAST("Google Cast"),
    DLNA_UPNP("DLNA / UPnP"),
    SAMSUNG_SMART_VIEW("Samsung SmartView"),
    LG_WEBOS("LG webOS Connect"),
    ROKU_DIAL("Roku DIAL"),
    AIRPLAY("AirPlay 2"),
    MIRACAST("Miracast")
}

data class CastDevice(
    val id: String,
    val name: String,
    val brand: DeviceBrand,
    val model: String,
    val ipAddress: String,
    val room: String,
    val protocol: CastProtocol,
    val signalStrengthPercentage: Int = 95, // 0 - 100
    val isOnline: Boolean = true,
    val resolutionSupport: String = "4K HDR Dolby Vision",
    val port: Int = 8008,
    val isNsdDiscovered: Boolean = false,
    val serviceType: String = "",
    val batteryLevel: Int? = 85
)

data class RecentTvDevice(
    val device: CastDevice,
    val lastConnectedTimestamp: Long = System.currentTimeMillis()
)

