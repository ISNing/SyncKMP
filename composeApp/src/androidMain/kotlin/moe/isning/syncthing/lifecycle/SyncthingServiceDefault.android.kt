package moe.isning.syncthing.lifecycle

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi
import org.koin.java.KoinJavaComponent.inject
import java.io.File


actual fun buildDefaultSyncthingServiceController(
    cfg: SyncthingProcessConfig,
    api: SyncthingApi,
): SyncthingServiceController {
    val context : Context by inject(Context::class.java)

    return AndroidSyncthingServiceController(
        context,
        cfg,
        CoroutineScope(SupervisorJob()),
        api,
        ConfigXmlParser(File(cfg.configPath)),
        NoopNotificationSink, //FIXME: Implement Android notifications
    )
}
