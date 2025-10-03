package moe.isning.syncthing.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Minimal typed models for common Syncthing endpoints.
 * Unknown fields are ignored to keep compatibility across Syncthing versions.
 */

@Serializable
data class SystemVersion(
    val version: String = "",
    val longVersion: String? = null,
    val os: String? = null,
    val arch: String? = null,
    val uid: Int? = null,
    val gid: Int? = null,
)

@Serializable
data class SystemStatus(
    val myID: String = "",
    val alloc: Long? = null,
    val sys: Long? = null,
    val goroutines: Int? = null,
    val uptime: Long? = null,
)

@Serializable
data class ConnectionInfo(
    val address: String? = null,
    val type: String? = null,
    val connected: Boolean = false,
    @Deprecated("Legacy props") val since: String? = null,
    val clientVersion: String? = null,
    @Deprecated("Legacy props") val inbound: Boolean? = null,
    val at: String? = null,
    val inBytesTotal: Long? = null,
    val outBytesTotal: Long? = null,
    val isLocal: Boolean? = null,
    val paused: Boolean? = null,
    val startedAt: String? = null
)

@Serializable
data class Connections(
    val total: ConnectionTotals? = null,
    val connections: Map<String, ConnectionInfo> = emptyMap(),
)

@Serializable
data class ConnectionTotals(
    val at: Long? = null,
    val inBytesTotal: Long? = null,
    val outBytesTotal: Long? = null,
)

@Serializable
data class CompletionInfo(
    val completion: Double = 0.0,
    val globalBytes: Long? = null,
    val needBytes: Long? = null,
    val globalItems: Long? = null,
    val needItems: Long? = null,
    val needDeletes: Long? = null,
    val remoteState: String? = null,
    val sequence: Long? = null,
    val state: String? = null,
)

@Serializable
data class FolderStatus(
    val state: String? = null,
    val stateChanged: String? = null,
    val globalBytes: Long? = null,
    val globalDeleted: Long? = null,
    val globalDirectories: Long? = null,
    val globalFiles: Long? = null,
    val globalSymlinks: Long? = null,
    val globalTotalItems: Long? = null,
    val ignorePatterns: Boolean? = null,
    val inSyncBytes: Long? = null,
    val inSyncFiles: Long? = null,
    val invalid: String? = null,
    val localBytes: Long? = null,
    val localDeleted: Long? = null,
    val localDirectories: Long? = null,
    val localFiles: Long? = null,
    val localSymlinks: Long? = null,
    val localTotalItems: Long? = null,
    val needBytes: Long? = null,
    val needDeletes: Long? = null,
    val needDirectories: Long? = null,
    val needFiles: Long? = null,
    val needSymlinks: Long? = null,
    val needTotalItems: Long? = null,
    val pullErrors: Long? = null,
    val receiveOnlyChangedBytes: Long? = null,
    val receiveOnlyChangedDeletes: Long? = null,
    val receiveOnlyChangedDirectories: Long? = null,
    val receiveOnlyChangedFiles: Long? = null,
    val receiveOnlyChangedSymlinks: Long? = null,
    val receiveOnlyTotalItems: Long? = null,
    val sequence: Long? = null,
    val version: Long? = null
)

@Serializable
data class DeviceStats(
    val lastSeen: String? = null,
    val lastConnectionDurationS: Long? = null,
)

@Serializable
data class SyncthingEvent(
    val id: Long,
    val type: String,
    val time: String,
    val data: JsonObject? = null,
)

@Serializable
data class PendingDevice(
    val deviceID: String,
    val name: String? = null,
    val address: String? = null,
)

@Serializable
data class PendingFolder(
    val id: String,
    val label: String? = null,
    val path: String? = null,
)

@Serializable
data class ReportInfo(
    val uniqueID: String? = null,
    val version: String? = null,
    val longVersion: String? = null,
    val platform: String? = null,
    val numFolders: Int? = null,
    val numDevices: Int? = null,
    val sha256: String? = null,
    val errors: List<String>? = null,
    val data: JsonElement? = null,
)

@Serializable
data class LogEntry(
    @SerialName("when") val time: String,
    val message: String
)

// 定义系统错误响应数据类
@Serializable
data class SystemErrorResponse(
    val errors: List<LogEntry>
)

@Serializable
data class SystemLogResponse(
    val messages: List<LogEntry>
)