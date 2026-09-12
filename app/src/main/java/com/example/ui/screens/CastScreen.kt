package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.CastState
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceBrand
import com.example.ui.components.TvRemoteBottomSheet
import com.example.ui.theme.CastAmberWarning
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastGlowBlue
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.theme.CastRoseDanger
import com.example.ui.viewmodel.CastViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastScreen(
    viewModel: CastViewModel,
    onNavigateBack: () -> Unit
) {
    val castState by viewModel.castState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val showRemoteSheet by viewModel.showRemoteSheet.collectAsState()

    val isScanning = castState.status == ConnectionStatus.SCANNING
    val isConnecting = castState.status == ConnectionStatus.CONNECTING
    val isConnected = castState.status == ConnectionStatus.CONNECTED
    val isDisconnected = castState.status == ConnectionStatus.DISCONNECTED

    var searchQuery by remember { mutableStateOf("") }
    var selectedProtocolFilter by remember { mutableStateOf("All") }
    var showManualAddDialog by remember { mutableStateOf(false) }

    // Manual TV form state
    var manualName by remember { mutableStateOf("") }
    var manualIp by remember { mutableStateOf("192.168.1.") }
    var manualPort by remember { mutableStateOf("8008") }
    var manualProtocol by remember { mutableStateOf(CastProtocol.CHROMECAST) }
    var protocolDropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Filter discovered devices
    val filteredDevices = remember(discoveredDevices, searchQuery, selectedProtocolFilter) {
        discoveredDevices.filter { device ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                device.name.contains(searchQuery, ignoreCase = true) ||
                        device.room.contains(searchQuery, ignoreCase = true) ||
                        device.brand.displayName.contains(searchQuery, ignoreCase = true) ||
                        device.model.contains(searchQuery, ignoreCase = true) ||
                        device.ipAddress.contains(searchQuery, ignoreCase = true)
            }

            val matchesProtocol = when (selectedProtocolFilter) {
                "All", "Tümü" -> true
                "Google Cast", "Chromecast" -> device.protocol == CastProtocol.CHROMECAST || device.brand == DeviceBrand.CHROMECAST
                "Samsung" -> device.protocol == CastProtocol.SAMSUNG_SMART_VIEW || device.brand == DeviceBrand.SAMSUNG
                "LG webOS" -> device.protocol == CastProtocol.LG_WEBOS || device.brand == DeviceBrand.LG
                "AirPlay" -> device.protocol == CastProtocol.AIRPLAY || device.brand == DeviceBrand.APPLE_TV
                "Sony" -> device.brand == DeviceBrand.SONY
                "DLNA" -> device.protocol == CastProtocol.DLNA_UPNP
                "Roku / Fire TV" -> device.protocol == CastProtocol.ROKU_DIAL || device.brand == DeviceBrand.ROKU || device.brand == DeviceBrand.FIRE_TV
                else -> true
            }

            matchesQuery && matchesProtocol
        }
    }

    // Scan Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "CastScanWave")
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CastPulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CastPulseAlpha"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cast_screen"),
        containerColor = Color(0xFF0A0E1A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CastCyanAccent, CastIndigoPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Cast",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                                // Dynamic Connection Status Pill
                                val statusBgColor = when (castState.status) {
                                    ConnectionStatus.CONNECTED -> CastEmeraldSuccess.copy(alpha = 0.2f)
                                    ConnectionStatus.CONNECTING -> CastCyanAccent.copy(alpha = 0.2f)
                                    ConnectionStatus.SCANNING -> CastGlowBlue.copy(alpha = 0.2f)
                                    ConnectionStatus.DISCONNECTED -> Color(0xFF334155).copy(alpha = 0.5f)
                                }
                                val statusTextColor = when (castState.status) {
                                    ConnectionStatus.CONNECTED -> CastEmeraldSuccess
                                    ConnectionStatus.CONNECTING -> CastCyanAccent
                                    ConnectionStatus.SCANNING -> CastCyanAccent
                                    ConnectionStatus.DISCONNECTED -> Color(0xFF94A3B8)
                                }
                                val statusText = when (castState.status) {
                                    ConnectionStatus.CONNECTED -> "Connected"
                                    ConnectionStatus.CONNECTING -> "Connecting..."
                                    ConnectionStatus.SCANNING -> "Scanning..."
                                    ConnectionStatus.DISCONNECTED -> "Not Connected"
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusBgColor,
                                    border = BorderStroke(1.dp, statusTextColor.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Wi-Fi: ${castState.wifiSsid} • ${castState.tabletIp}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("cast_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Manual IP Add button
                    IconButton(
                        onClick = { showManualAddDialog = !showManualAddDialog },
                        modifier = Modifier.testTag("cast_screen_add_ip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add IP",
                            tint = CastCyanAccent
                        )
                    }

                    // Remote Control shortcut (if connected)
                    if (isConnected) {
                        IconButton(
                            onClick = { viewModel.openRemoteSheet() },
                            modifier = Modifier.testTag("cast_screen_remote_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsRemote,
                                contentDescription = "TV Remote",
                                tint = CastCyanAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth > 720.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isWideScreen) 28.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 36.dp)
            ) {
                // 1. Prominent Scan Control & Hero Connection Status Card
                item {
                    CastHeroControlCard(
                        castState = castState,
                        isScanning = isScanning,
                        discoveredCount = discoveredDevices.size,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha,
                        radarRotation = radarRotation,
                        onScanClicked = {
                            if (isScanning) viewModel.stopDeviceScan() else viewModel.startDeviceScan()
                        },
                        onDisconnectClicked = { viewModel.disconnectDevice() },
                        onOpenRemote = { viewModel.openRemoteSheet() },
                        onToggleMirroring = { viewModel.toggleScreenMirroring() }
                    )
                }

                // 2. Expandable Manual TV IP Addition Card
                item {
                    AnimatedVisibility(
                        visible = showManualAddDialog,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D33)),
                            border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .testTag("cast_manual_add_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = CastCyanAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Add Smart TV Manually (Direct IP)",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    IconButton(
                                        onClick = { showManualAddDialog = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Text(
                                    text = "Connect directly to Smart TVs on enterprise Wi-Fi or hidden subnets where mDNS broadcast is blocked.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                OutlinedTextField(
                                    value = manualName,
                                    onValueChange = { manualName = it },
                                    label = { Text("TV Name & Location (e.g. Master Bedroom TV)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CastCyanAccent,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cast_manual_name_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = manualIp,
                                        onValueChange = { manualIp = it },
                                        label = { Text("IP Address") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CastCyanAccent,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF0F172A),
                                            unfocusedContainerColor = Color(0xFF0F172A)
                                        ),
                                        modifier = Modifier
                                            .weight(0.68f)
                                            .testTag("cast_manual_ip_input")
                                    )

                                    OutlinedTextField(
                                        value = manualPort,
                                        onValueChange = { manualPort = it },
                                        label = { Text("Port") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CastCyanAccent,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF0F172A),
                                            unfocusedContainerColor = Color(0xFF0F172A)
                                        ),
                                        modifier = Modifier
                                            .weight(0.32f)
                                            .testTag("cast_manual_port_input")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Protocol Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = protocolDropdownExpanded,
                                    onExpandedChange = { protocolDropdownExpanded = !protocolDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = manualProtocol.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Casting Protocol") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = protocolDropdownExpanded) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CastCyanAccent,
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF0F172A),
                                            unfocusedContainerColor = Color(0xFF0F172A)
                                        ),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .testTag("cast_manual_protocol_dropdown")
                                    )

                                    ExposedDropdownMenu(
                                        expanded = protocolDropdownExpanded,
                                        onDismissRequest = { protocolDropdownExpanded = false }
                                    ) {
                                        CastProtocol.values().forEach { proto ->
                                            DropdownMenuItem(
                                                text = { Text(proto.displayName) },
                                                onClick = {
                                                    manualProtocol = proto
                                                    protocolDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val portInt = manualPort.toIntOrNull() ?: 8008
                                        viewModel.addManualDevice(manualName, manualIp, portInt, manualProtocol)
                                        showManualAddDialog = false
                                        manualName = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CastCyanAccent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("cast_manual_submit_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add Device to Cast List",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Search & Protocol Filter Chips Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by TV name, room, brand or IP...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = CastCyanAccent)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CastCyanAccent,
                                unfocusedBorderColor = Color(0xFF1F2937),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF111827),
                                unfocusedContainerColor = Color(0xFF111827)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cast_search_input")
                        )

                        // Protocol Filter Chips
                        val filterOptions = listOf(
                            "All",
                            "Google Cast",
                            "Samsung",
                            "LG webOS",
                            "AirPlay",
                            "Sony",
                            "DLNA",
                            "Roku / Fire TV"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filterOptions) { filter ->
                                val isSelected = selectedProtocolFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CastCyanAccent else Color(0xFF131D33),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CastCyanAccent else Color(0xFF1F2937)
                                    ),
                                    modifier = Modifier
                                        .clickable { selectedProtocolFilter = filter }
                                        .testTag("cast_filter_chip_$filter")
                                ) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Section Header: Discovered Devices
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Discovered Devices",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Surface(
                                shape = CircleShape,
                                color = CastIndigoPrimary.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${filteredDevices.size}",
                                    color = CastCyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Scan state indicator text
                        Text(
                            text = if (isScanning) "Searching local network..." else "Ready to Cast",
                            color = if (isScanning) CastCyanAccent else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 5. Discovered Devices List
                if (filteredDevices.isEmpty()) {
                    item {
                        CastEmptyDevicesCard(
                            isScanning = isScanning,
                            searchQuery = searchQuery,
                            onStartScan = { viewModel.startDeviceScan() }
                        )
                    }
                } else {
                    items(filteredDevices, key = { it.id }) { device ->
                        val isThisDeviceConnected = isConnected && castState.connectedDevice?.id == device.id
                        val isThisDeviceConnecting = isConnecting && castState.connectedDevice?.id == device.id

                        CastDeviceCard(
                            device = device,
                            isConnected = isThisDeviceConnected,
                            isConnecting = isThisDeviceConnecting,
                            onConnect = { viewModel.connectToDevice(device) },
                            onDisconnect = { viewModel.disconnectDevice() },
                            onOpenRemote = { viewModel.openRemoteSheet() }
                        )
                    }
                }

                // 6. Network & Casting Diagnostics Card
                item {
                    CastDiagnosticsCard(
                        wifiSsid = castState.wifiSsid,
                        tabletIp = castState.tabletIp,
                        status = castState.status,
                        onScanAgain = { viewModel.startDeviceScan() }
                    )
                }
            }
        }

        // TV Remote Control Sheet (when opened from Cast Screen)
        if (showRemoteSheet) {
            TvRemoteBottomSheet(
                castState = castState,
                onSendCommand = { viewModel.sendRemoteCommand(it) },
                onSetVolume = { viewModel.setVolume(it) },
                onSetQuality = { viewModel.setQuality(it) },
                onSetSubtitle = { viewModel.setSubtitle(it) },
                onToggleMirroring = { viewModel.toggleScreenMirroring() },
                onDismiss = { viewModel.closeRemoteSheet() }
            )
        }
    }
}

@Composable
private fun CastHeroControlCard(
    castState: CastState,
    isScanning: Boolean,
    discoveredCount: Int,
    pulseScale: Float,
    pulseAlpha: Float,
    radarRotation: Float,
    onScanClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
    onOpenRemote: () -> Unit,
    onToggleMirroring: () -> Unit
) {
    val isConnected = castState.status == ConnectionStatus.CONNECTED
    val isConnecting = castState.status == ConnectionStatus.CONNECTING
    val connectedDevice = castState.connectedDevice

    val borderBrush = when {
        isConnected -> Brush.linearGradient(listOf(CastEmeraldSuccess, Color(0xFF0D9488)))
        isConnecting -> Brush.linearGradient(listOf(CastCyanAccent, CastGlowBlue))
        isScanning -> Brush.linearGradient(listOf(CastCyanAccent, CastIndigoPrimary))
        else -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
    }

    val cardBg = when {
        isConnected -> Color(0xFF09201A)
        isConnecting -> Color(0xFF0E1A33)
        else -> Color(0xFF10192E)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.5.dp, borderBrush),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cast_hero_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Radar or Device Icon
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning || isConnecting) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(CastCyanAccent.copy(alpha = pulseAlpha * 0.35f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            if (isConnected) CastEmeraldSuccess.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    if (isConnected) CastEmeraldSuccess else CastCyanAccent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isConnected -> Icons.Default.CastConnected
                                    isConnecting -> Icons.Default.Cast
                                    isScanning -> Icons.Default.RssFeed
                                    else -> Icons.Default.Tv
                                },
                                contentDescription = null,
                                tint = if (isConnected) CastEmeraldSuccess else CastCyanAccent,
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(if (isScanning) Modifier.rotate(radarRotation) else Modifier)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = when {
                                isConnected -> "Connected to ${connectedDevice?.name ?: "Smart TV"}"
                                isConnecting -> "Connecting to ${connectedDevice?.name ?: "Device"}..."
                                isScanning -> "Scanning Local Network..."
                                else -> "Ready to Cast"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = when {
                                isConnected -> "${connectedDevice?.room ?: "Living Room"} • ${connectedDevice?.protocol?.displayName ?: "Google Cast"}"
                                isConnecting -> "Negotiating TLS & Video Session handshake..."
                                isScanning -> "Discovering Chromecast, AirPlay, DLNA & Smart TVs..."
                                else -> "Select a Smart TV from the list below or scan"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isConnected) CastEmeraldSuccess else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Primary Scan / Connection Button
                Button(
                    onClick = onScanClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) Color(0xFF1E293B) else CastCyanAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    border = BorderStroke(1.dp, CastCyanAccent),
                    modifier = Modifier.testTag("cast_scan_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CastCyanAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scanning",
                            color = CastCyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan",
                            color = Color(0xFF0F172A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // If Connected: Active Connection Controls Sub-panel
            if (isConnected && connectedDevice != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F2D24),
                    border = BorderStroke(1.dp, CastEmeraldSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Live Stream Spec Chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CastEmeraldSuccess.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "● LIVE",
                                        color = CastEmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = castState.quality.shortLabel,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = "${connectedDevice.ipAddress}:${connectedDevice.port}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Volume & Mute Status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (castState.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (castState.isMuted) CastRoseDanger else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (castState.isMuted) "MUTED" else "${castState.volumePercent}%",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Remote Control, Screen Mirroring, Disconnect
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // TV Remote Button
                            Button(
                                onClick = onOpenRemote,
                                colors = ButtonDefaults.buttonColors(containerColor = CastCyanAccent),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cast_hero_remote_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SettingsRemote,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TV Remote",
                                    color = Color(0xFF0F172A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Screen Mirroring Toggle
                            Button(
                                onClick = onToggleMirroring,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (castState.isScreenMirroring) CastAmberWarning else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (castState.isScreenMirroring) CastAmberWarning else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cast_hero_mirroring_button")
                            ) {
                                Icon(
                                    imageVector = if (castState.isScreenMirroring) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                                    contentDescription = null,
                                    tint = if (castState.isScreenMirroring) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (castState.isScreenMirroring) "Stop Mirror" else "Mirror Screen",
                                    color = if (castState.isScreenMirroring) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Disconnect Button
                            OutlinedButton(
                                onClick = onDisconnectClicked,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CastRoseDanger.copy(alpha = 0.8f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("cast_hero_disconnect_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LinkOff,
                                    contentDescription = null,
                                    tint = CastRoseDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Disconnect",
                                    color = CastRoseDanger,
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
}

@Composable
private fun CastDeviceCard(
    device: CastDevice,
    isConnected: Boolean,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenRemote: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "device_card_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_glow"
    )

    val borderColor = when {
        isConnected -> CastEmeraldSuccess
        isConnecting -> CastCyanAccent.copy(alpha = borderGlowAlpha)
        else -> Color(0xFF1E293B)
    }

    val cardBg = when {
        isConnected -> Color(0xFF0F261F)
        isConnecting -> Color(0xFF0D1C38)
        else -> Color(0xFF111827)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(if (isConnected || isConnecting) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isConnected) onOpenRemote() else if (!isConnecting) onConnect()
            }
            .testTag("cast_device_item_${device.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isConnecting) {
                        Brush.verticalGradient(
                            listOf(
                                CastCyanAccent.copy(alpha = 0.12f * borderGlowAlpha),
                                Color(0xFF10192E)
                            )
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Device Brand Icon & Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Device Icon with Pulsing Radar Ripple during connection
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isConnecting) {
                            // Pulsing radar ripple wave
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(pulseScale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CastCyanAccent.copy(alpha = pulseAlpha * 0.5f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isConnected -> CastEmeraldSuccess.copy(alpha = 0.2f)
                                        isConnecting -> CastCyanAccent.copy(alpha = 0.25f)
                                        else -> Color(0xFF1E293B)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isConnected -> CastEmeraldSuccess.copy(alpha = 0.5f)
                                        isConnecting -> CastCyanAccent.copy(alpha = borderGlowAlpha)
                                        else -> Color(0xFF334155)
                                    },
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (device.brand) {
                                    DeviceBrand.CHROMECAST -> Icons.Default.Cast
                                    DeviceBrand.APPLE_TV -> Icons.Default.Tv
                                    DeviceBrand.ROKU, DeviceBrand.FIRE_TV -> Icons.Default.Devices
                                    else -> Icons.Default.Tv
                                },
                                contentDescription = null,
                                tint = when {
                                    isConnected -> CastEmeraldSuccess
                                    isConnecting -> CastCyanAccent
                                    else -> Color(0xFF94A3B8)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Device Name, Room, Specs
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Status Badge
                            if (isConnected) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CastEmeraldSuccess.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, CastEmeraldSuccess)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = CastEmeraldSuccess,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Connected",
                                            color = CastEmeraldSuccess,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (isConnecting) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CastCyanAccent.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = borderGlowAlpha))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(9.dp),
                                            strokeWidth = 1.8.dp,
                                            color = CastCyanAccent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Connecting...",
                                            color = CastCyanAccent,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${device.room} • ${device.brand.displayName} ${device.model}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // IP & Protocol tags
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = device.protocol.displayName,
                                    color = CastCyanAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }

                            Text(
                                text = "${device.ipAddress}:${device.port}",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )

                            Text(
                                text = "• ${device.signalStrengthPercentage}% Wi-Fi",
                                color = if (device.signalStrengthPercentage > 80) CastEmeraldSuccess else CastAmberWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right Action Button
                if (isConnected) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onOpenRemote,
                            colors = ButtonDefaults.buttonColors(containerColor = CastCyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("device_remote_button_${device.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsRemote,
                                contentDescription = "Remote",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onDisconnect,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CastRoseDanger.copy(alpha = 0.7f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("device_disconnect_button_${device.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Disconnect",
                                tint = CastRoseDanger,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else if (isConnecting) {
                    // Prominent Circular Progress & Pulsing Status Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF162544),
                        border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = borderGlowAlpha)),
                        modifier = Modifier.testTag("device_connecting_indicator_${device.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = CastCyanAccent
                            )
                            Text(
                                text = "Pairing...",
                                color = CastCyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("connect_device_button_${device.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = null,
                            tint = CastCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cast",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Connection handshake progress hint when this item is actively connecting
            AnimatedVisibility(
                visible = isConnecting,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = BorderStroke(0.5.dp, CastCyanAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(CastCyanAccent)
                                )
                                Text(
                                    text = "Authenticating TLS session & video transport socket...",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = CastCyanAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CastEmptyDevicesCard(
    isScanning: Boolean,
    searchQuery: String,
    onStartScan: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = BorderStroke(1.dp, Color(0xFF1F2937)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("cast_empty_devices_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = CastCyanAccent,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (searchQuery.isNotBlank())
                    "No devices match \"$searchQuery\""
                else if (isScanning)
                    "Searching local Wi-Fi for Smart TVs..."
                else
                    "No Cast Devices Found",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isScanning)
                    "Broadcasting mDNS & SSDP probes across the 5GHz network..."
                else
                    "Ensure your Smart TV, Chromecast, or Apple TV is powered on and connected to the same Wi-Fi network.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartScan,
                colors = ButtonDefaults.buttonColors(containerColor = CastCyanAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("cast_empty_rescan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isScanning) "Scan in Progress..." else "Scan for Devices",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CastDiagnosticsCard(
    wifiSsid: String,
    tabletIp: String,
    status: ConnectionStatus,
    onScanAgain: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1626)),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cast_diagnostics_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CastCyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Cast Connection & Network Tips",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val tips = listOf(
                "Same Wi-Fi Network: Both your tablet ($tabletIp) and Smart TV must be on '$wifiSsid'.",
                "Router AP Isolation: Disable 'AP Isolation' or 'Client Isolation' on your Wi-Fi router settings.",
                "Supported Protocols: Google Cast, DLNA / UPnP, AirPlay 2, Samsung SmartView, LG webOS Connect.",
                "Direct IP: If your router blocks multicast, use the '+ Add IP' button at the top to connect directly."
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        color = CastCyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = tip,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
