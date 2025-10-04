package moe.isning.syncthing.lifecycle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

object ForegroundServiceNotificationChannel {
    val channelId = "foreground_service_channel"
    val name = "Foreground Service"
    val descriptionText = "Foreground Service"
    val importance = NotificationManager.IMPORTANCE_LOW
}

@RequiresApi(Build.VERSION_CODES.O)
fun createForegroundServiceNotificationChannel(context: Context) {
    ForegroundServiceNotificationChannel.apply {
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = descriptionText
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun buildForegroundServiceNotification(context: Context) =
    NotificationCompat.Builder(context, ForegroundServiceNotificationChannel.channelId)
        .setContentTitle("SyncKMP")
        .setContentText("SyncKMP is running")//FIXME: Use string resources, dynamic content
        .build()
        .also { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createForegroundServiceNotificationChannel(context) }