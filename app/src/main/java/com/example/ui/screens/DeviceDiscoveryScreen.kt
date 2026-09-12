package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CastDevice
import com.example.data.model.CastProtocol
import com.example.data.model.CastState
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceBrand
import com.example.data.model.RecentTvDevice
import com.example.ui.components.TvRemoteBottomSheet
import com.example.ui.theme.CastAmberWarning
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastGlowBlue
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.theme.CastRoseDanger
import com.example.ui.viewmodel.CastViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiscoveryScreen(
    viewModel: CastViewModel,
    onNavigateBack: () -> Unit
) {
    val castState by viewModel.castState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val recentDevices by viewModel.recentDevices.collectAsState()
    val showRemoteSheet by viewModel.showRemoteSheet.collectAsState()

    val isScanning = castState.status == ConnectionStatus.SCANNING
    val isConnecting = castState.status == ConnectionStatus.CONNECTING
    val isConnected = castState.status == ConnectionStatus.CONNECTED

    var searchQuery by remember { mutableStateOf("") }
    var selectedProtocolFilter by remember { mutableStateOf("Tümü") }
    var showManualAddCard by remember { mutableStateOf(false) }

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

    // Filter discovered devices based on search query and protocol filter
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
                "Tümü" -> true
                "Chromecast" -> device.protocol == CastProtocol.CHROMECAST || device.brand == DeviceBrand.CHROMECAST
                "Samsung" -> device.protocol == CastProtocol.SAMSUNG_SMART_VIEW || device.brand == DeviceBrand.SAMSUNG
                "LG webOS" -> device.protocol == CastProtocol.LG_WEBOS || device.brand == DeviceBrand.LG
                "AirPlay" -> device.protocol == CastProtocol.AIRPLAY || device.brand == DeviceBrand.APPLE_TV
                "Sony" -> device.brand == DeviceBrand.SONY
                "DLNA" -> device.protocol == CastProtocol.DLNA_UPNP
                "Roku" -> device.protocol == CastProtocol.ROKU_DIAL || device.brand == DeviceBrand.ROKU
                else -> true
            }

            matchesQuery && matchesProtocol
        }
    }

    // Animation for radar scan waves
    val infiniteTransition = rememberInfiniteTransition(label = "RadarWave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("device_discovery_screen"),
        containerColor = Color(0xFF0A0E1A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Smart TV Keşfi",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isScanning) CastCyanAccent.copy(alpha = 0.2f) else CastEmeraldSuccess.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (isScanning) CastCyanAccent else CastEmeraldSuccess)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = if (isScanning) CastCyanAccent else CastEmeraldSuccess,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isScanning) "Taranıyor..." else "WiFi 5GHz",
                                        color = if (isScanning) CastCyanAccent else CastEmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Ağdaki TV'leri tara ve kablosuz bağlan",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("discovery_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Rescan Button
                    Button(
                        onClick = { viewModel.startDeviceScan() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScanning) CastCyanAccent.copy(alpha = 0.2f) else Color(0xFF1E293B)
                        ),
                        border = BorderStroke(1.dp, if (isScanning) CastCyanAccent else Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("discovery_rescan_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = CastCyanAccent
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = CastCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isScanning) "Taranıyor" else "Yeniden Tara",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
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
            val isWideScreen = maxWidth > 700.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isWideScreen) 24.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
            ) {
                // 1. Radar Scanning Banner / Network Status Card
                item {
                    DiscoveryRadarHeaderCard(
                        isScanning = isScanning,
                        discoveredCount = discoveredDevices.size,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha,
                        onTriggerScan = { viewModel.startDeviceScan() },
                        onToggleManualAdd = { showManualAddCard = !showManualAddCard },
                        showManualAdd = showManualAddCard
                    )
                }

                // 2. Active Connection Card (if currently connected to a TV)
                if (isConnected && castState.connectedDevice != null) {
                    item {
                        ActiveConnectedTvCard(
                            device = castState.connectedDevice!!,
                            castState = castState,
                            onDisconnect = { viewModel.disconnectDevice() },
                            onOpenRemote = { viewModel.openRemoteSheet() }
                        )
                    }
                }

                // 3. Expandable Manual TV Addition Form
                item {
                    AnimatedVisibility(
                        visible = showManualAddCard,
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
                                .testTag("manual_device_card")
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
                                            text = "Manuel IP ile Smart TV Ekle",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    IconButton(
                                        onClick = { showManualAddCard = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Kapat",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Text(
                                    text = "Gizli ağlarda veya farklı alt ağlarda (subnet) bulunan Smart TV'nize doğrudan IP ile bağlanın.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                OutlinedTextField(
                                    value = manualName,
                                    onValueChange = { manualName = it },
                                    label = { Text("TV Adı & Konum (örn: Salon TV)") },
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
                                        .testTag("manual_tv_name_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = manualIp,
                                        onValueChange = { manualIp = it },
                                        label = { Text("IP Adresi") },
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
                                            .testTag("manual_tv_ip_input")
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
                                            .testTag("manual_tv_port_input")
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
                                        label = { Text("Yayın Protokolü") },
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
                                            .testTag("manual_protocol_dropdown")
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
                                        showManualAddCard = false
                                        manualName = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CastCyanAccent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("manual_add_submit_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Smart TV Ekle ve Listeye Dahil Et",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Recent Devices Row (if present)
                if (recentDevices.isNotEmpty()) {
                    item {
                        DiscoveryRecentTvRow(
                            recentDevices = recentDevices,
                            connectedDeviceId = castState.connectedDevice?.id,
                            onConnect = { viewModel.connectToDevice(it) },
                            onRemove = { viewModel.removeRecentDevice(it) }
                        )
                    }
                }

                // 5. Search and Protocol Filter Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("TV adı, oda, marka veya IP adresi ara...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = CastCyanAccent)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = Color.White)
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
                                .testTag("device_discovery_search_input")
                        )

                        // Protocol Filter Chips
                        val filterOptions = listOf(
                            "Tümü",
                            "Chromecast",
                            "Samsung",
                            "LG webOS",
                            "AirPlay",
                            "Sony",
                            "DLNA",
                            "Roku"
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
                                        .testTag("filter_chip_$filter")
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

                // 6. Discovered Smart TVs Header & List
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
                                text = "Bulunan Smart TV'ler",
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

                        Text(
                            text = if (isScanning) "NsdManager aktif taranıyor..." else "Otomatik Keşif Aktif",
                            color = if (isScanning) CastCyanAccent else Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // Discovered Devices Items
                if (filteredDevices.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                            border = BorderStroke(1.dp, Color(0xFF1F2937))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank())
                                        "\"$searchQuery\" ile eşleşen TV bulunamadı"
                                    else
                                        "Ağda henüz TV bulunamadı",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "TV ve telefonun aynı WiFi ağına bağlı olduğundan emin olun veya manuel ekleyin.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.startDeviceScan() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, CastCyanAccent),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CastCyanAccent)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Yeniden Tara", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredDevices, key = { it.id }) { device ->
                        val isCurrent = castState.connectedDevice?.id == device.id
                        val isConnectingThis = isConnecting && isCurrent

                        DiscoveryDeviceItemCard(
                            device = device,
                            isSelected = isCurrent && isConnected,
                            isConnecting = isConnectingThis,
                            onConnect = { viewModel.connectToDevice(device) },
                            onDisconnect = { viewModel.disconnectDevice() },
                            onOpenRemote = { viewModel.openRemoteSheet() }
                        )
                    }
                }

                // 7. Troubleshooting / Diagnostic Tips Card
                item {
                    DiscoveryTroubleshootingCard(
                        onOpenManualAdd = { showManualAddCard = true }
                    )
                }
            }
        }

        // TV Remote Control Sheet (when opened from Discovery Screen)
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
private fun DiscoveryRadarHeaderCard(
    isScanning: Boolean,
    discoveredCount: Int,
    pulseScale: Float,
    pulseAlpha: Float,
    onTriggerScan: () -> Unit,
    onToggleManualAdd: () -> Unit,
    showManualAdd: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10192E)),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(CastCyanAccent.copy(alpha = 0.5f), CastIndigoPrimary))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discovery_radar_card")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background subtle gradient glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CastCyanAccent.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Radar pulse circle
                        Box(
                            modifier = Modifier.size(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScanning) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(CastCyanAccent.copy(alpha = pulseAlpha * 0.4f))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                        )
                                    )
                                    .border(1.5.dp, CastCyanAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isScanning) Icons.Default.RssFeed else Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = CastCyanAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isScanning) "Smart TV'ler Taranıyor..." else "WiFi Ağı Hazır",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "NsdManager (mDNS/DNS-SD) & UPnP",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CastCyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Bulunan TV Sayısı: $discoveredCount adet",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Action buttons (Manual add / Rescan)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onToggleManualAdd,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showManualAdd) CastCyanAccent else Color(0xFF1E293B)
                            ),
                            border = BorderStroke(1.dp, CastCyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("toggle_manual_add_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (showManualAdd) Color(0xFF0F172A) else CastCyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Manuel Ekle",
                                color = if (showManualAdd) Color(0xFF0F172A) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveConnectedTvCard(
    device: CastDevice,
    castState: CastState,
    onDisconnect: () -> Unit,
    onOpenRemote: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A291E)),
        border = BorderStroke(1.5.dp, CastEmeraldSuccess),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_connected_tv_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CastEmeraldSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CastConnected,
                            contentDescription = null,
                            tint = Color(0xFF0A291E),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CastEmeraldSuccess.copy(alpha = 0.25f),
                                border = BorderStroke(0.5.dp, CastEmeraldSuccess)
                            ) {
                                Text(
                                    text = "BAĞLI",
                                    color = CastEmeraldSuccess,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }

                            if (device.batteryLevel != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(0.5.dp, Color(0xFF334155))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (device.batteryLevel > 20) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                                            contentDescription = "Pil Durumu",
                                            tint = if (device.batteryLevel > 50) CastEmeraldSuccess else if (device.batteryLevel > 20) CastCyanAccent else CastRoseDanger,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "%${device.batteryLevel}",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "${device.brand.displayName} • ${device.ipAddress} • ${device.protocol.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Disconnect action
                OutlinedButton(
                    onClick = onDisconnect,
                    border = BorderStroke(1.dp, CastRoseDanger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CastRoseDanger),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("active_disconnect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Bağlantıyı Kes",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Bağlantıyı Kes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick TV Remote trigger button
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onOpenRemote,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("active_open_remote_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsRemote,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Smart TV Kumandasını Aç",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun DiscoveryRecentTvRow(
    recentDevices: List<RecentTvDevice>,
    connectedDeviceId: String?,
    onConnect: (CastDevice) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discovery_recent_tv_row")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = CastCyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Son Bağlanılan TV'ler",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Text(
                text = "Tek Dokunuşla Bağlan",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recentDevices, key = { it.device.id }) { item ->
                val isCurrent = connectedDeviceId == item.device.id

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCurrent) CastEmeraldSuccess.copy(alpha = 0.15f) else Color(0xFF131D33),
                    border = BorderStroke(
                        1.dp,
                        if (isCurrent) CastEmeraldSuccess else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .clickable { onConnect(item.device) }
                        .testTag("recent_tv_chip_${item.device.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) CastEmeraldSuccess else CastCyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCurrent) Icons.Default.CastConnected else Icons.Default.Tv,
                                contentDescription = null,
                                tint = if (isCurrent) Color(0xFF0F172A) else CastCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = item.device.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (isCurrent) "Şu an Bağlı" else "${item.device.room} • ${item.device.brand.displayName}",
                                color = if (isCurrent) CastEmeraldSuccess else Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryDeviceItemCard(
    device: CastDevice,
    isSelected: Boolean,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenRemote: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "discovery_item_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "item_pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "item_pulse_alpha"
    )

    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "item_border_glow"
    )

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

    val cardBg = when {
        isSelected -> Color(0xFF0F291E)
        isConnecting -> Color(0xFF0F1E38)
        else -> Color(0xFF111827)
    }
    val cardBorder = when {
        isSelected -> CastEmeraldSuccess
        isConnecting -> CastCyanAccent.copy(alpha = borderGlowAlpha)
        else -> Color(0xFF1F2937)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(if (isSelected || isConnecting) 1.5.dp else 1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!isSelected && !isConnecting) {
                    onConnect()
                }
            }
            .testTag("device_card_${device.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isConnecting) {
                        Brush.verticalGradient(
                            listOf(CastCyanAccent.copy(alpha = 0.12f * borderGlowAlpha), Color(0xFF0F172A))
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Brand Icon Box with Pulsing Ripple during connection
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isConnecting) {
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
                                    if (isConnecting) CastCyanAccent.copy(alpha = 0.25f)
                                    else brandColor.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (isConnecting) CastCyanAccent.copy(alpha = borderGlowAlpha)
                                    else brandColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = if (isConnecting) CastCyanAccent else brandColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isConnecting) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CastCyanAccent.copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, CastCyanAccent.copy(alpha = borderGlowAlpha))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(8.dp),
                                            strokeWidth = 1.5.dp,
                                            color = CastCyanAccent
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Bağlanıyor...",
                                            color = CastCyanAccent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (device.isNsdDiscovered) {
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
                            text = "${device.brand.displayName} • ${device.model}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Text(
                            text = "Konum: ${device.room} • ${device.ipAddress}:${device.port}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Connect / Status Action
                if (isConnecting) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF162544),
                        border = BorderStroke(1.dp, CastCyanAccent.copy(alpha = borderGlowAlpha)),
                        modifier = Modifier.testTag("device_connecting_badge_${device.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = CastCyanAccent
                            )
                            Text(
                                text = "Bağlanıyor...",
                                color = CastCyanAccent,
                                fontSize = 11.sp,
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
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(0.5.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (device.batteryLevel > 20) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                                        contentDescription = "Pil Seviyesi",
                                        tint = if (device.batteryLevel > 50) CastEmeraldSuccess else if (device.batteryLevel > 20) CastCyanAccent else CastRoseDanger,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "%${device.batteryLevel}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CastEmeraldSuccess,
                            modifier = Modifier.testTag("device_status_connected_${device.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Bağlı",
                                    color = Color(0xFF0F172A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        colors = ButtonDefaults.buttonColors(containerColor = CastCyanAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("connect_button_${device.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bağlan",
                            color = Color(0xFF0F172A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Badges & Signal Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Protocol Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(0.5.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = device.protocol.displayName,
                            color = CastCyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(0.5.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = device.resolutionSupport,
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // WiFi Signal Strength Meter & Battery Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (device.batteryLevel != null && isSelected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (device.batteryLevel > 20) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                                contentDescription = "Pil Durumu",
                                tint = if (device.batteryLevel > 50) CastEmeraldSuccess else if (device.batteryLevel > 20) CastCyanAccent else CastRoseDanger,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "%${device.batteryLevel}",
                                color = if (device.batteryLevel > 50) CastEmeraldSuccess else if (device.batteryLevel > 20) CastCyanAccent else CastRoseDanger,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = "Sinyal",
                            tint = if (device.signalStrengthPercentage >= 70) CastEmeraldSuccess else CastAmberWarning,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "%${device.signalStrengthPercentage} WiFi",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryTroubleshootingCard(
    onOpenManualAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, Color(0xFF1F2937)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discovery_troubleshooting_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    text = "Smart TV'nizi Göremiyor musunuz?",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1. Telefonunuz ve Smart TV'nizin aynı WiFi (2.4GHz / 5GHz) ağına bağlı olduğundan emin olun.\n" +
                        "2. TV ayarlarından Google Cast / Smart View / AirPlay / DLNA desteğinin açık olduğunu kontrol edin.\n" +
                        "3. TV görünmüyorsa aşağıdaki butona basarak doğrudan IP adresiyle manuel bağlantı kurabilirsiniz.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpenManualAdd,
                border = BorderStroke(1.dp, CastCyanAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CastCyanAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Doğrudan IP Adresi ile TV Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
