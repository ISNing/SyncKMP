package moe.isning.syncthing.lifecycle

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import moe.isning.syncthing.config.ConfigXmlParser
import moe.isning.syncthing.http.SyncthingApi
import java.io.File


class JvmSyncthingServiceController(
    cfg: SyncthingProcessConfig,
    scope: CoroutineScope,
    api: SyncthingApi,
    configXmlParser: ConfigXmlParser,
    notificationSink: NotificationSink = NoopNotificationSink,
) : SyncthingServiceController by RawSyncthingServiceController(cfg, scope, api, configXmlParser, notificationSink)

@Composable
actual fun buildDefaultSyncthingServiceController(
    cfg: SyncthingProcessConfig,
): SyncthingServiceController = JvmSyncthingServiceController(cfg, CoroutineScope(SupervisorJob()),
    SyncthingApi(HttpClient(), "http://localhost:8384"),
    ConfigXmlParser(File("config.xml")), NoopNotificationSink)