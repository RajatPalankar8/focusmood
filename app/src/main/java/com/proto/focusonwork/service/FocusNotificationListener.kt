package com.proto.focusonwork.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Notification integration point; filtering is enabled only during an active session. */
class FocusNotificationListener : NotificationListenerService() {
    private var blockedPackages: Set<String> = emptySet()

    fun updateBlockedPackages(packages: Set<String>) {
        blockedPackages = packages
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName in blockedPackages) {
            cancelNotification(sbn.key)
        }
    }
}
