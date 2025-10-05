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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import kotlinx.coroutines.launch
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
    
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadDevices()
    }
    
    // Show operation messages
    LaunchedEffect(state.operationMessage) {
        state.operationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(LocalStrings.current.titleDevices) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Device")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        // My Device ID Card
                        item {
                            state.myDeviceId?.let { deviceId ->
                                MyDeviceIdCard(
                                    deviceId = deviceId,
                                    onCopySuccess = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Device ID copied to clipboard")
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Section header for other devices
                        item {
                            Text(
                                text = "Connected Devices",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        items(state.devices) { deviceWithConnection ->
                            DeviceCard(
                                deviceWithConnection = deviceWithConnection,
                                onPause = { viewModel.pauseDevice(deviceWithConnection.device.id) },
                                onResume = { viewModel.resumeDevice(deviceWithConnection.device.id) },
                                onDelete = {
                                    val deviceId = deviceWithConnection.device.id
                                    val isMy = state.myDeviceId == deviceId
                                    coroutineScope.launch {
                                        if (isMy) {
                                            snackbarHostState.showSnackbar("不能删除本机设备")
                                            return@launch
                                        }
                                        val result = snackbarHostState.showSnackbar(
                                            message = "删除设备 ${deviceWithConnection.device.name ?: deviceId.take(7)}?",
                                            actionLabel = "确认",
                                            withDismissAction = true
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            // ask cascade via second confirm
                                            val cascade = snackbarHostState.showSnackbar(
                                                message = "是否同时从所有文件夹取消共享该设备?",
                                                actionLabel = "是",
                                                withDismissAction = true
                                            ) == SnackbarResult.ActionPerformed
                                            viewModel.deleteDevice(deviceId, cascade) { _, msg ->
                                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddDeviceDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { deviceId, name, addresses ->
                    viewModel.addDevice(deviceId, name, addresses) { success, message ->
                        showAddDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                        if (success) {
                            viewModel.loadDevices()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DeviceCard(
    deviceWithConnection: DeviceWithConnection,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
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

                OutlinedButton(onClick = onDelete, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (deviceId: String, name: String, addresses: List<String>) -> Unit
) {
    var deviceId by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var addressesText by remember { mutableStateOf("dynamic") }
    var compression by remember { mutableStateOf("metadata") }
    var introducer by remember { mutableStateOf(false) }
    
    var deviceIdError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Device") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = deviceId,
                        onValueChange = { 
                            deviceId = it.trim()
                            deviceIdError = false
                        },
                        label = { Text("Device ID *") },
                        isError = deviceIdError,
                        supportingText = if (deviceIdError) {
                            { Text("Device ID is required") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 4
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = { Text("Device Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = addressesText,
                        onValueChange = { addressesText = it },
                        label = { Text("Addresses") },
                        supportingText = { Text("Comma-separated list or 'dynamic'") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                }
                
                item {
                    Text(
                        text = "Compression",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = compression == "metadata",
                            onClick = { compression = "metadata" },
                            label = { Text("Metadata") }
                        )
                        FilterChip(
                            selected = compression == "always",
                            onClick = { compression = "always" },
                            label = { Text("Always") }
                        )
                        FilterChip(
                            selected = compression == "never",
                            onClick = { compression = "never" },
                            label = { Text("Never") }
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Introducer",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Auto-accept folders from this device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = introducer,
                            onCheckedChange = { introducer = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (deviceId.isBlank()) {
                        deviceIdError = true
                    } else {
                        val addresses = addressesText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        onConfirm(deviceId, deviceName, addresses)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MyDeviceIdCard(
    deviceId: String,
    onCopySuccess: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "This Device",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = { Text("Local") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Device ID",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = deviceId,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(deviceId))
                                onCopySuccess()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Device ID",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "Share this ID with other devices to establish connections",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}


