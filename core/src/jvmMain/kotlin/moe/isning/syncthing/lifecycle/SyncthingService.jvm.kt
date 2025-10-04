package moe.isning.syncthing.lifecycle

import kotlinx.coroutines.CoroutineScope
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi


class JvmSyncthingServiceController(
    cfg: SyncthingProcessConfig,
    scope: CoroutineScope,
    api: SyncthingApi,
    configXmlParser: ConfigXmlParser,
    notificationSink: NotificationSink = NoopNotificationSink,
) : SyncthingServiceController by RawSyncthingServiceController(cfg, scope, api, configXmlParser, notificationSink)
