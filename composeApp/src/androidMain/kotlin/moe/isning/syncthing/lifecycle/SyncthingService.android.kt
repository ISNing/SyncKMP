package moe.isning.syncthing.lifecycle

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import moe.isning.syncthing.config.ConfigApi
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi
import org.koin.compose.koinInject
import java.io.File
import kotlin.time.Duration


@Composable
actual fun buildDefaultSyncthingServiceController(
    cfg: SyncthingProcessConfig,
): SyncthingServiceController {
    val context = LocalContext.current.applicationContext

    return AndroidSyncthingServiceController(
        context,
        cfg,
        CoroutineScope(SupervisorJob()),
        SyncthingApi(koinInject(), "http://localhost:8384"),
        ConfigXmlParser(File(cfg.configPath)),
        NoopNotificationSink, //FIXME: Implement Android notifications
    )
}

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

    override val isRunning: Boolean by rawController::isRunning
    override suspend fun startServe(
        waitForWebGui: Boolean,
        pollInterval: Duration,
        timeout: Duration?
    ) {
        val intent = Intent() // Build the intent for the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        rawController.startServe(waitForWebGui, pollInterval, timeout)
        val serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
                val binder = service as SyncthingService.LocalBinder
                binder.service.serviceController = this@AndroidSyncthingServiceController
                context.unbindService(this)
            }

            override fun onServiceDisconnected(arg0: ComponentName?) {
            }
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    override suspend fun restartServe(waitForWebGui: Boolean) = rawController.restartServe(waitForWebGui)

    override suspend fun shutdownAndStop(gracefulWaitMs: Long) {
        rawController.shutdownAndStop(gracefulWaitMs)
    }

    override suspend fun runDeviceId(): String? {
        val res = rawController.runDeviceId()
        return res
    }

    override suspend fun runGenerate(): Int {
        val res = rawController.runGenerate()
        return res
    }

    override suspend fun runResetDatabase(): Int {
        val res = rawController.runResetDatabase()
        return res
    }

    override suspend fun runResetDeltas(): Int {
        val res = rawController.runResetDeltas()
        return res
    }

    override val configApi: ConfigApi by rawController::configApi

}