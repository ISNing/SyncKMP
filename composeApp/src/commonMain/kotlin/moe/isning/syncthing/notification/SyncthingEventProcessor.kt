package moe.isning.syncthing.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moe.isning.lifecycle.NotificationSink
import moe.isning.syncthing.http.SyncthingApi
import moe.isning.syncthing.http.SyncthingEvent

/**
 * Processes Syncthing events and dispatches notifications via NotificationSink.
 * Call [start] to begin processing; [stop] to cancel.
 */
class SyncthingEventProcessor(
  private val api: SyncthingApi,
  private val notificationSink: NotificationSink,
  private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun start(startId: Long = 0L) {
        job?.cancel()
        job = scope.launch {
            api.streamEvents(startId).collect { event ->
                handleEvent(event)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun handleEvent(event: SyncthingEvent) {
        when (event.type) {
            "ConfigSaved" -> { /* Optionally trigger config reload */ }
            "DeviceConnected", "DeviceDisconnected", "DevicePaused", "DeviceResumed" -> {
                // Update persistent notification (device state change)
                notificationSink.updatePersistentNotification("ACTIVE", 0, -1) // TODO: pass real values
            }
            "FolderCompletion", "FolderErrors", "ItemFinished" -> {
                // Show consent or info notification if needed
                notificationSink.showConsentNotification(
                    notificationId = event.id.toInt(),
                    text = "Event: ${event.type} - ${event.data}"
                )
            }
            "StateChanged" -> {
                // Update persistent notification (folder state change)
                notificationSink.updatePersistentNotification("ACTIVE", 0, -1) // TODO: pass real values
            }
            "Ping" -> { /* Ignore */ }
            else -> { /* Handle or ignore other events as needed */ }
        }
    }
}