package moe.isning.synckmp.page.main.folders

import moe.isning.synckmp.config.Device
import moe.isning.synckmp.config.Folder
import moe.isning.synckmp.http.FolderStatus

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
