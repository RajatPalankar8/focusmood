package com.proto.focusonwork.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.app.AppOpsManager
import android.os.Process

class PermissionManager(private val context: Context) {
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        return appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayAccess(): Boolean = Settings.canDrawOverlays(context)

    fun usageAccessIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun overlayAccessIntent(): Intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )

    fun notificationListenerIntent(): Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

    fun dndAccessIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
}
