package moe.isning.syncthing.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi
import java.io.File


actual fun buildDefaultSyncthingServiceController(
    cfg: SyncthingProcessConfig,
    api: SyncthingApi,
): SyncthingServiceController {
    val controller = JvmSyncthingServiceController(
        cfg,
        CoroutineScope(SupervisorJob()),
        api,
        ConfigXmlParser(File(cfg.configPath)),
        NoopNotificationSink
    ) // FIXME: Implement notifications

    // 应用退出时优雅停止 Syncthing 进程，避免残留导致的异常
    Runtime.getRuntime().addShutdownHook(Thread {
        runCatching {
            runBlocking { controller.shutdownAndStop(gracefulWaitMs = 2000) }
        }
    })

    return controller
}