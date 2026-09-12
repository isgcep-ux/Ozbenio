package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.CastState
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceBrand
import com.example.data.model.RecentTvDevice
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastGlowBlue
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.theme.CastRoseDanger
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastDeviceDialog(
    castState: CastState,
    devices: List<CastDevice>,
    recentDevices: List<RecentTvDevice> = emptyList(),
    onSelectDevice: (CastDevice) -> Unit,
    onDisconnect: () -> Unit,
    onRescan: () -> Unit,
    onAddManualDevice: (name: String, ip: String, port: Int, protocol: CastProtocol) -> Unit = { _, _, _, _ -> },
    onDismiss: () -> Unit
) {
    val isScanning = castState.status == ConnectionStatus.SCANNING
    val isConnecting = castState.status == ConnectionStatus.CONNECTING

    var selectedProtocolFilter by remember { mutableStateOf("Tümü") }
    var deviceSearchQuery by remember { mutableStateOf("") }
    var showManualAddSection by remember { mutableStateOf(false) }

    // Manual TV form state
    var manualName by remember { mutableStateOf("") }
    var manualIp by remember { mutableStateOf("192.168.1.") }
    var manualPort by remember { mutableStateOf("8008") }
    var manualProtocol by remember { mutableStateOf(CastProtocol.CHROMECAST) }
    var protocolDropdownExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val filterOptions = listOf(
        "Tümü",
        "Chromecast",
        "AirPlay",
        "Samsung",
        "LG webOS",
        "DLNA / UPnP"
    )

    val filteredDevices = remember(devices, selectedProtocolFilter, deviceSearchQuery) {
        val query = deviceSearchQuery.trim().lowercase()
        devices.filter { device ->
            val matchesProtocol = when (selectedProtocolFilter) {
                "Tümü" -> true
                "Chromecast" -> device.protocol == CastProtocol.CHROMECAST || device.brand == DeviceBrand.CHROMECAST
                "AirPlay" -> device.protocol == CastProtocol.AIRPLAY || device.brand == DeviceBrand.APPLE_TV
                "Samsung" -> device.protocol == CastProtocol.SAMSUNG_SMART_VIEW || device.brand == DeviceBrand.SAMSUNG
                "LG webOS" -> device.protocol == CastProtocol.LG_WEBOS || device.brand == DeviceBrand.LG
                "DLNA / UPnP" -> device.protocol == CastProtocol.DLNA_UPNP || device.protocol == CastProtocol.ROKU_DIAL
                else -> true
            }

            val matchesSearch = query.isEmpty() ||
                device.name.lowercase().contains(query) ||
                device.room.lowercase().contains(query) ||
                device.brand.displayName.lowercase().contains(query) ||
                device.model.lowercase().contains(query) ||
                device.ipAddress.lowercase().contains(query)

            matchesProtocol && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("cast_device_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0D1322)
            ),
            border = BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(CastCyanAccent.copy(alpha = 0.6f), CastIndigoPrimary.copy(alpha = 0.3f))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CastGlowBlue)
                                .scale(if (isScanning || isConnecting) pulseScale else 1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (castState.connectedDevice != null) Icons.Default.CastConnected else Icons.Default.Cast,
                                contentDescription = "Cast",
                                tint = CastCyanAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Smart TV Keşfi & Yayınla",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Text(
                                text = if (isScanning) "NsdManager ile mDNS servisleri taranıyor..."
                                else if (isConnecting) "TV ile bağlantı kuruluyor..."
                                else "${devices.size} Smart TV ağı üzerinde hazır",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isScanning) CastCyanAccent else Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_cast_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Network & NsdManager Live Scanner Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, if (isScanning) CastCyanAccent.copy(alpha = 0.5f) else Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isScanning) CastCyanAccent else CastEmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Router,
                                        contentDescription = null,
                                        tint = CastCyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Wi-Fi: ${castState.wifiSsid}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "NsdManager (mDNS / DNS-SD Service Discovery)",
                                    color = CastCyanAccent.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = onRescan,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isScanning) Color(0xFF334155) else CastCyanAccent
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("rescan_devices_button")
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Taranıyor", color = Color.White, fontSize = 11.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Yeniden Tara",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ağı Tara", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Connected Device Banner (if any)
                if (castState.connectedDevice != null) {
                    val activeTv = castState.connectedDevice
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CastEmeraldSuccess.copy(alpha = 0.14f))
                            .border(1.5.dp, CastEmeraldSuccess.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CastEmeraldSuccess),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = activeTv.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "${activeTv.room} • ${activeTv.ipAddress}:${activeTv.port}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CastEmeraldSuccess,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onDisconnect,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CastRoseDanger
                                ),
                                border = BorderStroke(1.dp, CastRoseDanger.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("disconnect_tv_button")
                            ) {
                                Text("Bağlantıyı Kes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { filter ->
                        val isSelected = selectedProtocolFilter == filter
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CastCyanAccent else Color(0xFF1E293B),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CastCyanAccent else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .clickable { selectedProtocolFilter = filter }
                                .testTag("filter_chip_$filter")
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TV Search Bar at the top of Discovery List
                OutlinedTextField(
                    value = deviceSearchQuery,
                    onValueChange = { deviceSearchQuery = it },
                    placeholder = {
                        Text(
                            text = "TV adı, oda, marka veya IP ara...",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = if (deviceSearchQuery.isNotBlank()) CastCyanAccent else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (deviceSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { deviceSearchQuery = "" },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("clear_tv_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Aramayı Temizle",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CastCyanAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF131D33),
                        unfocusedContainerColor = Color(0xFF131D33)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("tv_discovery_search_input")
                )

                // Recent Devices Quick 1-Tap Strip inside Dialog
                if (recentDevices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = CastCyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Son Bağlanılan TV'ler (1-Dokunuş)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "${recentDevices.take(5).size} TV",
                            color = CastCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentDevices.take(5), key = { "dialog_recent_${it.device.id}" }) { rDev ->
                            val isCurrent = castState.connectedDevice?.id == rDev.device.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) CastEmeraldSuccess.copy(alpha = 0.15f) else Color(0xFF1E293B),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) CastEmeraldSuccess else CastCyanAccent.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .clickable(enabled = !isConnecting) {
                                        onSelectDevice(rDev.device)
                                    }
                                    .testTag("dialog_recent_tv_chip_${rDev.device.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrent) Icons.Default.CastConnected else Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (isCurrent) CastEmeraldSuccess else CastCyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Column {
                                        Text(
                                            text = rDev.device.name,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${rDev.device.room} • ${rDev.device.brand.displayName}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Available Devices List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keşfedilen Smart TV'ler (${filteredDevices.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Text(
                        text = if (showManualAddSection) "Gizle" else "+ Manuel IP Ekle",
                        color = CastCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { showManualAddSection = !showManualAddSection }
                            .padding(4.dp)
                            .testTag("toggle_manual_ip_button")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Expandable Manual IP section
                AnimatedVisibility(
                    visible = showManualAddSection,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Özel Smart TV IP ve Port Girin",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualName,
                                    onValueChange = { manualName = it },
                                    placeholder = { Text("TV Adı (örn. Salon TV)", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CastCyanAccent,
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    ),
                                    modifier = Modifier.weight(1f).testTag("manual_device_name_input")
                                )

                                OutlinedTextField(
                                    value = manualIp,
                                    onValueChange = { manualIp = it },
                                    placeholder = { Text("IP Adresi", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CastCyanAccent,
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    ),
                                    modifier = Modifier.weight(1f).testTag("manual_device_ip_input")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualPort,
                                    onValueChange = { manualPort = it },
                                    placeholder = { Text("Port (8008)", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CastCyanAccent,
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    ),
                                    modifier = Modifier.weight(0.4f).testTag("manual_device_port_input")
                                )

                                Button(
                                    onClick = {
                                        val portInt = manualPort.toIntOrNull() ?: 8008
                                        onAddManualDevice(
                                            manualName.ifBlank { "Özel Smart TV" },
                                            manualIp,
                                            portInt,
                                            manualProtocol
                                        )
                                        showManualAddSection = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CastCyanAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(0.6f).testTag("save_manual_device_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Listeye Ekle", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Discovered Devices List
                if (filteredDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (deviceSearchQuery.isNotBlank())
                                    "\"$deviceSearchQuery\" aramasına uygun TV bulunamadı"
                                else
                                    "Bu filtreye uygun Smart TV bulunamadı",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (deviceSearchQuery.isNotBlank())
                                    "Farklı bir TV adı veya oda aramayı deneyin"
                                else
                                    "NsdManager ağı taramaya devam ediyor...",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDevices, key = { it.id }) { device ->
                            val isCurrent = castState.connectedDevice?.id == device.id
                            DeviceListItem(
                                device = device,
                                isSelected = isCurrent,
                                isConnecting = isConnecting && isCurrent,
                                onClick = { onSelectDevice(device) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Hint
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RssFeed,
                        contentDescription = null,
                        tint = CastCyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NsdManager DNS-SD: Google Cast, AirPlay, DIAL ve DLNA servislerini otomatik keşfeder.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceListItem(
    device: CastDevice,
    isSelected: Boolean,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dialog_device_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dialog_pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dialog_pulse_alpha"
    )

    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dialog_border_glow"
    )

    val protocolLabel = when (device.protocol) {
        CastProtocol.CHROMECAST -> "Google Cast / Android TV"
        CastProtocol.SAMSUNG_SMART_VIEW -> "Samsung Smart View"
        CastProtocol.LG_WEBOS -> "LG webOS ThinQ"
        CastProtocol.DLNA_UPNP -> "DLNA / UPnP Media"
        CastProtocol.AIRPLAY -> "Apple AirPlay 2"
        CastProtocol.ROKU_DIAL -> "Roku Connect"
        CastProtocol.MIRACAST -> "Miracast Direct"
    }

    val brandColor = when (device.brand) {
        DeviceBrand.SAMSUNG -> Color(0xFF3B82F6)
        DeviceBrand.LG -> Color(0xFFEF4444)
        DeviceBrand.SONY -> Color(0xFF10B981)
        DeviceBrand.APPLE_TV -> Color(0xFFA855F7)
        DeviceBrand.CHROMECAST -> Color(0xFFF59E0B)
        DeviceBrand.ROKU -> Color(0xFF8B5CF6)
        DeviceBrand.FIRE_TV -> Color(0xFFF97316)
        else -> CastCyanAccent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isSelected -> CastIndigoPrimary.copy(alpha = 0.25f)
                    isConnecting -> Color(0xFF0F1E38)
                    else -> Color(0xFF1E293B).copy(alpha = 0.5f)
                }
            )
            .border(
                1.dp,
                when {
                    isSelected -> CastCyanAccent
                    isConnecting -> CastCyanAccent.copy(alpha = borderGlowAlpha)
                    else -> Color(0xFF334155)
                },
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("device_item_${device.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnecting) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .scale(pulseScale)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CastCyanAccent.copy(alpha = pulseAlpha * 0.5f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isConnecting) CastCyanAccent.copy(alpha = 0.25f)
                                else brandColor.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (isConnecting) CastCyanAccent.copy(alpha = borderGlowAlpha)
                                else brandColor.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = if (isConnecting) CastCyanAccent else brandColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        if (isConnecting) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CastCyanAccent.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, CastCyanAccent.copy(alpha = borderGlowAlpha))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(7.dp),
                                        strokeWidth = 1.5.dp,
                                        color = CastCyanAccent
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Bağlanıyor...",
                                        color = CastCyanAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (device.isNsdDiscovered) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CastCyanAccent.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, CastCyanAccent)
                            ) {
                                Text(
                                    text = "NSD Canlı",
                                    color = CastCyanAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "$protocolLabel • ${device.ipAddress}:${device.port}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${device.room} • ${device.resolutionSupport}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isConnecting) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF162544),
                    border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = borderGlowAlpha))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.8.dp,
                            color = CastCyanAccent
                        )
                        Text(
                            text = "Bağlanıyor",
                            color = CastCyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isSelected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (device.batteryLevel != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = if (device.batteryLevel > 20) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                                    contentDescription = "Pil Seviyesi",
                                    tint = if (device.batteryLevel > 50) CastEmeraldSuccess else if (device.batteryLevel > 20) CastCyanAccent else Color(0xFFEF4444),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "%${device.batteryLevel}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(CastEmeraldSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Bağlı",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Sinyal",
                        tint = CastEmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%${device.signalStrengthPercentage}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
