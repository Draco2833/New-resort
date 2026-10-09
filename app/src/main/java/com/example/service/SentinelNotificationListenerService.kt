package com.example.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SentinelNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("SentinelDRP", "SentinelNotificationListenerService connected and monitoring.")
        NotificationShieldManager.setServiceActive(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i("SentinelDRP", "SentinelNotificationListenerService disconnected.")
        NotificationShieldManager.setServiceActive(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.notification == null) return

        val packageName = sbn.packageName ?: "unknown"
        // Avoid self-inspection loop
        if (packageName == this.packageName) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val bodyText = if (bigText.isNotBlank()) bigText else text
        if (title.isBlank() && bodyText.isBlank()) return

        val appName = try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }

        // Run deep on-device security & impersonation inspection
        NotificationShieldManager.inspectAndLog(
            packageName = packageName,
            appName = appName,
            title = title,
            bodyText = bodyText
        )
    }
}
