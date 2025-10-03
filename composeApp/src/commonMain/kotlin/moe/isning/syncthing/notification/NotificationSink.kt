package moe.isning.lifecycle

/**
 * Platform-specific notification sink for Syncthing event processing.
 * Implement this in androidMain to display notifications.
 */
interface NotificationSink {
    /**
     * Update or show the persistent foreground notification.
     * @param state Current Syncthing service state (e.g., ACTIVE, STARTING, DISABLED, ERROR)
     * @param onlineDeviceCount Number of online devices
     * @param totalSyncCompletion Sync completion percent (0-100, or -1 if unknown)
     */
    fun updatePersistentNotification(state: String, onlineDeviceCount: Int, totalSyncCompletion: Int)

    /**
     * Show a consent notification with accept/ignore actions.
     * @param notificationId Deterministic notification ID
     * @param text Notification text
     */
    fun showConsentNotification(notificationId: Int, text: String)

    /**
     * Show a crash notification.
     * @param title Notification title
     * @param extraInfo Additional info (e.g., stack trace)
     */
    fun showCrashNotification(title: String, extraInfo: String)

    /**
     * Cancel a notification by ID.
     * @param notificationId The notification ID to cancel
     */
    fun cancelNotification(notificationId: Int)
}

