package moe.isning.syncthing.lifecycle

import kotlinx.coroutines.CoroutineScope

class IosSyncthingProcessRunner(
    private val scope: CoroutineScope,
    private val notificationSink: NotificationSink = NoopNotificationSink,
) : SyncthingProcessRunner {
    override val isRunning: Boolean
        get() = false

    override suspend fun start(command: SyncthingCommand, cfg: SyncthingProcessConfig): Int? {
        notificationSink.onCrashed(-1)
        return null
    }

    override suspend fun awaitExit(): Int {
        return 0
    }

    override suspend fun stop(graceful: Boolean, killDelayMs: Long): Int? {
        return null
    }
}

actual fun buildSyncthingProcessRunner(
    scope: CoroutineScope,
    notificationSink: NotificationSink
): SyncthingProcessRunner = IosSyncthingProcessRunner(scope, notificationSink)

