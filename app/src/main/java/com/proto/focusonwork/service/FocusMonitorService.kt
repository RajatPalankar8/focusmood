package com.proto.focusonwork.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.proto.focusonwork.R
import com.proto.focusonwork.presentation.blocker.BlockerActivity
import com.proto.focusonwork.system.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusMonitorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorDispatcher)
    private var monitorJob: Job? = null
    private var blockedPackages: Set<String> = emptySet()
    private var lastForegroundPackage: String? = null
    private var lastEventTime = 0L
    private var lastEndTimeMillis = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        blockedPackages = intent?.getStringArrayListExtra(EXTRA_BLOCKED_PACKAGES)?.toSet().orEmpty()
        lastEndTimeMillis = intent?.getLongExtra(EXTRA_ENDS_AT, 0L) ?: 0L
        monitorJob?.cancel()

        if (blockedPackages.isEmpty() || !PermissionManager(this).hasUsageAccess()) {
            stopSelf()
            return START_NOT_STICKY
        }

        monitorJob = serviceScope.launch {
            while (isActive) {
                pollForegroundApp()
                updateNotification()
                delay(POLL_INTERVAL_MILLIS)
            }
        }
        return START_STICKY
    }

    private fun pollForegroundApp() {
        val now = System.currentTimeMillis()
        val events = getSystemService(UsageStatsManager::class.java)
            .queryEvents(now - POLL_WINDOW_MILLIS, now)
        val event = UsageEvents.Event()
        var newestPackage: String? = null
        var newestTime = lastEventTime

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && event.timeStamp > newestTime) {
                newestPackage = event.packageName
                newestTime = event.timeStamp
            }
        }

        lastEventTime = newestTime
        if (newestPackage != null && newestPackage != lastForegroundPackage) {
            lastForegroundPackage = newestPackage
            if (newestPackage in blockedPackages) showBlocker(newestPackage)
        }
    }

    private fun showBlocker(packageName: String) {
        startActivity(Intent(this, BlockerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockerActivity.EXTRA_BLOCKED_PACKAGE, packageName)
        })
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Focus protection", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun updateNotification() {
        val endsAt = lastEndTimeMillis
        val remaining = if (endsAt > 0L) {
            ((endsAt - System.currentTimeMillis()).coerceAtLeast(0L) / 1_000L)
        } else 0L
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(remaining))
    }

    private fun notification(remainingSeconds: Long = 0L): Notification {
        val minutes = remainingSeconds / 60L
        val seconds = remainingSeconds % 60L
        val text = if (remainingSeconds > 0L) {
            "Focus running • %02d:%02d remaining".format(minutes, seconds)
        } else {
            "Focus protection is active"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Focus On Work")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private val SupervisorDispatcher = Dispatchers.Default
        const val EXTRA_BLOCKED_PACKAGES = "blocked_packages"
        const val EXTRA_ENDS_AT = "ends_at"
        const val EXTRA_TOTAL_SECONDS = "total_seconds"
        const val CHANNEL_ID = "focus_monitor"
        const val NOTIFICATION_ID = 1002
        private const val POLL_WINDOW_MILLIS = 2_000L
        private const val POLL_INTERVAL_MILLIS = 300L
    }
}
