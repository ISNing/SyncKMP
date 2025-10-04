package moe.isning.syncthing.page.main.folders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import kotlinx.coroutines.launch
import moe.isning.syncthing.lifecycle.LocalServiceController
import moe.isning.syncthing.config.Device

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersPage() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val controller = LocalServiceController.current
    val api = controller.api
    
    val viewModel = remember { FoldersPageViewModel(api) }
    val state by viewModel.state.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadFolders()
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
                title = { Text(LocalStrings.current.titleFolders) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Folder")
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
                        Button(onClick = { viewModel.loadFolders() }) {
                            Text("Retry")
                        }
                    }
                }
                state.folders.isEmpty() -> {
                    Text(
                        text = "No folders configured",
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
                        items(state.folders) { folderWithStatus ->
                            FolderCard(
                                folderWithStatus = folderWithStatus,
                                onPause = { viewModel.pauseFolder(folderWithStatus.folder.id) },
                                onResume = { viewModel.resumeFolder(folderWithStatus.folder.id) },
                                onRefresh = { viewModel.refreshFolder(folderWithStatus.folder.id) },
                                onOverride = { viewModel.overrideFolder(folderWithStatus.folder.id) },
                                onRevert = { viewModel.revertFolder(folderWithStatus.folder.id) }
                            )
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddFolderDialog(
                onDismiss = { showAddDialog = false },
                devices = state.availableDevices,
                onConfirm = { folderId, label, path, folderType, deviceIds ->
                    viewModel.addFolder(folderId, label, path, folderType, deviceIds) { success, message ->
                        showAddDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                        if (success) {
                            viewModel.loadFolders()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FolderCard(
    folderWithStatus: FolderWithStatus,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRefresh: () -> Unit,
    onOverride: () -> Unit,
    onRevert: () -> Unit
) {
    val folder = folderWithStatus.folder
    val status = folderWithStatus.status
    
    var showMoreActions by remember { mutableStateOf(false) }
    var showShareEdit by remember { mutableStateOf(false) }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with folder name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.label ?: folder.id,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = folder.path ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                val stateText = status?.state ?: "unknown"
                val stateColor = when (stateText) {
                    "idle" -> MaterialTheme.colorScheme.primary
                    "syncing" -> MaterialTheme.colorScheme.tertiary
                    "error" -> MaterialTheme.colorScheme.error
                    "paused" -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outline
                }
                
                AssistChip(
                    onClick = { },
                    label = { Text(stateText) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = stateColor.copy(alpha = 0.2f),
                        labelColor = stateColor
                    )
                )
            }
            
            Divider()
            
            // Stats
            status?.let { s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "本地文件",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${s.localFiles ?: 0} 文件",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "全局文件",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${s.globalFiles ?: 0} 文件",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Progress bar if syncing
                val completion = if ((s.globalBytes ?: 0) > 0) {
                    ((s.inSyncBytes ?: 0).toFloat() / (s.globalBytes ?: 1).toFloat())
                } else 1f
                
                if (completion < 1f) {
                    Column {
                        LinearProgressIndicator(
                            progress = { completion },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${(completion * 100).toInt()}% synchronized",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (folder.paused == true) {
                    FilledTonalButton(onClick = onResume, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Resume")
                    }
                } else {
                    FilledTonalButton(onClick = onPause, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Pause")
                    }
                }
                
                OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Scan")
                }
                
                IconButton(onClick = { showMoreActions = !showMoreActions }) {
                    Icon(
                        if (showMoreActions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "More actions"
                    )
                }
            }
            
            // Warning if not shared to any device
            if (folder.devices.isEmpty()) {
                AssistChip(
                    onClick = { showShareEdit = true },
                    label = { Text("未共享到任何设备 · 点击设置共享") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) }
                )
            }

            // More actions expanded
            if (showMoreActions) {
                Divider()
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showShareEdit = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("共享到设备", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "修改与哪些设备共享此文件夹",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onOverride,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("Override Changes", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Send local changes to other devices",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onRevert,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("Revert Local Changes", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Revert unexpected local changes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showShareEdit) {
        val controller = LocalServiceController.current
        val api = controller.api
        val vm = remember { FoldersPageViewModel(api) } // lightweight for action
        val state by vm.state.collectAsState()
        // ensure we have device list
        LaunchedEffect(Unit) { vm.loadFolders() }
        val availableDevices = state.availableDevices
        ShareDevicesDialog(
            folderName = folder.label ?: folder.id,
            initialSelected = folder.devices.map { it.id }.toSet(),
            devices = availableDevices,
            onDismiss = { showShareEdit = false },
            onConfirm = { ids ->
                vm.updateFolderDevices(folder.id, ids) { _, _ -> }
                showShareEdit = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderDialog(
    onDismiss: () -> Unit,
    devices: List<Device>,
    onConfirm: (folderId: String, label: String, path: String, folderType: String, deviceIds: List<String>) -> Unit
) {
    var folderId by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    var folderType by remember { mutableStateOf("sendreceive") }
    var selectedDeviceIds by remember(devices) { mutableStateOf(devices.map { it.id }.toSet()) }
    
    var folderIdError by remember { mutableStateOf(false) }
    var pathError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Folder") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = folderId,
                        onValueChange = { 
                            folderId = it.trim()
                            folderIdError = false
                        },
                        label = { Text("Folder ID *") },
                        isError = folderIdError,
                        supportingText = if (folderIdError) {
                            { Text("Folder ID is required") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Folder Label") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = path,
                        onValueChange = { 
                            path = it
                            pathError = false
                        },
                        label = { Text("Folder Path *") },
                        isError = pathError,
                        supportingText = if (pathError) {
                            { Text("Folder path is required") }
                        } else {
                            { Text("Absolute path to the folder") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    Text(
                        text = "Folder Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = folderType == "sendreceive",
                                onClick = { folderType = "sendreceive" }
                            )
                            Column {
                                Text("Send & Receive", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Synchronize files in both directions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = folderType == "sendonly",
                                onClick = { folderType = "sendonly" }
                            )
                            Column {
                                Text("Send Only", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Only send changes to other devices",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = folderType == "receiveonly",
                                onClick = { folderType = "receiveonly" }
                            )
                            Column {
                                Text("Receive Only", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Only receive changes from other devices",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Device selection
                if (devices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Share With Devices",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            devices.forEach { d ->
                                val checked = d.id in selectedDeviceIds
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            selectedDeviceIds = if (isChecked) {
                                                selectedDeviceIds + d.id
                                            } else {
                                                selectedDeviceIds - d.id
                                            }
                                        }
                                    )
                                    Column {
                                        Text(d.name ?: d.id, style = MaterialTheme.typography.bodyMedium)
                                        if (!d.name.isNullOrBlank()) {
                                            Text(d.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        if (selectedDeviceIds.isEmpty()) {
                            Text(
                                text = "未选择任何设备，文件夹将仅在本机生效。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var hasError = false
                    if (folderId.isBlank()) {
                        folderIdError = true
                        hasError = true
                    }
                    if (path.isBlank()) {
                        pathError = true
                        hasError = true
                    }
                    
                    if (!hasError) {
                        onConfirm(folderId, label, path, folderType, selectedDeviceIds.toList())
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
private fun ShareDevicesDialog(
    folderName: String,
    initialSelected: Set<String>,
    devices: List<Device>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var selected by remember(initialSelected, devices) { mutableStateOf(initialSelected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("共享设备 - $folderName") },
        text = {
            if (devices.isEmpty()) {
                Text("无可用设备。先在设备页添加并连接设备。")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    devices.forEach { d ->
                        val checked = d.id in selected
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = { isChecked ->
                                selected = if (isChecked) selected + d.id else selected - d.id
                            })
                            Column {
                                Text(d.name ?: d.id, style = MaterialTheme.typography.bodyMedium)
                                if (!d.name.isNullOrBlank()) {
                                    Text(d.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (selected.isEmpty()) {
                        Text(
                            text = "未选择任何设备，文件夹将仅在本机生效。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toList()) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

