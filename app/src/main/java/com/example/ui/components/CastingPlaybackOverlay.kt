package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.rounded.GraphicEq
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CastDevice
import com.example.data.model.StreamQuality
import com.example.ui.theme.CastAmberWarning
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastGlowBlue
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.theme.CastRoseDanger
import kotlinx.coroutines.delay

/**
 * Reusable casting screen playback control overlay component.
 * Supports:
 * - Play / Pause with glowing touch feedback
 * - Fine-grained seek slider with live timestamp updates
 * - Relative quick skipping (-10s / +10s)
 * - Complete volume management (slider + mute toggle + step buttons)
 * - Auto-hiding interactive overlay touch gestures
 * - TV casting connection banner & remote controller shortcut
 */
@Composable
fun CastingPlaybackOverlay(
    isPlaying: Boolean,
    currentPositionSec: Int,
    durationSec: Int,
    volumePercent: Int,
    isMuted: Boolean,
    mediaTitle: String,
    mediaCategory: String,
    connectedDevice: CastDevice?,
    resolution: String = "4K UHD",
    quality: StreamQuality = StreamQuality.AUTO,
    isFullscreen: Boolean = false,
    isScreenMirroring: Boolean = false,
    autoHideDurationMs: Long = 4500L,
    onTogglePlayPause: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    onSeekRelative: (Int) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onToggleMute: () -> Unit,
    onSelectQuality: ((StreamQuality) -> Unit)? = null,
    onOpenRemote: (() -> Unit)? = null,
    onToggleFullscreen: (() -> Unit)? = null,
    onToggleMirroring: (() -> Unit)? = null,
    onOpenCastPicker: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var showVolumeSliderDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    // Auto-hide controls when media is playing
    LaunchedEffect(isPlaying, controlsVisible, showVolumeSliderDialog, showQualityDialog) {
        if (isPlaying && controlsVisible && !showVolumeSliderDialog && !showQualityDialog && autoHideDurationMs > 0) {
            delay(autoHideDurationMs)
            controlsVisible = false
        }
    }

    val progressFraction = if (durationSec > 0) {
        (currentPositionSec.toFloat() / durationSec.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val formattedCurrentTime = formatSecToTime(currentPositionSec)
    val formattedDurationTime = formatSecToTime(durationSec)

    // Animated Live Audio Spectrum
    val infiniteTransition = rememberInfiniteTransition(label = "audio_spectrum")
    val barAnim1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )
    val barAnim2 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(530, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )
    val barAnim3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(610, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
            }
            .testTag("casting_playback_overlay_container")
    ) {
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(220)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                // 1. TOP HEADER BAR: Casting Device & Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Casting device chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        border = BorderStroke(
                            1.dp,
                            if (connectedDevice != null) CastEmeraldSuccess else CastCyanAccent.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .clickable { onOpenCastPicker?.invoke() }
                            .testTag("overlay_cast_device_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (connectedDevice != null) CastEmeraldSuccess.copy(alpha = 0.25f)
                                        else CastCyanAccent.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (connectedDevice != null) Icons.Default.CastConnected else Icons.Default.Cast,
                                    contentDescription = null,
                                    tint = if (connectedDevice != null) CastEmeraldSuccess else CastCyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = connectedDevice?.name ?: "Yayınlanacak Cihaz Seç",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Quality & Audio spectrum indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, CastCyanAccent),
                            modifier = Modifier
                                .clickable { showQualityDialog = true }
                                .testTag("overlay_quality_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = null,
                                    tint = CastCyanAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = quality.shortLabel.ifBlank { resolution },
                                    color = CastCyanAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isPlaying) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CastEmeraldSuccess.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, CastEmeraldSuccess)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height((10 * barAnim1).dp)
                                            .background(CastEmeraldSuccess)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height((10 * barAnim2).dp)
                                            .background(CastEmeraldSuccess)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height((10 * barAnim3).dp)
                                            .background(CastEmeraldSuccess)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. CENTER PLAYBACK CONTROLS (Rewind 10, Big Play/Pause, Forward 10)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Rewind 10s
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, Color(0xFF475569), CircleShape)
                            .clickable { onSeekRelative(-10) }
                            .testTag("overlay_rewind_10_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "10 Saniye Geri",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Main Center Play / Pause
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        CastCyanAccent,
                                        CastIndigoPrimary
                                    )
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                            .clickable { onTogglePlayPause() }
                            .testTag("overlay_main_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Forward 10s
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, Color(0xFF475569), CircleShape)
                            .clickable { onSeekRelative(10) }
                            .testTag("overlay_forward_10_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "10 Saniye İleri",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Quick Volume Floating Bar (when toggled or on demand)
                if (showVolumeSliderDialog) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .width(54.dp)
                            .height(180.dp)
                            .testTag("overlay_volume_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)),
                        border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { onVolumeChange((volumePercent + 10).coerceAtMost(100)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Ses Artır",
                                    tint = CastCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = if (isMuted) "0%" else "$volumePercent%",
                                color = if (isMuted) CastRoseDanger else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeDown,
                                    contentDescription = "Sessiz",
                                    tint = if (isMuted) CastRoseDanger else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 3. BOTTOM CONTROL HUD: Title, Progress Bar, Volume & Tools
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Media Title & Metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = mediaTitle.ifBlank { "Yayın Hazır" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (mediaCategory.isNotBlank()) {
                                Text(
                                    text = mediaCategory,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Current Volume Quick Indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.clickable { showVolumeSliderDialog = !showVolumeSliderDialog }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        isMuted -> Icons.Default.VolumeMute
                                        volumePercent > 60 -> Icons.Default.VolumeUp
                                        volumePercent > 20 -> Icons.Default.VolumeDown
                                        else -> Icons.Default.VolumeOff
                                    },
                                    contentDescription = "Ses",
                                    tint = if (isMuted) CastRoseDanger else CastCyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMuted) "Sessiz" else "TV %$volumePercent",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Seek Bar with Timestamps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedCurrentTime,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Slider(
                            value = progressFraction,
                            onValueChange = onSeekFraction,
                            colors = SliderDefaults.colors(
                                thumbColor = CastCyanAccent,
                                activeTrackColor = CastCyanAccent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .testTag("overlay_seek_slider")
                        )

                        Text(
                            text = formattedDurationTime,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Bottom Utility Row (Volume slider inline, Mirroring, TV Remote, Fullscreen)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Volume Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier.size(32.dp).testTag("overlay_mute_button")
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Ses Aç/Kapat",
                                    tint = if (isMuted) CastRoseDanger else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Slider(
                                value = if (isMuted) 0f else volumePercent / 100f,
                                onValueChange = { onVolumeChange((it * 100).toInt()) },
                                colors = SliderDefaults.colors(
                                    thumbColor = if (isMuted) CastRoseDanger else CastCyanAccent,
                                    activeTrackColor = if (isMuted) CastRoseDanger else CastCyanAccent,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .width(100.dp)
                                    .testTag("overlay_inline_volume_slider")
                            )
                        }

                        // Right action items: Quality, Mirroring, Remote, Fullscreen
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (onSelectQuality != null) {
                                IconButton(
                                    onClick = { showQualityDialog = true },
                                    modifier = Modifier.size(32.dp).testTag("overlay_quality_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HighQuality,
                                        contentDescription = "Yayın Kalitesi",
                                        tint = CastCyanAccent,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            if (onToggleMirroring != null) {
                                IconButton(
                                    onClick = onToggleMirroring,
                                    modifier = Modifier.size(32.dp).testTag("overlay_mirror_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ScreenShare,
                                        contentDescription = "Ekran Yansıt",
                                        tint = if (isScreenMirroring) CastCyanAccent else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (onOpenRemote != null) {
                                IconButton(
                                    onClick = onOpenRemote,
                                    modifier = Modifier.size(32.dp).testTag("overlay_remote_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SettingsRemote,
                                        contentDescription = "TV Kumandası",
                                        tint = CastCyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (onToggleFullscreen != null) {
                                IconButton(
                                    onClick = onToggleFullscreen,
                                    modifier = Modifier.size(32.dp).testTag("overlay_fullscreen_button")
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Tam Ekran",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quality Selection Dialog
        if (showQualityDialog && onSelectQuality != null) {
            StreamingQualitySelectorDialog(
                currentQuality = quality,
                onSelectQuality = {
                    onSelectQuality(it)
                    showQualityDialog = false
                },
                onDismiss = { showQualityDialog = false }
            )
        }
    }
}

@Composable
fun StreamingQualitySelectorDialog(
    currentQuality: StreamQuality,
    onSelectQuality: (StreamQuality) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(CastCyanAccent, CastIndigoPrimary))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("streaming_quality_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CastCyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = CastCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Yayın Akış Kalitesi",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Ağ hızınıza göre performansı optimize edin",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Network optimization info banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = CastEmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "WiFi 5GHz • Bağlantı stabil (Önerilen: 1080p veya 4K)",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quality presets list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StreamQuality.values().forEach { q ->
                        val isSelected = q == currentQuality
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) CastCyanAccent.copy(alpha = 0.14f) else Color(0xFF131D33),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) CastCyanAccent else Color(0xFF1E293B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectQuality(q) }
                                .testTag("quality_option_${q.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Quality badge chip
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CastCyanAccent else Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, if (isSelected) CastCyanAccent else Color(0xFF334155))
                                    ) {
                                        Text(
                                            text = q.shortLabel,
                                            color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = q.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                                fontSize = 13.sp
                                            )
                                        )
                                        Text(
                                            text = q.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isSelected) CastCyanAccent.copy(alpha = 0.9f) else Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Bitrate tag
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Text(
                                            text = q.bitrate,
                                            color = if (isSelected) CastCyanAccent else Color(0xFF94A3B8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Checkmark
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(CastCyanAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seçildi",
                                                tint = Color(0xFF0F172A),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("close_quality_dialog_button")
                ) {
                    Text(
                        text = "Tamam",
                        color = CastCyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun formatSecToTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
