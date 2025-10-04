package moe.isning.syncthing.page.main.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.isning.syncthing.http.SyncthingApi
import moe.isning.syncthing.http.SyncthingExpensiveEndpoint
import moe.isning.syncthing.config.Folder
import moe.isning.syncthing.config.FolderDevice
import moe.isning.syncthing.config.Device

@OptIn(SyncthingExpensiveEndpoint::class)
class FoldersPageViewModel(private val api: SyncthingApi) : ViewModel() {
    private val _state = MutableStateFlow(FoldersPageState())
    val state: StateFlow<FoldersPageState> = _state.asStateFlow()

    fun loadFolders() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                val folders = api.getFolders()
                val foldersWithStatus = folders.map { folder ->
                    try {
                        val status = api.getFolderStatus(folder.id)
                        FolderWithStatus(folder, status)
                    } catch (e: Exception) {
                        FolderWithStatus(folder, null)
                    }
                }
                // also load available devices to allow sharing selection when adding a folder
                val myId = runCatching { api.getSystemStatus().myID }.getOrNull()
                val devices = runCatching { api.getDevices() }
                    .getOrDefault(emptyList())
                    .let { list -> if (myId == null) list else list.filter { it.id != myId } }

                _state.value = _state.value.copy(
                    isLoading = false,
                    folders = foldersWithStatus,
                    availableDevices = devices
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun updateFolderDevices(
        folderId: String,
        deviceIds: List<String>,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val current = api.getFolder(folderId)
                val newDevices = deviceIds.distinct().map { FolderDevice(id = it) }
                val updated = current.copy(devices = newDevices)
                val ok = api.putFolder(folderId, updated)
                if (ok) {
                    runCatching { api.scanFolder(folderId) }
                    _state.value = _state.value.copy(operationMessage = "已更新共享设备并开始扫描")
                    loadFolders()
                    onComplete(true, "共享设置已更新")
                } else {
                    onComplete(false, "共享设置更新失败")
                }
            } catch (e: Exception) {
                onComplete(false, "更新失败: ${e.message}")
            }
        }
    }

    fun addFolder(
        folderId: String,
        label: String,
        path: String,
        folderType: String,
        deviceIds: List<String>,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Build selected devices list
                val selectedDevices: List<FolderDevice> = deviceIds.distinct().map { id ->
                    FolderDevice(id = id)
                }

                val folder = Folder(
                    id = folderId,
                    label = label.ifBlank { null },
                    path = path,
                    type = folderType,
                    devices = selectedDevices,
                    paused = false,
                    rescanIntervalS = 3600,
                    fsWatcherEnabled = true,
                    fsWatcherDelayS = 10
                )
                
                val success = api.addFolder(folder)
                if (success) {
                    // Trigger an initial scan to start sync as soon as possible
                    runCatching { api.scanFolder(folderId) }
                    onComplete(true, "Folder added successfully. 如果对端未开启自动接受，请到对端确认接受该文件夹共享。")
                } else {
                    onComplete(false, "Failed to add folder")
                }
            } catch (e: Exception) {
                onComplete(false, "Error: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    fun refreshFolder(folderId: String) {
        viewModelScope.launch {
            try {
                api.scanFolder(folderId)
                _state.value = _state.value.copy(
                    operationMessage = "Scan started successfully"
                )
                loadFolders()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    operationMessage = "Failed to scan folder: ${e.message}"
                )
            }
        }
    }
    
    fun pauseFolder(folderId: String) {
        viewModelScope.launch {
            try {
                val folder = api.getFolder(folderId)
                api.patchFolder(folderId, folder.copy(paused = true))
                _state.value = _state.value.copy(
                    operationMessage = "Folder paused successfully"
                )
                loadFolders()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    operationMessage = "Failed to pause folder: ${e.message}"
                )
            }
        }
    }
    
    fun resumeFolder(folderId: String) {
        viewModelScope.launch {
            try {
                val folder = api.getFolder(folderId)
                api.patchFolder(folderId, folder.copy(paused = false))
                _state.value = _state.value.copy(
                    operationMessage = "Folder resumed successfully"
                )
                loadFolders()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    operationMessage = "Failed to resume folder: ${e.message}"
                )
            }
        }
    }
    
    fun overrideFolder(folderId: String) {
        viewModelScope.launch {
            try {
                api.overrideFolder(folderId)
                _state.value = _state.value.copy(
                    operationMessage = "Override sent successfully"
                )
                loadFolders()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    operationMessage = "Failed to override folder: ${e.message}"
                )
            }
        }
    }
    
    fun revertFolder(folderId: String) {
        viewModelScope.launch {
            try {
                api.revertFolder(folderId)
                _state.value = _state.value.copy(
                    operationMessage = "Folder reverted successfully"
                )
                loadFolders()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    operationMessage = "Failed to revert folder: ${e.message}"
                )
            }
        }
    }
}
