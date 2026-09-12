package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CastState
import com.example.data.model.StreamQuality
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.theme.CastRoseDanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvRemoteBottomSheet(
    castState: CastState,
    onSendCommand: (String) -> Unit,
    onSetVolume: (Int) -> Unit,
    onSetQuality: (StreamQuality) -> Unit,
    onSetSubtitle: (String) -> Unit,
    onToggleMirroring: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val connectedTvName = castState.connectedDevice?.name ?: "Akıllı TV"

    var showQualityMenu by remember { mutableStateOf(false) }
    var showSubtitleMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF334155))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("tv_remote_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Remote Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📺 TV Uzaktan Kumandası",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = connectedTvName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CastCyanAccent,
                                fontSize = 12.sp
                            )
                        )
                        val battery = castState.connectedDevice?.batteryLevel
                        if (battery != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(0.5.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (battery > 20) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                                        contentDescription = "Pil Seviyesi",
                                        tint = if (battery > 50) CastEmeraldSuccess else if (battery > 20) CastCyanAccent else CastRoseDanger,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "%$battery",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_remote_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Power, Source, Mirror Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteCircleButton(
                    icon = Icons.Default.PowerSettingsNew,
                    label = "Güç",
                    tint = CastRoseDanger,
                    bgColor = CastRoseDanger.copy(alpha = 0.15f),
                    onClick = { onSendCommand("POWER") }
                )
                RemoteCircleButton(
                    icon = Icons.Default.Input,
                    label = "Giriş (HDMI)",
                    tint = Color(0xFFE2E8F0),
                    bgColor = Color(0xFF1E293B),
                    onClick = { onSendCommand("SOURCE") }
                )
                RemoteCircleButton(
                    icon = Icons.Rounded.ScreenShare,
                    label = if (castState.isScreenMirroring) "Yansıtılıyor" else "Ekran Yansıt",
                    tint = if (castState.isScreenMirroring) CastCyanAccent else Color(0xFFE2E8F0),
                    bgColor = if (castState.isScreenMirroring) CastCyanAccent.copy(alpha = 0.2f) else Color(0xFF1E293B),
                    onClick = onToggleMirroring
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // D-PAD Controller Canvas/Surface
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color(0xFF334155), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Up Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { onSendCommand("UP") }
                        .testTag("remote_up_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowDropUp, contentDescription = "Yukarı", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                // Down Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { onSendCommand("DOWN") }
                        .testTag("remote_down_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Aşağı", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                // Left Button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { onSendCommand("LEFT") }
                        .testTag("remote_left_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Sol", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                // Right Button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { onSendCommand("RIGHT") }
                        .testTag("remote_right_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Sağ", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                // Center OK Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(CastCyanAccent, CastIndigoPrimary)))
                        .clickable { onSendCommand("OK") }
                        .testTag("remote_ok_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Row: Back & Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RemoteCircleButton(
                    icon = Icons.Default.ArrowBack,
                    label = "Geri",
                    tint = Color(0xFFE2E8F0),
                    bgColor = Color(0xFF1E293B),
                    onClick = { onSendCommand("BACK") }
                )
                RemoteCircleButton(
                    icon = Icons.Default.Home,
                    label = "Ana Menü",
                    tint = Color(0xFFE2E8F0),
                    bgColor = Color(0xFF1E293B),
                    onClick = { onSendCommand("HOME") }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Volume Control Slider & Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TV Ses Düzeyi",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (castState.isMuted) "Sessiz (Muted)" else "%${castState.volumePercent}",
                            color = if (castState.isMuted) CastRoseDanger else CastCyanAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onSendCommand("MUTE") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (castState.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                                contentDescription = "Sessiz",
                                tint = if (castState.isMuted) CastRoseDanger else Color(0xFF94A3B8)
                            )
                        }

                        Slider(
                            value = castState.volumePercent.toFloat(),
                            onValueChange = { onSetVolume(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = CastCyanAccent,
                                activeTrackColor = CastCyanAccent,
                                inactiveTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { onSendCommand("VOL_UP") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Ses Artır",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quality & Subtitles Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quality selector chip
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showQualityMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Kalite", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(castState.quality.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                        }
                    }

                    DropdownMenu(
                        expanded = showQualityMenu,
                        onDismissRequest = { showQualityMenu = false }
                    ) {
                        StreamQuality.values().forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q.label) },
                                onClick = {
                                    onSetQuality(q)
                                    showQualityMenu = false
                                }
                            )
                        }
                    }
                }

                // Subtitle selector chip
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSubtitleMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Altyazı", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(castState.subtitleLanguage, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Rounded.Subtitles, contentDescription = null, tint = CastCyanAccent)
                        }
                    }

                    DropdownMenu(
                        expanded = showSubtitleMenu,
                        onDismissRequest = { showSubtitleMenu = false }
                    ) {
                        listOf("Türkçe (Varsayılan)", "İngilizce (Original)", "Almanca", "Kapalı").forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    onSetSubtitle(sub)
                                    showSubtitleMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RemoteCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.dp, tint.copy(alpha = 0.4f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
