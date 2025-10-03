package moe.isning.syncthing.lifecycle

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat


/**
 * Holds the native syncthing instance and provides an API to access it.
 */
class SyncthingService : Service() {
    inner class LocalBinder : Binder() {
        val service: SyncthingService
            get() = this@SyncthingService
    }

    private val binder: LocalBinder = LocalBinder()
    var serviceController: SyncthingServiceController? = null
        set(value) {
            if (field == null) {
                field = value
            } else {
                throw IllegalStateException("serviceController is already set")
            }
        }

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onBind(intent: Intent?): LocalBinder {
        return binder
    }

    private fun startForeground() {
        try {
            val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("SyncKMP")
                .setContentText("SyncKMP is running")//FIXME: Use string resources, dynamic content
                // Create the notification to display while the service is running
                .build()
            ServiceCompat.startForeground(
                /* service = */ this,
                /* id = */ 100, // Cannot be 0
                /* notification = */ notification,
                /* foregroundServiceType = */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                },
            )
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
                // FIXME: Handle this error
            }
        }
    }
}
