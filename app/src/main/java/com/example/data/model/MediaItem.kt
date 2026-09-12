package com.example.data.model

data class MediaItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val durationSeconds: Int,
    val resolution: String, // e.g. "4K UHD", "1080p 60FPS", "8K HDR"
    val fps: Int = 60,
    val videoUrl: String,
    val thumbnailUrl: String,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
