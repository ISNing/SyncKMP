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
import moe.isning.syncthing.lifecycle.LocalServiceController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersPage() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val controller = LocalServiceController.current
    val api = controller.api
    
    val viewModel = remember { FoldersPageViewModel(api) }
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadFolders()
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(LocalStrings.current.titleFolders) },
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
                                onRefresh = { viewModel.refreshFolder(folderWithStatus.folder.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderCard(
    folderWithStatus: FolderWithStatus,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRefresh: () -> Unit
) {
    val folder = folderWithStatus.folder
    val status = folderWithStatus.status
    
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
                
                OutlinedButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Scan")
                }
            }
        }
    }
}
