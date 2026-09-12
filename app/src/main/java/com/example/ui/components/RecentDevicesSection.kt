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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CastDevice
import com.example.data.model.CastState
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceBrand
import com.example.data.model.RecentTvDevice
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastGlowBlue
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.theme.CastRoseDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentDevicesSection(
    recentDevices: List<RecentTvDevice>,
    castState: CastState,
    onReconnectDevice: (CastDevice) -> Unit,
    onDisconnectDevice: () -> Unit,
    onOpenRemote: () -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearAllRecent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = castState.status == ConnectionStatus.CONNECTING
    val connectedDeviceId = if (castState.status == ConnectionStatus.CONNECTED) castState.connectedDevice?.id else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recent_devices_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CastCyanAccent.copy(alpha = 0.25f), CastIndigoPrimary.copy(alpha = 0.4f))
                            )
                        )
                        .border(1.dp, CastCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = CastCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Son Bağlanılan TV'ler",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                        if (recentDevices.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = CastCyanAccent.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "${recentDevices.take(5).size}/5",
                                    color = CastCyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Tek dokunuşla hızlı yeniden bağlanma",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            if (recentDevices.isNotEmpty()) {
                TextButton(
                    onClick = onClearAllRecent,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("clear_recent_devices_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Temizle",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Temizle",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (recentDevices.isEmpty()) {
            // Empty State Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E).copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Henüz TV Bağlantısı Yok",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Başarıyla bağlandığınız son 5 TV burada listelenir ve tek tıkla tekrar bağlanabilirsiniz.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // Carousel of Last 5 Connected TVs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentDevices.take(5), key = { it.device.id }) { recentTv ->
                    val isCurrentConnected = connectedDeviceId == recentTv.device.id
                    val isThisConnecting = isConnecting && castState.connectedDevice?.id == recentTv.device.id

                    RecentTvCard(
                        recentTv = recentTv,
                        isCurrentlyConnected = isCurrentConnected,
                        isConnecting = isThisConnecting,
                        onReconnect = { onReconnectDevice(recentTv.device) },
                        onDisconnect = onDisconnectDevice,
                        onOpenRemote = onOpenRemote,
                        onRemove = { onRemoveRecent(recentTv.device.id) },
                        modifier = Modifier.testTag("recent_tv_card_${recentTv.device.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTvCard(
    recentTv: RecentTvDevice,
    isCurrentlyConnected: Boolean,
    isConnecting: Boolean,
    onReconnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenRemote: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val device = recentTv.device

    val infiniteTransition = rememberInfiniteTransition(label = "recent_tv_card_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "recent_pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "recent_pulse_alpha"
    )

    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recent_border_glow"
    )

    // Brand accent color mapping
    val brandColor = when (device.brand) {
        DeviceBrand.SAMSUNG -> Color(0xFF1428A0)
        DeviceBrand.LG -> Color(0xFFA50034)
        DeviceBrand.SONY -> Color(0xFFE50914)
        DeviceBrand.PHILIPS -> Color(0xFF0066A1)
        DeviceBrand.APPLE_TV -> Color(0xFF999999)
        DeviceBrand.ROKU -> Color(0xFF662D91)
        DeviceBrand.VESTEL -> Color(0xFFE31B23)
        DeviceBrand.CHROMECAST -> CastCyanAccent
        else -> CastIndigoPrimary
    }

    val cardBorder = when {
        isCurrentlyConnected -> BorderStroke(1.5.dp, CastEmeraldSuccess)
        isConnecting -> BorderStroke(1.5.dp, CastCyanAccent.copy(alpha = borderGlowAlpha))
        else -> BorderStroke(1.dp, Color(0xFF1E293B))
    }

    val cardBackground = when {
        isCurrentlyConnected -> Brush.verticalGradient(
            listOf(
                CastEmeraldSuccess.copy(alpha = 0.14f),
                Color(0xFF0F172A).copy(alpha = 0.95f)
            )
        )
        isConnecting -> Brush.verticalGradient(
            listOf(
                CastCyanAccent.copy(alpha = 0.12f * borderGlowAlpha),
                Color(0xFF0F1E38)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color(0xFF172033),
                Color(0xFF0F172A)
            )
        )
    }

    Card(
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = cardBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Row: Brand & Status Badge + Remove Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brand Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = brandColor.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, brandColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = device.brand.displayName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isCurrentlyConnected) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CastEmeraldSuccess.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, CastEmeraldSuccess)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(CastEmeraldSuccess)
                                    )
                                    Text(
                                        text = "BAĞLI",
                                        color = CastEmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (isConnecting) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CastCyanAccent.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = borderGlowAlpha))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(8.dp),
                                        strokeWidth = 1.5.dp,
                                        color = CastCyanAccent
                                    )
                                    Text(
                                        text = "BAĞLANIYOR",
                                        color = CastCyanAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Remove TV from history button
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("remove_recent_tv_${device.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Geçmişten Kaldır",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Device Name & Room
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isConnecting) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .scale(pulseScale)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CastCyanAccent.copy(alpha = pulseAlpha * 0.5f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isCurrentlyConnected -> CastEmeraldSuccess.copy(alpha = 0.2f)
                                        isConnecting -> CastCyanAccent.copy(alpha = 0.25f)
                                        else -> CastCyanAccent.copy(alpha = 0.12f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isCurrentlyConnected -> CastEmeraldSuccess
                                        isConnecting -> CastCyanAccent.copy(alpha = borderGlowAlpha)
                                        else -> CastCyanAccent.copy(alpha = 0.3f)
                                    },
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCurrentlyConnected) Icons.Default.CastConnected else Icons.Default.Tv,
                                contentDescription = null,
                                tint = when {
                                    isCurrentlyConnected -> CastEmeraldSuccess
                                    isConnecting -> CastCyanAccent
                                    else -> CastCyanAccent
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = device.room,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Technical Info Row (IP & Protocol & Time)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${device.ipAddress} • ${device.protocol.displayName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatRelativeTimestamp(recentTv.lastConnectedTimestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CastCyanAccent.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Area: One-Tap Reconnect or Manage Live Connection
                if (isCurrentlyConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenRemote,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CastIndigoPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("recent_tv_open_remote_${device.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsRemote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kumanda", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CastRoseDanger
                            ),
                            border = BorderStroke(1.dp, CastRoseDanger.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("recent_tv_disconnect_${device.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = CastRoseDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ayır", fontSize = 12.sp)
                        }
                    }
                } else {
                    // ONE-TAP RECONNECTION BUTTON
                    Button(
                        onClick = onReconnect,
                        enabled = !isConnecting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CastCyanAccent,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("one_tap_reconnect_button_${device.id}")
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color(0xFF0F172A),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bağlanıyor...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tek Dokunuşla Bağlan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format timestamp into human-readable relative time
 */
private fun formatRelativeTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - timestamp
    if (diffMillis < 0) return "Şimdi"

    val diffSeconds = diffMillis / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffMinutes < 1 -> "Az önce"
        diffMinutes < 60 -> "$diffMinutes dk önce"
        diffHours < 24 -> "$diffHours saat önce"
        diffDays == 1L -> "Dün"
        diffDays < 7 -> "$diffDays gün önce"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale("tr"))
            sdf.format(Date(timestamp))
        }
    }
}
