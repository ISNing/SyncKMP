package moe.isning.synckmp.page.main.home

import moe.isning.synckmp.config.Folder
import moe.isning.synckmp.http.Connections
import moe.isning.synckmp.http.SystemStatus
import moe.isning.synckmp.http.SystemVersion

data class HomePageState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val systemVersion: SystemVersion? = null,
    val systemStatus: SystemStatus? = null,
    val connections: Connections? = null,
    val folders: List<Folder> = emptyList(),
    val uptime: String = "",
    val downloadSpeed: String = "0 B/s",
    val uploadSpeed: String = "0 B/s",
    val downloadTotal: String = "0 B",
    val uploadTotal: String = "0 B",
    val localFilesCount: Long = 0,
    val localFoldersCount: Long = 0,
    val localTotalSize: String = "0 B",
    val connectedDevicesCount: Int = 0,
    val totalDevicesCount: Int = 0,
    val listeningAddresses: Int = 0
)