package moe.isning.syncthing.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import moe.isning.syncthing.config.ConfigApi
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

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
class RawSyncthingServiceController(
    override var cfg: SyncthingProcessConfig,
    private val scope: CoroutineScope,
    override val api: SyncthingApi,
    private val configXmlParser: ConfigXmlParser,
    private val notificationSink: NotificationSink = NoopNotificationSink,
) : SyncthingServiceController {
    private var runner = buildSyncthingProcessRunner(scope, notificationSink)
    private var monitorJob: Job? = null

    override val isRunning get() = runner.isRunning

    override val configApi: ConfigApi
        get() = if (runner.isRunning) api else configXmlParser


    /** Start the Syncthing process in serve mode and optionally wait until Web GUI is reachable. */
    override suspend fun startServe(
        waitForWebGui: Boolean,
        pollInterval: Duration,
        timeout: Duration?,
    ) {
        runner.start(SyncthingCommand.Serve, cfg)
        if (waitForWebGui) {
            api.poller.waitUntilAvailable(pollInterval, timeout)
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
        withContext(Dispatchers.IO) {
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
        }
    }

    /** Execute one-off commands that exit immediately. */
    override suspend fun runDeviceId(): String? {
        val sb = StringBuilder()
        val tmp = buildSyncthingProcessRunner(scope, object : NotificationSink {
            override fun onOutput(line: String, isError: Boolean) {
                if (!isError) {
                    if (sb.isNotEmpty()) sb.append('\n')
                    sb.append(line)
                }
            }
        })
        tmp.start(SyncthingCommand.DeviceId, cfg)
        tmp.awaitExit()
        return sb.toString().trim().ifEmpty { null }
    }

    override suspend fun runGenerate(): Int {
        val tmp = buildSyncthingProcessRunner(scope, notificationSink)
        tmp.start(SyncthingCommand.Generate, cfg)
        return tmp.awaitExit()
    }

    override suspend fun runResetDatabase(): Int {
        val tmp = buildSyncthingProcessRunner(scope, notificationSink)
        tmp.start(SyncthingCommand.ResetDatabase, cfg)
        return tmp.awaitExit()
    }

    override suspend fun runResetDeltas(): Int {
        val tmp = buildSyncthingProcessRunner(scope, notificationSink)
        tmp.start(SyncthingCommand.ResetDeltas, cfg)
        return tmp.awaitExit()
    }
}
