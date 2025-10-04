package moe.isning.syncthing.lifecycle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import moe.isning.syncthing.config.ConfigApi
import moe.isning.syncthing.http.SyncthingApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

enum class SyncthingServiceState {
    Idle,
    Starting,
    Running,
    Stopping,
    Error,
}

interface SyncthingServiceController : SyncthingProcessLifecycleController, SyncthingConfigController,
    SyncthingApiController

interface SyncthingProcessLifecycleController {
    var cfg: SyncthingProcessConfig

    val isRunning: Boolean

    val state: StateFlow<SyncthingServiceState>

    suspend fun startServe(
        waitForWebGui: Boolean = true,
        pollInterval: Duration = 150.milliseconds,
        timeout: Duration? = null,
    )

    /** Restart the Syncthing process (serve). */
    suspend fun restartServe(waitForWebGui: Boolean = true)

    /** Tries to shut down via REST, then stops the process if still running. */
    suspend fun shutdownAndStop(gracefulWaitMs: Long = 5000)

    /** Execute one-off commands that exit immediately. */
    suspend fun runDeviceId(): String?
    suspend fun runGenerate(): Int
    suspend fun runResetDatabase(): Int
    suspend fun runResetDeltas(): Int
}

interface SyncthingConfigController {
    val configApi: ConfigApi
}

interface SyncthingApiController {
    val api: SyncthingApi
}
