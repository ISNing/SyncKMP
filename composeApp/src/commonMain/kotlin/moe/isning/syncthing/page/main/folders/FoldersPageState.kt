package moe.isning.syncthing.page.main.folders

import moe.isning.syncthing.config.Folder
import moe.isning.syncthing.http.FolderStatus
import moe.isning.syncthing.config.Device

data class FolderWithStatus(
    val folder: Folder,
    val status: FolderStatus? = null
)

data class FoldersPageState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val folders: List<FolderWithStatus> = emptyList(),
    val operationMessage: String? = null,
    val availableDevices: List<Device> = emptyList()
)
