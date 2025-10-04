package moe.isning.syncthing.page.main.folders

import moe.isning.syncthing.config.Folder
import moe.isning.syncthing.http.FolderStatus

data class FolderWithStatus(
    val folder: Folder,
    val status: FolderStatus? = null
)

data class FoldersPageState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val folders: List<FolderWithStatus> = emptyList()
)
