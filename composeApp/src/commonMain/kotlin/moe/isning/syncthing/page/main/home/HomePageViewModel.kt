package moe.isning.syncthing.page.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.isning.syncthing.http.SyncthingApi
import moe.isning.syncthing.http.FolderStatus
import moe.isning.syncthing.http.SyncthingExpensiveEndpoint
import kotlin.time.Duration.Companion.seconds

@OptIn(SyncthingExpensiveEndpoint::class)
class HomePageViewModel(private val api: SyncthingApi) : ViewModel() {
    private val _state = MutableStateFlow(HomePageState())
    val state: StateFlow<HomePageState> = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                // 并发获取所有数据
                val version = api.getSystemVersion()
                val status = api.getSystemStatus()
                val connections = api.getConnections()
                val folders = api.getFolders()
                
                // 计算文件夹统计信息
                var totalFiles = 0L
                var totalFolders = 0L
                var totalBytes = 0L
                
                folders.forEach { folder ->
                    try {
                        val folderStatus = api.getFolderStatus(folder.id)
                        totalFiles += folderStatus.localFiles ?: 0L
                        totalFolders += folderStatus.localDirectories ?: 0L
                        totalBytes += folderStatus.localBytes ?: 0L
                    } catch (e: Exception) {
                        // 忽略单个文件夹的错误
                    }
                }
                
                // 计算运行时间
                val uptimeSeconds = status.uptime ?: 0L
                val uptime = formatUptime(uptimeSeconds)
                
                // 计算连接设备数量
                val connectedCount = connections.connections.count { it.value.connected }
                val totalCount = connections.connections.size
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    systemVersion = version,
                    systemStatus = status,
                    connections = connections,
                    folders = folders,
                    uptime = uptime,
                    downloadTotal = formatBytes(connections.total?.inBytesTotal ?: 0L),
                    uploadTotal = formatBytes(connections.total?.outBytesTotal ?: 0L),
                    localFilesCount = totalFiles,
                    localFoldersCount = totalFolders,
                    localTotalSize = formatBytes(totalBytes),
                    connectedDevicesCount = connectedCount,
                    totalDevicesCount = totalCount
                )
            } catch (e: Exception) {
                // 检查是否是连接被拒绝的错误（服务未运行）
                val errorMessage = when {
                    e.message?.contains("Connection refused") == true -> 
                        "Syncthing 服务未运行"
                    e.message?.contains("ConnectException") == true -> 
                        "无法连接到 Syncthing 服务"
                    else -> 
                        e.message ?: "未知错误"
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }
    
    fun resetState() {
        _state.value = HomePageState()
    }
    
    private fun formatUptime(seconds: Long): String {
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        
        return buildString {
            if (days > 0) append("${days}天 ")
            if (hours > 0 || days > 0) append("${hours}时 ")
            append("${minutes}分")
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.2f KiB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.2f MiB".format(mb)
        val gb = mb / 1024.0
        return "%.2f GiB".format(gb)
    }
}