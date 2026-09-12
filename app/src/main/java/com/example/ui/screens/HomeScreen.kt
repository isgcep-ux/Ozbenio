package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.rounded.Router
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.CastDevice
import com.example.data.model.DeviceBrand
import com.example.data.model.ConnectionStatus
import com.example.data.model.Playlist
import com.example.ui.components.AddStreamUrlDialog
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CastDeviceDialog
import com.example.ui.components.CastingPlaybackOverlay
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.FeatureBadgesCard
import com.example.ui.components.PlaylistManagerDialog
import com.example.ui.components.RecentDevicesSection
import com.example.ui.components.TvCastLivePreview
import com.example.ui.components.TvRemoteBottomSheet
import com.example.ui.components.VideoPlayerCard
import com.example.ui.components.getPlaylistIcon
import com.example.ui.theme.CastCyanAccent
import com.example.ui.theme.CastEmeraldSuccess
import com.example.ui.theme.CastIndigoPrimary
import com.example.ui.viewmodel.CastViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: CastViewModel) {
    val castState by viewModel.castState.collectAsState()
    val mediaItems by viewModel.allMediaItems.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val historySessions by viewModel.historySessions.collectAsState()
    val recentDevices by viewModel.recentDevices.collectAsState()

    // Playlist States
    val detailedPlaylists by viewModel.detailedPlaylists.collectAsState()
    val showPlaylistsSheet by viewModel.showPlaylistsSheet.collectAsState()
    val selectedPlaylistForDetail by viewModel.selectedPlaylistForDetail.collectAsState()
    val mediaForAddToPlaylist by viewModel.mediaForAddToPlaylist.collectAsState()
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()

    val showCastDialog by viewModel.showCastDialog.collectAsState()
    val showRemoteSheet by viewModel.showRemoteSheet.collectAsState()
    val showAddUrlDialog by viewModel.showAddUrlDialog.collectAsState()
    val isTvPreviewFullscreen by viewModel.isTvPreviewFullscreen.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val categories = listOf("Tümü", "Doğa & Vahşi", "Belgesel", "Sinematik", "Ambiyans", "Gökyüzü", "Aksiyon", "Özel")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(CastCyanAccent, CastIndigoPrimary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Smart TV Cast",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "Tablet & Smart TV Yayınlayıcı",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CastCyanAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Playlists Button with Badge
                    IconButton(
                        onClick = { viewModel.openPlaylistsSheet() },
                        modifier = Modifier.testTag("appbar_playlists_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (detailedPlaylists.isNotEmpty()) {
                                    Badge(
                                        containerColor = CastCyanAccent,
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = detailedPlaylists.size.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistPlay,
                                contentDescription = "Çalma Listeleri",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // TV Discovery Screen Button
                    IconButton(
                        onClick = { viewModel.navigateToDeviceDiscovery() },
                        modifier = Modifier.testTag("appbar_discovery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Smart TV Keşfi",
                            tint = CastCyanAccent
                        )
                    }

                    // TV Remote Button
                    IconButton(
                        onClick = { viewModel.openRemoteSheet() },
                        modifier = Modifier.testTag("appbar_remote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsRemote,
                            contentDescription = "Kumanda",
                            tint = Color.White
                        )
                    }

                    // Add Custom Stream URL
                    IconButton(
                        onClick = { viewModel.openAddUrlDialog() },
                        modifier = Modifier.testTag("appbar_add_stream_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Akış Ekle",
                            tint = CastCyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Prominent Primary "📺 Cast" Button
                    val isConnected = castState.status == ConnectionStatus.CONNECTED
                    Button(
                        onClick = { viewModel.navigateToCast() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnected) CastEmeraldSuccess else CastCyanAccent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("appbar_cast_button")
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                            contentDescription = "Cast",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConnected) (castState.connectedDevice?.brand?.name ?: "TV'ye Bağlı") else "📺 Cast",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0F19)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF090D16)
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrLandscape = maxWidth >= 700.dp

            if (isTabletOrLandscape) {
                // Adaptive Tablet 2-Column Master-Detail Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Left Column (42% width) - Live TV Monitor & Cast Control Panel
                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                    ) {
                        TvCastLivePreview(
                            castState = castState,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSeek = { viewModel.seekToFraction(it) },
                            onSeekRelative = { viewModel.seekRelative(it) },
                            onOpenCastDialog = { viewModel.openCastDialog() },
                            onOpenRemote = { viewModel.openRemoteSheet() },
                            onToggleMirroring = { viewModel.toggleScreenMirroring() },
                            onToggleMute = { viewModel.toggleMute() },
                            onToggleFullscreen = { viewModel.toggleTvPreviewFullscreen() },
                            onSelectQuality = { viewModel.setQuality(it) },
                            onVolumeChange = { viewModel.setVolume(it) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        FeatureBadgesCard(castState = castState)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Recent Connected TVs Section (One-Tap Reconnect)
                        RecentDevicesSection(
                            recentDevices = recentDevices,
                            castState = castState,
                            onReconnectDevice = { viewModel.connectToDevice(it) },
                            onDisconnectDevice = { viewModel.disconnectDevice() },
                            onOpenRemote = { viewModel.openRemoteSheet() },
                            onRemoveRecent = { viewModel.removeRecentDevice(it) },
                            onClearAllRecent = { viewModel.clearRecentDevices() }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Smart TVs on Network (NsdManager Discovery Quick Bar)
                        DiscoveredDevicesQuickBar(
                            devices = discoveredDevices,
                            connectedDevice = castState.connectedDevice,
                            isScanning = castState.status == ConnectionStatus.SCANNING,
                            onSelectDevice = { viewModel.connectToDevice(it) },
                            onOpenCastDialog = { viewModel.openCastDialog() },
                            onOpenDiscoveryScreen = { viewModel.navigateToDeviceDiscovery() },
                            onRescan = { viewModel.startDeviceScan() }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Playlists Strip
                        PlaylistsQuickBar(
                            playlists = detailedPlaylists,
                            onOpenPlaylists = { viewModel.openPlaylistsSheet() },
                            onSelectPlaylist = { pl ->
                                viewModel.selectPlaylistForDetail(pl)
                                viewModel.openPlaylistsSheet()
                            },
                            onCreateNew = { viewModel.openCreatePlaylistDialog() },
                            onPlayPlaylist = { pl -> viewModel.playPlaylist(pl, 0) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Recent Sessions / TV History Section
                        if (historySessions.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                                border = BorderStroke(1.dp, Color(0xFF1F2937))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = CastCyanAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Son TV Yayınları",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.clearHistory() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Temizle",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyColumn(
                                        modifier = Modifier.height(95.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(historySessions) { session ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = session.mediaTitle,
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = session.deviceName,
                                                        color = CastCyanAccent,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Text(
                                                    text = session.durationFormatted,
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Column (58% width) - Search, Categories & Video Grid
                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    ) {
                        // Search and Add Stream Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Video veya kategori ara...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = CastCyanAccent)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
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
                                    .weight(1f)
                                    .testTag("search_input_field")
                            )

                            Button(
                                onClick = { viewModel.openAddUrlDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(54.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = CastCyanAccent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Akış Ekle", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CastCyanAccent else Color(0xFF111827),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CastCyanAccent else Color(0xFF1F2937)
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.setCategory(cat) }
                                        .testTag("category_chip_$cat")
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Media Grid (2 columns on tablet)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(mediaItems, key = { it.id }) { item ->
                                val isCurrent = castState.currentMedia?.id == item.id && castState.isPlaying
                                val isConnectedTv = castState.status == ConnectionStatus.CONNECTED
                                VideoPlayerCard(
                                    media = item,
                                    isCurrentlyPlaying = isCurrent,
                                    isConnectedToTv = isConnectedTv,
                                    onPlay = { viewModel.playMedia(it) },
                                    onAddToPlaylist = { viewModel.openAddToPlaylistDialog(it) },
                                    onDeleteCustom = { viewModel.deleteCustomStream(it) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Phone / Compact Vertical Layout
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        TvCastLivePreview(
                            castState = castState,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSeek = { viewModel.seekToFraction(it) },
                            onSeekRelative = { viewModel.seekRelative(it) },
                            onOpenCastDialog = { viewModel.openCastDialog() },
                            onOpenRemote = { viewModel.openRemoteSheet() },
                            onToggleMirroring = { viewModel.toggleScreenMirroring() },
                            onToggleMute = { viewModel.toggleMute() },
                            onToggleFullscreen = { viewModel.toggleTvPreviewFullscreen() },
                            onSelectQuality = { viewModel.setQuality(it) },
                            onVolumeChange = { viewModel.setVolume(it) }
                        )
                    }

                    item {
                        FeatureBadgesCard(castState = castState)
                    }

                    item {
                        // Recent Connected TVs Section (One-Tap Reconnect)
                        RecentDevicesSection(
                            recentDevices = recentDevices,
                            castState = castState,
                            onReconnectDevice = { viewModel.connectToDevice(it) },
                            onDisconnectDevice = { viewModel.disconnectDevice() },
                            onOpenRemote = { viewModel.openRemoteSheet() },
                            onRemoveRecent = { viewModel.removeRecentDevice(it) },
                            onClearAllRecent = { viewModel.clearRecentDevices() }
                        )
                    }

                    item {
                        // Smart TVs on Network (NsdManager Discovery Quick Bar)
                        DiscoveredDevicesQuickBar(
                            devices = discoveredDevices,
                            connectedDevice = castState.connectedDevice,
                            isScanning = castState.status == ConnectionStatus.SCANNING,
                            onSelectDevice = { viewModel.connectToDevice(it) },
                            onOpenCastDialog = { viewModel.openCastDialog() },
                            onOpenDiscoveryScreen = { viewModel.navigateToDeviceDiscovery() },
                            onRescan = { viewModel.startDeviceScan() }
                        )
                    }

                    item {
                        // Quick Playlists Strip
                        PlaylistsQuickBar(
                            playlists = detailedPlaylists,
                            onOpenPlaylists = { viewModel.openPlaylistsSheet() },
                            onSelectPlaylist = { pl ->
                                viewModel.selectPlaylistForDetail(pl)
                                viewModel.openPlaylistsSheet()
                            },
                            onCreateNew = { viewModel.openCreatePlaylistDialog() },
                            onPlayPlaylist = { pl -> viewModel.playPlaylist(pl, 0) }
                        )
                    }

                    item {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Video veya yayın ara...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = CastCyanAccent)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
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
                                .testTag("search_input_field_compact")
                        )
                    }

                    item {
                        // Category Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CastCyanAccent else Color(0xFF111827),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CastCyanAccent else Color(0xFF1F2937)
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.setCategory(cat) }
                                        .testTag("compact_category_chip_$cat")
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Video list items
                    items(mediaItems, key = { it.id }) { item ->
                        val isCurrent = castState.currentMedia?.id == item.id && castState.isPlaying
                        val isConnectedTv = castState.status == ConnectionStatus.CONNECTED
                        VideoPlayerCard(
                            media = item,
                            isCurrentlyPlaying = isCurrent,
                            isConnectedToTv = isConnectedTv,
                            onPlay = { viewModel.playMedia(it) },
                            onAddToPlaylist = { viewModel.openAddToPlaylistDialog(it) },
                            onDeleteCustom = { viewModel.deleteCustomStream(it) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Dialogs & Sheets
        if (showCastDialog) {
            CastDeviceDialog(
                castState = castState,
                devices = discoveredDevices,
                recentDevices = recentDevices,
                onSelectDevice = { viewModel.connectToDevice(it) },
                onDisconnect = { viewModel.disconnectDevice() },
                onRescan = { viewModel.startDeviceScan() },
                onAddManualDevice = { name, ip, port, protocol ->
                    viewModel.addManualDevice(name, ip, port, protocol)
                },
                onDismiss = { viewModel.closeCastDialog() }
            )
        }

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

        if (showAddUrlDialog) {
            AddStreamUrlDialog(
                onAddStream = { title, url, cat, res ->
                    viewModel.addCustomStream(title, url, cat, res)
                },
                onDismiss = { viewModel.closeAddUrlDialog() }
            )
        }

        // Playlists Sheet / Manager Dialog
        if (showPlaylistsSheet) {
            PlaylistManagerDialog(
                playlists = detailedPlaylists,
                selectedPlaylist = selectedPlaylistForDetail,
                castState = castState,
                onSelectPlaylist = { viewModel.selectPlaylistForDetail(it) },
                onCreateNewPlaylist = { viewModel.openCreatePlaylistDialog() },
                onDeletePlaylist = { viewModel.deletePlaylist(it) },
                onPlayPlaylist = { pl, startIdx ->
                    viewModel.playPlaylist(pl, startIdx)
                    viewModel.closePlaylistsSheet()
                },
                onPlayItemDirectly = { viewModel.playMedia(it) },
                onRemoveItem = { viewModel.removePlaylistItem(it) },
                onMoveItemUp = { plId, item -> viewModel.movePlaylistItemUp(plId, item) },
                onMoveItemDown = { plId, item -> viewModel.movePlaylistItemDown(plId, item) },
                onDismiss = { viewModel.closePlaylistsSheet() }
            )
        }

        // Add Video To Playlist Dialog
        if (mediaForAddToPlaylist != null) {
            AddToPlaylistDialog(
                media = mediaForAddToPlaylist!!,
                playlists = detailedPlaylists,
                onSelectPlaylist = { plId ->
                    viewModel.addMediaToPlaylist(plId, mediaForAddToPlaylist!!)
                },
                onCreateNewPlaylist = {
                    viewModel.openCreatePlaylistDialog()
                },
                onDismiss = { viewModel.closeAddToPlaylistDialog() }
            )
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onCreate = { name, desc, color, icon ->
                    viewModel.createPlaylist(name, desc, color, icon)
                },
                onDismiss = { viewModel.closeCreatePlaylistDialog() }
            )
        }

        // Fullscreen Theater Casting Screen with Reusable Playback Overlay
        if (isTvPreviewFullscreen) {
            Dialog(
                onDismissRequest = { viewModel.toggleTvPreviewFullscreen() },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .testTag("fullscreen_casting_theater_view")
                ) {
                    val currentMedia = castState.currentMedia
                    if (currentMedia != null) {
                        AsyncImage(
                            model = currentMedia.thumbnailUrl,
                            contentDescription = currentMedia.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    CastingPlaybackOverlay(
                        isPlaying = castState.isPlaying,
                        currentPositionSec = castState.currentPositionSec,
                        durationSec = castState.durationSec,
                        volumePercent = castState.volumePercent,
                        isMuted = castState.isMuted,
                        mediaTitle = currentMedia?.title ?: "Yayınlanıyor",
                        mediaCategory = currentMedia?.category ?: "",
                        connectedDevice = castState.connectedDevice,
                        resolution = currentMedia?.resolution ?: "4K UHD",
                        quality = castState.quality,
                        isFullscreen = true,
                        isScreenMirroring = castState.isScreenMirroring,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSeekFraction = { viewModel.seekToFraction(it) },
                        onSeekRelative = { viewModel.seekRelative(it) },
                        onVolumeChange = { viewModel.setVolume(it) },
                        onToggleMute = { viewModel.toggleMute() },
                        onSelectQuality = { viewModel.setQuality(it) },
                        onOpenRemote = { viewModel.openRemoteSheet() },
                        onToggleFullscreen = { viewModel.toggleTvPreviewFullscreen() },
                        onToggleMirroring = { viewModel.toggleScreenMirroring() },
                        onOpenCastPicker = { viewModel.openCastDialog() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistsQuickBar(
    playlists: List<Playlist>,
    onOpenPlaylists: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreateNew: () -> Unit,
    onPlayPlaylist: (Playlist) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = BorderStroke(1.dp, Color(0xFF1F2937))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenPlaylists() }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = CastCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Çalma Listeleri & Koleksiyonlar",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${playlists.size}",
                            color = CastCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Tümünü Gör",
                    color = CastCyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onOpenPlaylists() }
                        .padding(4.dp)
                        .testTag("view_all_playlists_text")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Add Playlist quick chip
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .clickable { onCreateNew() }
                            .testTag("quick_create_playlist_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CastCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yeni Liste",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                items(playlists, key = { it.id }) { pl ->
                    val plColor = try {
                        Color(android.graphics.Color.parseColor(pl.colorHex))
                    } catch (e: Exception) {
                        CastCyanAccent
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, plColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onSelectPlaylist(pl) }
                            .testTag("quick_playlist_chip_${pl.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(plColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getPlaylistIcon(pl.iconName),
                                    contentDescription = null,
                                    tint = plColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = pl.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${pl.itemCount} video",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }

                            if (pl.items.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = plColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onPlayPlaylist(pl) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Oynat",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveredDevicesQuickBar(
    devices: List<CastDevice>,
    connectedDevice: CastDevice?,
    isScanning: Boolean,
    onSelectDevice: (CastDevice) -> Unit,
    onOpenCastDialog: () -> Unit,
    onOpenDiscoveryScreen: () -> Unit = onOpenCastDialog,
    onRescan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDiscoveryScreen() }
            .testTag("discovered_devices_quick_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = BorderStroke(1.dp, Color(0xFF1F2937))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenDiscoveryScreen() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CastCyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = null,
                            tint = CastCyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ağdaki Smart TV'ler",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isScanning) CastCyanAccent else Color(0xFF334155)
                    ) {
                        Text(
                            text = if (isScanning) "Taranıyor..." else "${devices.size} TV",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (isScanning) Color.Black else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRescan,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yeniden Tara",
                            tint = if (isScanning) CastCyanAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Keşfet 🔍",
                        color = CastCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onOpenDiscoveryScreen() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(devices, key = { it.id }) { device ->
                    val isCurrent = connectedDevice?.id == device.id
                    val chipBorderColor = if (isCurrent) CastCyanAccent else Color(0xFF334155)
                    val chipBg = if (isCurrent) CastCyanAccent.copy(alpha = 0.12f) else Color(0xFF1E293B)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = chipBg,
                        border = BorderStroke(1.dp, chipBorderColor),
                        modifier = Modifier
                            .clickable { onSelectDevice(device) }
                            .testTag("device_quick_chip_${device.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) CastCyanAccent else Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCurrent) Icons.Default.CastConnected else Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = if (isCurrent) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = device.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (isCurrent) "Bağlı (${device.ipAddress})" else "${device.brand.displayName} • ${device.protocol.displayName}",
                                    color = if (isCurrent) CastCyanAccent else Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
