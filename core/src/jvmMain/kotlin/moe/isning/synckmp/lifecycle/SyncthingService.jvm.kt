package moe.isning.synckmp.lifecycle

import kotlinx.coroutines.CoroutineScope
import moe.isning.synckmp.config.ConfigXmlParser
import moe.isning.synckmp.http.SyncApi


class JvmSyncServiceController(
    cfg: SyncProcessConfig,
    scope: CoroutineScope,
    api: SyncApi,
    configXmlParser: ConfigXmlParser,
    notificationSink: NotificationSink = NoopNotificationSink,
) : SyncServiceController by RawSyncServiceController(cfg, scope, api, configXmlParser, notificationSink)
