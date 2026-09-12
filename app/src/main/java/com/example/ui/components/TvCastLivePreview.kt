package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CastState
import com.example.data.model.ConnectionStatus
import com.example.data.model.StreamQuality
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastGlowBlue
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.theme.CastRoseDanger

@Composable
fun TvCastLivePreview(
    castState: CastState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekRelative: (Int) -> Unit,
    onOpenCastDialog: () -> Unit,
    onOpenRemote: () -> Unit,
    onToggleMirroring: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onSelectQuality: ((StreamQuality) -> Unit)? = null,
    onVolumeChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentMedia = castState.currentMedia
    val isConnected = castState.status == ConnectionStatus.CONNECTED && castState.connectedDevice != null
    val isPlaying = castState.isPlaying

    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val waveBar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w1"
    )
    val waveBar2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w2"
    )
    val waveBar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w3"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tv_cast_live_preview_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1322)
        ),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(
                    if (isConnected) CastCyanAccent.copy(alpha = 0.6f) else Color(0xFF334155),
                    if (isConnected) CastEmeraldSuccess.copy(alpha = 0.4f) else Color(0xFF1E293B)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // TV Monitor Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) CastEmeraldSuccess.copy(alpha = 0.2f) else Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Tv else Icons.Default.Cast,
                            contentDescription = null,
                            tint = if (isConnected) CastEmeraldSuccess else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (isConnected) "📺 ${castState.connectedDevice?.name}" else "📺 TV Bağlı Değil (Tablet Yerel Önizleme)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isConnected && isPlaying) "Canlı TV Akışı • Senkronize Oynatılıyor"
                            else if (isConnected) "TV'ye Bağlı • Hazır"
                            else "TV'de izlemek için Cast butonuna dokunun",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isConnected) CastEmeraldSuccess else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Quick Cast Button on top
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isConnected) CastEmeraldSuccess.copy(alpha = 0.15f) else CastCyanAccent.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (isConnected) CastEmeraldSuccess else CastCyanAccent
                    ),
                    modifier = Modifier.clickable(onClick = onOpenCastDialog).testTag("quick_cast_status_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                            contentDescription = "Cast",
                            tint = if (isConnected) CastEmeraldSuccess else CastCyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConnected) "Bağlı" else "📺 Cast",
                            color = if (isConnected) CastEmeraldSuccess else CastCyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated TV Screen Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF000000))
                    .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                // Background Video Poster / Thumbnail
                if (currentMedia != null) {
                    AsyncImage(
                        model = currentMedia.thumbnailUrl,
                        contentDescription = currentMedia.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Interactive Direct Casting Playback Overlay Component
                CastingPlaybackOverlay(
                    isPlaying = isPlaying,
                    currentPositionSec = castState.currentPositionSec,
                    durationSec = castState.durationSec,
                    volumePercent = castState.volumePercent,
                    isMuted = castState.isMuted,
                    mediaTitle = currentMedia?.title ?: "Yayınlanacak Video Seçin",
                    mediaCategory = currentMedia?.category ?: "",
                    connectedDevice = castState.connectedDevice,
                    resolution = currentMedia?.resolution ?: "4K UHD",
                    quality = castState.quality,
                    isScreenMirroring = castState.isScreenMirroring,
                    onTogglePlayPause = onTogglePlayPause,
                    onSeekFraction = onSeek,
                    onSeekRelative = onSeekRelative,
                    onVolumeChange = { onVolumeChange?.invoke(it) },
                    onToggleMute = onToggleMute,
                    onSelectQuality = onSelectQuality,
                    onOpenRemote = onOpenRemote,
                    onToggleFullscreen = onToggleFullscreen,
                    onToggleMirroring = onToggleMirroring,
                    onOpenCastPicker = onOpenCastDialog,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seek Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = castState.formattedCurrentTime,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Slider(
                    value = castState.progressFraction,
                    onValueChange = onSeek,
                    colors = SliderDefaults.colors(
                        thumbColor = CastCyanAccent,
                        activeTrackColor = CastCyanAccent,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("video_progress_slider")
                )

                Text(
                    text = castState.formattedDurationTime,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Playback Control HUD Actions (Tablet-first Ergonomics)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Left Tools
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier.testTag("mute_button")
                    ) {
                        Icon(
                            imageVector = if (castState.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Ses",
                            tint = if (castState.isMuted) CastRoseDanger else Color(0xFFCBD5E1)
                        )
                    }

                    IconButton(
                        onClick = onToggleMirroring,
                        modifier = Modifier.testTag("mirror_screen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ScreenShare,
                            contentDescription = "Ekran Yansıt",
                            tint = if (castState.isScreenMirroring) CastCyanAccent else Color(0xFFCBD5E1)
                        )
                    }
                }

                // Primary Center Controls (Rewind 10, Play/Pause, Forward 10)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onSeekRelative(-10) },
                        modifier = Modifier.testTag("rewind_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "10sn Geri",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(CastCyanAccent, CastIndigoPrimary)))
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("main_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Oynat",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSeekRelative(10) },
                        modifier = Modifier.testTag("forward_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "10sn İleri",
                            tint = Color.White
                        )
                    }
                }

                // Secondary Right Tools (TV Remote & Fullscreen)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenRemote,
                        modifier = Modifier.testTag("open_remote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsRemote,
                            contentDescription = "TV Kumandası",
                            tint = CastCyanAccent
                        )
                    }

                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier.testTag("toggle_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Tam Ekran",
                            tint = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}
