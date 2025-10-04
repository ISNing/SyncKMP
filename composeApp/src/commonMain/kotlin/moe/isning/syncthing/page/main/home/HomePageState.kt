package moe.isning.syncthing.page.main.home

import moe.isning.syncthing.http.SystemVersion
import moe.isning.syncthing.http.SystemStatus
import moe.isning.syncthing.http.Connections
import moe.isning.syncthing.config.Folder

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