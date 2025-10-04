package moe.isning.syncthing.page.main.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.isning.syncthing.http.SyncthingApi
import moe.isning.syncthing.http.SyncthingExpensiveEndpoint

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
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    folders = foldersWithStatus
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun refreshFolder(folderId: String) {
        viewModelScope.launch {
            try {
                api.scanFolder(folderId)
                loadFolders()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun pauseFolder(folderId: String) {
        viewModelScope.launch {
            try {
                val folder = api.getFolder(folderId)
                api.patchFolder(folderId, folder.copy(paused = true))
                loadFolders()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun resumeFolder(folderId: String) {
        viewModelScope.launch {
            try {
                val folder = api.getFolder(folderId)
                api.patchFolder(folderId, folder.copy(paused = false))
                loadFolders()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
