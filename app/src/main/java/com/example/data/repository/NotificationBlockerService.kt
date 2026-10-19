package com.example.data.repository

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Removes notifications only after the user explicitly enables Notification Access
 * for this app in Android Settings. Notification content is never read or stored.
 */
class NotificationBlockerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val enabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
        if (enabled && sbn.packageName != packageName) {
            try {
                cancelNotification(sbn.key)
            } catch (_: SecurityException) {
                // Access may be revoked while the service is running.
            }
        }
    }

    companion object {
        const val PREFS_NAME = "notification_blocker"
        const val KEY_ENABLED = "enabled"
    }
}
