package moe.isning.synckmp.lifecycle

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import moe.isning.synckmp.config.ConfigXmlParser
import moe.isning.synckmp.http.SyncApi
import org.koin.java.KoinJavaComponent.inject
import java.io.File


actual fun buildDefaultSyncServiceController(
    cfg: SyncProcessConfig,
    api: SyncApi,
): SyncServiceController {
    val context : Context by inject(Context::class.java)

    return AndroidSyncServiceController(
        context,
        cfg,
        CoroutineScope(SupervisorJob()),
        api,
        ConfigXmlParser(File(cfg.configPath)),
        NoopNotificationSink, //FIXME: Implement Android notifications
    )
}
