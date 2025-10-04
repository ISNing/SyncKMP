package moe.isning.syncthing.page.main.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import moe.isning.syncthing.lifecycle.LocalServiceController
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesPage() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val controller = LocalServiceController.current
    val api = controller.api
    
    val viewModel = remember { DevicesPageViewModel(api) }
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadDevices()
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(LocalStrings.current.titleDevices) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.error}")
                        Button(onClick = { viewModel.loadDevices() }) {
                            Text("Retry")
                        }
                    }
                }
                state.devices.isEmpty() -> {
                    Text(
                        text = "No devices configured",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.devices) { deviceWithConnection ->
                            DeviceCard(
                                deviceWithConnection = deviceWithConnection,
                                onPause = { viewModel.pauseDevice(deviceWithConnection.device.id) },
                                onResume = { viewModel.resumeDevice(deviceWithConnection.device.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    deviceWithConnection: DeviceWithConnection,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    val device = deviceWithConnection.device
    val connection = deviceWithConnection.connection
    val isConnected = connection?.connected ?: false
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with device name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name ?: device.id.take(7),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = device.id.take(7) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Connection status badge
                AssistChip(
                    onClick = { },
                    label = { Text(if (isConnected) "Connected" else "Disconnected") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isConnected) 
                            MaterialTheme.colorScheme.primaryContainer
                        else 
                            MaterialTheme.colorScheme.errorContainer,
                        labelColor = if (isConnected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
            
            if (isConnected) {
                Divider()
                
                // Connection details
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    connection?.let { conn ->
                        DeviceInfoRow(
                            Icons.Default.Computer,
                            "Address",
                            conn.address ?: "unknown"
                        )
                        
                        DeviceInfoRow(
                            Icons.Default.Extension,
                            "Client Version",
                            conn.clientVersion ?: "unknown"
                        )
                        
                        DeviceInfoRow(
                            Icons.Default.Download,
                            "Downloaded",
                            formatBytes(conn.inBytesTotal ?: 0L)
                        )
                        
                        DeviceInfoRow(
                            Icons.Default.Upload,
                            "Uploaded",
                            formatBytes(conn.outBytesTotal ?: 0L)
                        )
                        
                        if (conn.paused == true) {
                            DeviceInfoRow(
                                Icons.Default.Pause,
                                "Status",
                                "Paused",
                                highlight = true
                            )
                        }
                    }
                }
            } else {
                Divider()
                Text(
                    text = "Device is not currently connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (device.paused == true) {
                    FilledTonalButton(onClick = onResume) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Resume")
                    }
                } else {
                    FilledTonalButton(onClick = onPause) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Pause")
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceInfoRow(icon: ImageVector, title: String, value: String, highlight: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatBytes(bytes: Long): String {
    val absBytes = abs(bytes)
    if (absBytes < 1024) return "$bytes B"
    val kb = absBytes / 1024.0
    if (kb < 1024) return "%.2f KiB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.2f MiB".format(mb)
    val gb = mb / 1024.0
    return "%.2f GiB".format(gb)
}


