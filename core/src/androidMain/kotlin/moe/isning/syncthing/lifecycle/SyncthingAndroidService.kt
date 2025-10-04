package moe.isning.syncthing.lifecycle

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import io.github.oshai.kotlinlogging.KotlinLogging


private val logger = KotlinLogging.logger {}
/**
 * Holds the native syncthing instance and provides an API to access it.
 */
class SyncthingAndroidService : Service() {
    inner class LocalBinder : Binder() {
        val service: SyncthingAndroidService
            get() = this@SyncthingAndroidService
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

    private var notification: Notification? = null
    private val notificationId: Int get() = 100

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onBind(intent: Intent?): LocalBinder {
        logger.debug { "onBind" }
        return binder
    }

    override fun onDestroy() {
        logger.debug { "onDestroy" }
        super.onDestroy()
    }

    private fun startForeground() {
        try {
            logger.debug { "startForeground" }
            val notification = buildForegroundServiceNotification(this)
            this.notification = notification
            ServiceCompat.startForeground(
                /* service = */ this,
                /* id = */ notificationId, // Cannot be 0
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
                logger.error(e) { "App not in a valid state to start foreground service" }
            }
        }
    }
    private fun stopForeground() {
        logger.debug { "stopForeground" }
        notification?.let {
            NotificationManagerCompat.from(this).cancel(notificationId)
        }
        notification = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
}