package com.example.data.model

import com.example.data.local.PlaylistItemEntity

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val colorHex: String = "#06B6D4",
    val iconName: String = "playlist_play",
    val itemCount: Int = 0,
    val totalDurationSeconds: Int = 0,
    val items: List<PlaylistItemEntity> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalDurationFormatted: String
        get() {
            val hours = totalDurationSeconds / 3600
            val minutes = (totalDurationSeconds % 3600) / 60
            val seconds = totalDurationSeconds % 60
            return if (hours > 0) {
                String.format("%d sa %d dk", hours, minutes)
            } else {
                String.format("%d dk %d sn", minutes, seconds)
            }
        }
}
