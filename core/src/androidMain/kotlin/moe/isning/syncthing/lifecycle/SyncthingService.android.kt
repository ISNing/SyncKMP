package moe.isning.syncthing.lifecycle

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.isning.syncthing.config.ConfigApi
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi
import kotlin.time.Duration


private val logger = KotlinLogging.logger {}

class AndroidSyncthingServiceController(
    private val context: Context,
    cfg: SyncthingProcessConfig,
    scope: CoroutineScope,
    api: SyncthingApi,
    configXmlParser: ConfigXmlParser,
    notificationSink: NotificationSink = NoopNotificationSink,
) : SyncthingServiceController {
    val rawController: RawSyncthingServiceController = RawSyncthingServiceController(
        cfg = cfg,
        scope = scope,
        api = api,
        configXmlParser = configXmlParser,
        notificationSink = notificationSink,
    )

    override var cfg: SyncthingProcessConfig by rawController::cfg
    override val api: SyncthingApi by rawController::api

    val mutex = Mutex()

    override val isRunning: Boolean by rawController::isRunning
    override val state: StateFlow<SyncthingServiceState> by rawController::state
    override suspend fun startServe(
        waitForWebGui: Boolean,
        pollInterval: Duration,
        timeout: Duration?
    ) {
        mutex.withLock {
            logger.debug { "startServe" }
            val intent = Intent(context, SyncthingAndroidService::class.java) // Build the intent for the service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            logger.debug { "Service started" }
            rawController.startServe(waitForWebGui, pollInterval, timeout)
            val serviceConnection: ServiceConnection = object : ServiceConnection {
                override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
                    logger.debug { "Service connected" }
                    val binder = service as SyncthingAndroidService.LocalBinder
                    binder.service.serviceController = this@AndroidSyncthingServiceController
                    context.unbindService(this)
                }

                override fun onServiceDisconnected(arg0: ComponentName?) {
                    logger.debug { "Service disconnected" }
                }
            }
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override suspend fun restartServe(waitForWebGui: Boolean) = rawController.restartServe(waitForWebGui)

    override suspend fun shutdownAndStop(gracefulWaitMs: Long) {
        mutex.withLock {
            rawController.shutdownAndStop(gracefulWaitMs)
            context.stopService(Intent(context, SyncthingAndroidService::class.java))
        }
    }

    override suspend fun runDeviceId(): String? = mutex.withLock {
        val res = rawController.runDeviceId()
        return res
    }

    override suspend fun runGenerate(): Int = mutex.withLock {
        val res = rawController.runGenerate()
        return res
    }

    override suspend fun runResetDatabase(): Int = mutex.withLock {
        val res = rawController.runResetDatabase()
        return res
    }

    override suspend fun runResetDeltas(): Int = mutex.withLock {
        val res = rawController.runResetDeltas()
        return res
    }

    override val configApi: ConfigApi by rawController::configApi

}