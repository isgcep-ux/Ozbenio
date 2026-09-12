package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cast_sessions")
data class CastSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceName: String,
    val deviceModel: String,
    val mediaTitle: String,
    val durationFormatted: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_streams")
data class CustomStreamEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val url: String,
    val category: String,
    val resolution: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String = "",
    val colorHex: String = "#06B6D4",
    val iconName: String = "playlist_play",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_items",
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["playlistId", "mediaId"])
    ]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: String,
    val mediaId: String,
    val mediaTitle: String,
    val mediaDescription: String = "",
    val mediaUrl: String,
    val mediaThumbnailUrl: String,
    val mediaDurationSeconds: Int,
    val mediaResolution: String = "4K UHD",
    val mediaCategory: String = "Genel",
    val orderIndex: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_devices")
data class RecentDeviceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val brandName: String,
    val model: String,
    val ipAddress: String,
    val room: String,
    val protocolName: String,
    val signalStrengthPercentage: Int = 90,
    val resolutionSupport: String = "4K Ultra HD",
    val port: Int = 8008,
    val serviceType: String = "_googlecast._tcp.",
    val lastConnectedTimestamp: Long = System.currentTimeMillis()
)

