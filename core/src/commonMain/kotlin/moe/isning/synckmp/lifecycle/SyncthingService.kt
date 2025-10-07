package moe.isning.synckmp.lifecycle

import kotlinx.coroutines.flow.StateFlow
import moe.isning.synckmp.config.ConfigApi
import moe.isning.synckmp.http.SyncApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

enum class SyncServiceState {
    Idle,
    Starting,
    Running,
    Stopping,
    Error,
}

interface SyncServiceController : SyncProcessLifecycleController, SyncConfigController,
    SyncApiController

interface SyncProcessLifecycleController {
    var cfg: SyncProcessConfig

    val isRunning: Boolean

    val state: StateFlow<SyncServiceState>

    suspend fun startServe(
        waitForWebGui: Boolean = true,
        pollInterval: Duration = 150.milliseconds,
        timeout: Duration? = null,
    )

    suspend fun restartServe(waitForWebGui: Boolean = true)

    /** Tries to shut down via REST, then stops the process if still running. */
    suspend fun shutdownAndStop(gracefulWaitMs: Long = 5000)

    /** Execute one-off commands that exit immediately. */
    suspend fun runDeviceId(): String?
    suspend fun runGenerate(): Int
    suspend fun runResetDatabase(): Int
    suspend fun runResetDeltas(): Int
}

interface SyncConfigController {
    val configApi: ConfigApi
}

interface SyncApiController {
    val api: SyncApi
}
