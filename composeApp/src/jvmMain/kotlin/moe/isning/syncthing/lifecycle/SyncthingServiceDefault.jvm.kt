package moe.isning.syncthing.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi
import java.io.File


actual fun buildDefaultSyncthingServiceController(
    cfg: SyncthingProcessConfig,
    api: SyncthingApi,
): SyncthingServiceController = JvmSyncthingServiceController(cfg, CoroutineScope(SupervisorJob()),
    api,
    ConfigXmlParser(File(cfg.configPath)), NoopNotificationSink)//FIXME: Implement notifications