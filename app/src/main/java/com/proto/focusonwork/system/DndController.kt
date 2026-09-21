package com.proto.focusonwork.system

import android.app.NotificationManager
import android.content.Context

class DndController(context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private var originalFilter: Int? = null

    val hasPolicyAccess: Boolean
        get() = notificationManager.isNotificationPolicyAccessGranted

    fun startFocusPolicy() {
        if (!hasPolicyAccess) return
        if (originalFilter == null) originalFilter = notificationManager.currentInterruptionFilter
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    fun restoreOriginalPolicy() {
        val previous = originalFilter ?: return
        if (hasPolicyAccess) notificationManager.setInterruptionFilter(previous)
        originalFilter = null
    }
}
