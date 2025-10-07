package moe.isning.synckmp.page.main.folders

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
import moe.isning.synckmp.config.Device
import moe.isning.synckmp.lifecycle.LocalServiceController

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
    
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteLabel by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(LocalStrings.current.titleFolders) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { viewModel.loadFolders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新文件夹列表")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加文件夹")
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
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text("错误：${state.error}")
                        Button(onClick = { viewModel.loadFolders() }) {
                            Text("重试")
                        }
                    }
                }
                state.folders.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "暂无文件夹",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "点击右下角 + 按钮添加第一个文件夹",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                                onRevert = { viewModel.revertFolder(folderWithStatus.folder.id) },
                                onDelete = {
                                    pendingDeleteId = folderWithStatus.folder.id
                                    pendingDeleteLabel = folderWithStatus.folder.label ?: folderWithStatus.folder.id
                                }
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
    // Delete confirm dialog
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null; pendingDeleteLabel = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("删除文件夹") },
            text = { Text("确定要删除文件夹 ${pendingDeleteLabel}? 此操作不会删除磁盘上的文件。") },
            confirmButton = {
                TextButton(onClick = {
                    val id = pendingDeleteId
                    pendingDeleteId = null; pendingDeleteLabel = null
                    if (id != null) {
                        viewModel.deleteFolder(id) { _, msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    }
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null; pendingDeleteLabel = null }) { Text("取消") }
            }
        )
    }
}

@Composable
fun FolderCard(
    folderWithStatus: FolderWithStatus,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRefresh: () -> Unit,
    onOverride: () -> Unit,
    onRevert: () -> Unit,
    onDelete: () -> Unit
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
                            text = "${(completion * 100).toInt()}% 已同步",
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
                        Text("继续")
                    }
                } else {
                    FilledTonalButton(onClick = onPause, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("暂停")
                    }
                }
                
                OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("扫描")
                }
                
                IconButton(onClick = { showMoreActions = !showMoreActions }) {
                    Icon(
                        if (showMoreActions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "更多操作"
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
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("删除文件夹")
                    }
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
                            Text("强制覆盖变更", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "将本地更改发送至其他设备",
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
                            Text("回滚本地更改", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "回滚意外的本地更改",
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
    title = { Text("添加文件夹") },
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
                        label = { Text("文件夹 ID *") },
                        isError = folderIdError,
                        supportingText = if (folderIdError) {
                            { Text("文件夹 ID 为必填") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("文件夹名称") },
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
                        label = { Text("文件夹路径 *") },
                        isError = pathError,
                        supportingText = if (pathError) {
                            { Text("文件夹路径为必填") }
                        } else {
                            { Text("请填写文件夹的绝对路径") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                item {
                    Text(
                        text = "文件夹类型",
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
                                Text("发送与接收", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "双向同步文件",
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
                                Text("仅发送", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "仅向其他设备发送更改",
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
                                Text("仅接收", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "仅从其他设备接收更改",
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
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
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

