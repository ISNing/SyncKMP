package moe.isning.synckmp.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import moe.isning.synckmp.config.ConfigXmlParser
import moe.isning.synckmp.http.SyncApi
import java.io.File


actual fun buildDefaultSyncServiceController(
    cfg: SyncProcessConfig,
    api: SyncApi,
): SyncServiceController {
    val controller = JvmSyncServiceController(
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