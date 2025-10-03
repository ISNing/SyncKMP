package moe.isning.syncthing.http

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.isSuccess
import io.ktor.http.path
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import moe.isning.syncthing.config.ConfigApi
import moe.isning.syncthing.config.Device
import moe.isning.syncthing.config.Folder
import moe.isning.syncthing.config.Gui
import moe.isning.syncthing.config.Options

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn
annotation class SyncthingExpensiveEndpoint

/**
 * High-level, KMP-friendly API for Syncthing using Ktor.
 *
 * - Avoids Android/JVM-only APIs; ready to move into a KMP shared module.
 * - Typed models provided for common endpoints; generic fallbacks for others.
 * - Events streaming exposed as Kotlin Flow.
 */
class SyncthingApi(
    private val client: HttpClient,
    private val baseUrl: String,
): ConfigApi {

    private fun io.ktor.client.request.HttpRequestBuilder.apiUrl(path: String) {
        url(baseUrl)
        url { this.path(path) }
    }

    // ---------- System ----------

    suspend fun getSystemVersion(): SystemVersion =
        client.get { apiUrl(SyncthingEndpoints.VERSION) }.body()

    suspend fun getSystemStatus(): SystemStatus =
        client.get { apiUrl(SyncthingEndpoints.SYSTEM_STATUS) }.body()

    suspend fun getConnections(): Connections =
        client.get { apiUrl(SyncthingEndpoints.CONNECTIONS) }.body()

    suspend fun getSystemLogLevels(): JsonObject =
        client.get { apiUrl(SyncthingEndpoints.SYSTEM_LOGLEVELS) }.body()

    suspend fun setSystemLogLevels(levels: Map<String, String>): Boolean {
        val response = client.post {
            apiUrl(SyncthingEndpoints.SYSTEM_LOGLEVELS)
            setBody(levels)
        }
        return response.status.isSuccess()
    }

    suspend fun getDiscovery(): JsonObject =
        client.get { apiUrl(SyncthingEndpoints.SYSTEM_DISCOVERY) }.body()

    suspend fun clearSystemError(): Boolean {
        val response = client.post { apiUrl(SyncthingEndpoints.SYSTEM_ERROR_CLEAR) }
        return response.status.isSuccess()
    }

    suspend fun getSystemErrors(): SystemErrorResponse =
        client.get { apiUrl(SyncthingEndpoints.SYSTEM_ERROR) }.body()

    suspend fun postSystemError(message: String): Boolean {
        val response = client.post {
            apiUrl(SyncthingEndpoints.SYSTEM_ERROR)
            setBody(message)
        }
        return response.status.isSuccess()
    }

    // GET config as raw JSON (often large/variable)
    suspend fun getConfig(): JsonObject =
        client.get { apiUrl(SyncthingEndpoints.CONFIG) }.body()

    // PUT full config JSON back (replaces entire config)
    suspend fun setConfig(config: JsonObject): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG)
            setBody(config)
        }
        return response.status.isSuccess()
    }

    /**
     * Returns the list of recent log entries in JSON format.
     * @param since Optional RFC 3339 timestamp. If provided, only messages newer than this will be returned.
     * @return SystemLogResponse containing a list of log entries.
     */
    suspend fun getSystemLog(since: String? = null): SystemLogResponse =
        client.get {
            apiUrl(SyncthingEndpoints.SYSTEM_LOG)
            if (!since.isNullOrBlank()) parameter("since", since)
        }.body()

    /**
     * Returns the list of recent log entries as plain text.
     * @param since Optional RFC 3339 timestamp. If provided, only messages newer than this will be returned.
     * @return Log entries as a plain text string.
     */
    suspend fun getSystemLogTxt(since: String? = null): String =
        client.get {
            apiUrl(SyncthingEndpoints.SYSTEM_LOG_TXT)
            if (!since.isNullOrBlank()) parameter("since", since)
        }.body()

    /** 获取 requiresRestart 状态 */
    suspend fun getRequiresRestart(): Boolean =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_RESTART_REQUIRED) }
            .body<JsonObject>()["requiresRestart"]?.jsonPrimitive?.booleanOrNull ?: false

    // Shutdown Syncthing
    suspend fun shutdown(): Boolean {
        val response = client.post { apiUrl(SyncthingEndpoints.SYSTEM_SHUTDOWN) }
        return response.status.isSuccess()
    }

    // ---------- Services/Misc ----------

    suspend fun getDeviceId(): String =
        client.get { apiUrl(SyncthingEndpoints.DEVICE_ID) }.body()

    suspend fun getReport(): ReportInfo =
        client.get { apiUrl(SyncthingEndpoints.REPORT) }.body()

    suspend fun getSupportBundle(): ByteArray =
        client.get { apiUrl(SyncthingEndpoints.DEBUG_SUPPORT) }.body()

    // ---------- Database / Folders ----------

    @SyncthingExpensiveEndpoint
    suspend fun getDbCompletion(folder: String, device: String? = null): CompletionInfo =
        client.get {
            apiUrl(SyncthingEndpoints.DB_COMPLETION)
            parameter("folder", folder)
            if (!device.isNullOrBlank()) parameter("device", device)
        }.body()

    @SyncthingExpensiveEndpoint
    suspend fun getFolderStatus(folder: String): FolderStatus =
        client.get {
            apiUrl(SyncthingEndpoints.DB_STATUS)
            parameter("folder", folder)
        }.body()

    @SyncthingExpensiveEndpoint
    suspend fun getIgnores(folder: String): JsonArray =
        client.get {
            apiUrl(SyncthingEndpoints.DB_IGNORES)
            parameter("folder", folder)
        }.body()

    @SyncthingExpensiveEndpoint
    suspend fun setIgnores(folder: String, patterns: List<String>): Boolean {
        val body = JsonObject(
            mapOf(
                "ignore" to JsonArray(patterns.map { JsonPrimitive(it) })
            )
        )
        val response = client.post {
            apiUrl(SyncthingEndpoints.DB_IGNORES)
            parameter("folder", folder)
            setBody(body)
        }
        return response.status.isSuccess()
    }

    @SyncthingExpensiveEndpoint
    suspend fun overrideFolder(folder: String): Boolean {
        val response = client.post {
            apiUrl(SyncthingEndpoints.DB_OVERRIDE)
            parameter("folder", folder)
        }
        return response.status.isSuccess()
    }

    @SyncthingExpensiveEndpoint
    suspend fun revertFolder(folder: String): Boolean {
        val response = client.post {
            apiUrl(SyncthingEndpoints.DB_REVERT)
            parameter("folder", folder)
        }
        return response.status.isSuccess()
    }

    @SyncthingExpensiveEndpoint
    suspend fun scanFolder(folder: String, subPath: String? = null): Boolean {
        val response = client.post {
            apiUrl(SyncthingEndpoints.DB_SCAN)
            parameter("folder", folder)
            if (!subPath.isNullOrBlank()) parameter("sub", subPath)
        }
        return response.status.isSuccess()
    }

    // ---------- Pending ----------

    suspend fun getPendingDevices(): List<PendingDevice> =
        client.get { apiUrl(SyncthingEndpoints.PENDING_DEVICES) }.body()

    suspend fun getPendingFolders(): List<PendingFolder> =
        client.get { apiUrl(SyncthingEndpoints.PENDING_FOLDERS) }.body()

    // ---------- Stats ----------

    suspend fun getDeviceStats(): Map<String, DeviceStats> =
        client.get { apiUrl(SyncthingEndpoints.STATS_DEVICE) }.body()

    // ---------- Events ----------

    suspend fun getEvents(since: Long = 0L, limit: Int = 0): List<SyncthingEvent> =
        client.get {
            apiUrl(SyncthingEndpoints.EVENTS)
            if (since > 0) parameter("since", since)
            if (limit > 0) parameter("limit", limit)
        }.body()

    suspend fun getDiskEvents(since: Long = 0L, limit: Int = 0): List<SyncthingEvent> =
        client.get {
            apiUrl(SyncthingEndpoints.EVENTS_DISK)
            if (since > 0) parameter("since", since)
            if (limit > 0) parameter("limit", limit)
        }.body()

    /**
     * Stream events continuously using Kotlin Flow.
     * Pages through history starting at [startId] and then long-polls for new events.
     *
     * @param startId last processed event id (0 to read from current head)
     * @param pageLimit number of events per request (0 = server default / unlimited)
     * @param pollIntervalMs delay between requests when idle or on error
     */
    fun streamEvents(
        startId: Long = 0,
        pageLimit: Int = 0,
        pollIntervalMs: Long = 5_000,
    ): Flow<SyncthingEvent> = channelFlow {
        var lastId = startId
        while (!isClosedForSend) {
            try {
                val events = getEvents(since = lastId, limit = pageLimit)
                if (events.isNotEmpty()) {
                    for (e in events) {
                        trySend(e)
                        if (e.id > lastId) lastId = e.id
                    }
                    // Immediately fetch next page without delay
                    continue
                }
            } catch (t: Throwable) {
                // Swallow errors and retry after delay
            }
            delay(pollIntervalMs)
        }
    }

    // ---------- Images / Binary ----------

    /**
     * Fetches a resource as raw bytes (e.g., QR or icon) with optional query parameters.
     * Caller is responsible for decoding (e.g., to Bitmap on Android or NSImage on iOS).
     */
    suspend fun fetchBytes(path: String, query: Map<String, String> = emptyMap()): ByteArray =
        client.get {
            apiUrl(path)
            query.forEach { (k, v) -> parameter(k, v) }
        }.body()

    // ---------- Config: Folders ----------

    /** 获取所有 folders 配置 */
    override suspend fun getFolders(): List<Folder> =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_FOLDERS) }.body()

    /** 批量替换所有 folders 配置 */
    override suspend fun putFolders(folders: List<Folder>): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_FOLDERS)
            setBody(folders)
        }
        return response.status.isSuccess()
    }

    /** 新增单个 folder */
    override suspend fun addFolder(folder: Folder): Boolean {
        val response = client.post {
            apiUrl(SyncthingEndpoints.CONFIG_FOLDERS)
            setBody(folder)
        }
        return response.status.isSuccess()
    }

    /** 获取单个 folder 配置 */
    override suspend fun getFolder(id: String): Folder =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_FOLDER_ID + id) }.body()

    /** 替换单个 folder 配置 */
    override suspend fun putFolder(id: String, folder: Folder): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_FOLDER_ID + id)
            setBody(folder)
        }
        return response.status.isSuccess()
    }

    /** 局部更新单个 folder 配置 */
    override suspend fun patchFolder(id: String, patch: Folder): Boolean {
        val response = client.patch {
            apiUrl(SyncthingEndpoints.CONFIG_FOLDER_ID + id)
            setBody(patch)
        }
        return response.status.isSuccess()
    }

    /** 删除单个 folder 配置 */
    override suspend fun deleteFolder(id: String): Boolean {
        val response = client.delete {
            apiUrl(SyncthingEndpoints.CONFIG_FOLDER_ID + id)
        }
        return response.status.isSuccess()
    }

    // ---------- Config: Devices ----------

    /** 获取所有 devices 配置 */
    override suspend fun getDevices(): List<Device> =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_DEVICES) }.body()

    /** 批量替换所有 devices 配置 */
    override suspend fun putDevices(devices: List<Device>): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_DEVICES)
            setBody(devices)
        }
        return response.status.isSuccess()
    }

    /** 新增单个 device */
    override suspend fun addDevice(device: Device): Boolean {
        val response = client.post {
            apiUrl(SyncthingEndpoints.CONFIG_DEVICES)
            setBody(device)
        }
        return response.status.isSuccess()
    }

    /** 获取单个 device 配置 */
    override suspend fun getDevice(id: String): Device =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_DEVICE_ID + id) }.body()

    /** 替换单个 device 配置 */
    override suspend fun putDevice(id: String, device: Device): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_DEVICE_ID + id)
            setBody(device)
        }
        return response.status.isSuccess()
    }

    override suspend fun patchDevice(id: String, patch: Device): Boolean {
        val response = client.patch {
            apiUrl(SyncthingEndpoints.CONFIG_DEVICE_ID + id)
            setBody(patch)
        }
        return response.status.isSuccess()
    }

    /** 删除单个 device 配置 */
    override suspend fun deleteDevice(id: String): Boolean {
        val response = client.delete {
            apiUrl(SyncthingEndpoints.CONFIG_DEVICE_ID + id)
        }
        return response.status.isSuccess()
    }

    // ---------- Config: Options ----------

    /** 获取 options 配置 */
    override suspend fun getOptions(): Options =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_OPTIONS) }.body()

    /** 替换 options 配置 */
    override suspend fun putOptions(options: Options): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_OPTIONS)
            setBody(options)
        }
        return response.status.isSuccess()
    }

    /** 局部更新 options 配置 */
    override suspend fun patchOptions(patch: Options): Boolean {
        val response = client.patch {
            apiUrl(SyncthingEndpoints.CONFIG_OPTIONS)
            setBody(patch)
        }
        return response.status.isSuccess()
    }

    // ---------- Config: Defaults ----------

    /** 获取默认 folder 配置模板 */
    override suspend fun getDefaultFolder(): Folder =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_FOLDER) }.body()

    /** 替换默认 folder 配置模板 */
    override suspend fun putDefaultFolder(defaults: Folder): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_FOLDER)
            setBody(defaults)
        }
        return response.status.isSuccess()
    }

    /** 局部更新默认 folder 配置模板 */
    override suspend fun patchDefaultFolder(patch: Folder): Boolean {
        val response = client.patch {
            apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_FOLDER)
            setBody(patch)
        }
        return response.status.isSuccess()
    }

    /** 获取默认 device 配置模板 */
    override suspend fun getDefaultDevice(): Device =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_DEVICE) }.body()

    /** 替换默认 device 配置模板 */
    override suspend fun putDefaultDevice(defaults: Device): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_DEVICE)
            setBody(defaults)
        }
        return response.status.isSuccess()
    }

    /** 局部更新默认 device 配置模板 */
    override suspend fun patchDefaultDevice(patch: Device): Boolean {
        val response = client.patch {
            apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_DEVICE)
            setBody(patch)
        }
        return response.status.isSuccess()
    }

    /** 获取默认 ignores 配置，直接返回 lines 字段的 List<String> */
    override suspend fun getDefaultIgnores(): List<String> =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_IGNORES) }
            .body<JsonObject>()["lines"]
            ?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

    override suspend fun putDefaultIgnores(lines: List<String>): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_DEFAULTS_IGNORES)
            setBody(JsonObject(mapOf("lines" to JsonArray(lines.map { JsonPrimitive(it) }))))
        }
        return response.status.isSuccess()
    }

    /** 获取 gui 配置 */
    override suspend fun getGui(): Gui =
        client.get { apiUrl(SyncthingEndpoints.CONFIG_GUI) }.body()

    /** 替换 gui 配置 */
    override suspend fun putGui(gui: Gui): Boolean {
        val response = client.put {
            apiUrl(SyncthingEndpoints.CONFIG_GUI)
            setBody(gui)
        }
        return response.status.isSuccess()
    }

    /** 局部更新 gui 配置 */
    override suspend fun patchGui(patch: Gui): Boolean {
        val response = client.patch {
            apiUrl(SyncthingEndpoints.CONFIG_GUI)
            setBody(patch)
        }
        return response.status.isSuccess()
    }

    val poller by lazy { WebGuiPoller(client, baseUrl) }
}
