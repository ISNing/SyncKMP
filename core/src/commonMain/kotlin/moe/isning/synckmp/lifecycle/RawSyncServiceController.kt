package moe.isning.synckmp.lifecycle

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.isning.synckmp.config.ConfigApi
import moe.isning.synckmp.config.ConfigXmlParser
import moe.isning.synckmp.http.SyncApi
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


private val logger = KotlinLogging.logger {}

/**
 * Platform-agnostic controller that manages the native process lifecycle and the REST API.
 *
 * This is a cleaner facade compared to the old Android Service.
 * Mapping to old intent actions:
 * - ACTION_RESTART -> restartServe()
 * - ACTION_STOP -> shutdownAndStop()
 * - ACTION_RESET_DATABASE -> runResetDatabase()
 * - ACTION_RESET_DELTAS -> runResetDeltas()
 * - ACTION_OVERRIDE_CHANGES / ACTION_REVERT_LOCAL_CHANGES -> available via REST API (DB_OVERRIDE/DB_REVERT)
 */
class RawSyncServiceController(
    override var cfg: SyncProcessConfig,
    private val scope: CoroutineScope,
    override val api: SyncApi,
    private val configXmlParser: ConfigXmlParser,
    private val notificationSink: NotificationSink = NoopNotificationSink,
) : SyncServiceController {
    private var runner = buildSyncProcessRunner(scope, notificationSink)
    private var monitorJob: Job? = null

    private val mutex = Mutex()

    override val isRunning get() = runner.isRunning

    val _state: MutableStateFlow<SyncServiceState> = MutableStateFlow(SyncServiceState.Idle)
    override val state: StateFlow<SyncServiceState> = _state.asStateFlow()

    override val configApi: ConfigApi
        get() = if (runner.isRunning) api else configXmlParser


    /** Start the Syncthing process in serve mode and optionally wait until Web GUI is reachable. */
    override suspend fun startServe(
        waitForWebGui: Boolean,
        pollInterval: Duration,
        timeout: Duration?,
    ) {
        mutex.withLock {
            if (runner.isRunning) {
                logger.warn { "startServe called but process is already running" }
                return
            }
            _state.value = SyncServiceState.Starting
            logger.info { "Starting Syncthing process" }
            runner.start(ProcessCommand.Serve, cfg)
            if (waitForWebGui) {
                api.poller.waitUntilAvailable(pollInterval, timeout)
                _state.value = SyncServiceState.Running
            } else {
                scope.launch {
                    api.poller.waitUntilAvailable(pollInterval, timeout)
                    _state.value = SyncServiceState.Running
                }
            }
        }
    }

    /** Restart the Syncthing process (serve). */
    override suspend fun restartServe(waitForWebGui: Boolean) {
        shutdownAndStop()
        startServe(waitForWebGui)
    }

    /** Tries to shut down via REST, then stops the process if still running. */
    @OptIn(ExperimentalTime::class)
    override suspend fun shutdownAndStop(gracefulWaitMs: Long) {
        mutex.withLock {
            if (!runner.isRunning) {
                logger.warn { "shutdownAndStop called but process is not running" }
                return
            }
            _state.value = SyncServiceState.Stopping
            withContext(Dispatchers.IO) {
                logger.info { "Shutting down Syncthing process" }
                runCatching { api.shutdown() }
                // Wait a bit for the process to exit by itself
                val started = Clock.System.now().toEpochMilliseconds()
                while (Clock.System.now().toEpochMilliseconds() - started < gracefulWaitMs) {
                    if (!runner.isRunning) break
                    delay(100)
                }
                if (runner.isRunning) {
                    // Process didn’t exit, kill it
                    runner.stop(graceful = false, killDelayMs = 2000)
                }
                _state.value = SyncServiceState.Idle
            }
        }
    }

    /** Execute one-off commands that exit immediately. */
    override suspend fun runDeviceId(): String? {
        val sb = StringBuilder()
        val tmp = buildSyncProcessRunner(scope, object : NotificationSink {
            override fun onOutput(line: String, isError: Boolean) {
                if (!isError) {
                    if (sb.isNotEmpty()) sb.append('\n')
                    sb.append(line)
                }
            }
        })
        tmp.start(ProcessCommand.DeviceId, cfg)
        tmp.awaitExit()
        return sb.toString().trim().ifEmpty { null }
    }

    override suspend fun runGenerate(): Int {
        val tmp = buildSyncProcessRunner(scope, notificationSink)
        tmp.start(ProcessCommand.Generate, cfg)
        return tmp.awaitExit()
    }

    override suspend fun runResetDatabase(): Int {
        val tmp = buildSyncProcessRunner(scope, notificationSink)
        tmp.start(ProcessCommand.ResetDatabase, cfg)
        return tmp.awaitExit()
    }

    override suspend fun runResetDeltas(): Int {
        val tmp = buildSyncProcessRunner(scope, notificationSink)
        tmp.start(ProcessCommand.ResetDeltas, cfg)
        return tmp.awaitExit()
    }
}
