package com.proto.focusonwork

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.proto.focusonwork.security.PinManager
import com.proto.focusonwork.system.PermissionManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Checkbox
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.geometry.Offset
import com.proto.focusonwork.ui.theme.FocusAmber
import com.proto.focusonwork.ui.theme.FocusIndigo
import com.proto.focusonwork.ui.theme.FocusLavender
import com.proto.focusonwork.ui.theme.FocusOnWorkTheme
import com.proto.focusonwork.ui.theme.FocusPink
import com.proto.focusonwork.ui.theme.FocusViolet
import com.proto.focusonwork.data.local.SessionLogRepository
import com.proto.focusonwork.presentation.session.formatRemaining
import com.proto.focusonwork.presentation.session.remainingSeconds
import com.proto.focusonwork.presentation.stats.FocusStatsScreen
import com.proto.focusonwork.service.FocusMonitorService
import com.proto.focusonwork.presentation.onboarding.PermissionDialog
import kotlinx.coroutines.delay

data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

private fun loadInstalledApps(context: Context): List<InstalledApp> {
    val packageManager = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
        .map { it.activityInfo.applicationInfo }
        .distinctBy { it.packageName }
        .filter { it.packageName != context.packageName }
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        .map {
            InstalledApp(
                label = packageManager.getApplicationLabel(it).toString(),
                packageName = it.packageName,
                icon = packageManager.getApplicationIcon(it)
            )
        }
        .sortedBy { it.label.lowercase() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusOnWorkTheme {
                FocusOnWorkApp()
            }
        }
    }
}

@Composable
fun FocusOnWorkApp() {
    val context = LocalContext.current
    val installedApps = remember { loadInstalledApps(context) }
    val sessionLogRepository = remember { SessionLogRepository(context) }
    val completedSessions by sessionLogRepository.completedSessions.collectAsState(initial = emptyList())
    var selectedDuration by remember { mutableIntStateOf(45) }
    var selectedPackages by remember { mutableStateOf(setOf<String>()) }
    var lockedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isSessionActive by remember { mutableStateOf(false) }
    var sessionEndsAtMillis by remember { mutableLongStateOf(0L) }
    var sessionStartedAtMillis by remember { mutableLongStateOf(0L) }
    var secondsRemaining by remember { mutableLongStateOf(0L) }
    var showAppPicker by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    LaunchedEffect(isSessionActive, sessionEndsAtMillis) {
        while (isSessionActive) {
            secondsRemaining = remainingSeconds(sessionEndsAtMillis, System.currentTimeMillis())
            if (secondsRemaining <= 0L) {
                val completedBlockedAppCount = lockedPackages.size
                isSessionActive = false
                lockedPackages = emptySet()
                context.stopService(Intent(context, FocusMonitorService::class.java))
                sessionLogRepository.recordCompletedSession(
                    startedAtMillis = sessionStartedAtMillis,
                    endedAtMillis = sessionEndsAtMillis,
                    durationMinutes = selectedDuration,
                    blockedAppCount = completedBlockedAppCount
                )
                break
            }
            delay(1_000L)
        }
    }
    var showPinSetup by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pinDraft by remember { mutableStateOf("") }

    val selectedApps = selectedPackages.size
    val totalSessionSeconds = selectedDuration * 60L

    if (showHistory) {
        Column(Modifier.fillMaxSize()) {
            FocusStatsScreen(
                sessions = completedSessions,
                protectedAppNames = installedApps.filter { it.packageName in selectedPackages }.map { it.label },
                onBack = { showHistory = false },
                modifier = Modifier.weight(1f)
            )
            BannerAd()
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isSessionActive) {
            ActiveSessionScreen(
                secondsRemaining = secondsRemaining,
                totalSeconds = totalSessionSeconds,
                blockedApps = lockedPackages.size,
                onEndSession = {
                    isSessionActive = false
                    lockedPackages = emptySet()
                    context.stopService(Intent(context, FocusMonitorService::class.java))
                },
                modifier = Modifier.padding(padding)
            )
            BannerAd()
        } else {
            DashboardScreen(
                selectedDuration = selectedDuration,
                selectedApps = selectedApps,
                onDurationSelected = { selectedDuration = it },
                onSelectApps = { if (!isSessionActive) showAppPicker = true },
                onStartFocus = {
                    val pinManager = PinManager(context)
                    val permissions = PermissionManager(context)
                    when {
                        !pinManager.hasPin() -> {
                            // A session can never begin without an emergency PIN.
                            showPinSetup = true
                        }
                        selectedPackages.isEmpty() -> {
                            showAppPicker = true
                        }
                        !permissions.hasUsageAccess() || !permissions.hasOverlayAccess() -> {
                            showPermissionDialog = true
                        }
                        else -> {
                            lockedPackages = selectedPackages.toSet()
                            sessionStartedAtMillis = System.currentTimeMillis()
                            sessionEndsAtMillis = sessionStartedAtMillis + selectedDuration * 60_000L
                            secondsRemaining = selectedDuration * 60L
                            isSessionActive = true
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(context as ComponentActivity, arrayOf(
                                    Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
                            }
                            ContextCompat.startForegroundService(
                                context,
                                Intent(context, FocusMonitorService::class.java).apply {
                                    putStringArrayListExtra(
                                        FocusMonitorService.EXTRA_BLOCKED_PACKAGES,
                                        ArrayList(lockedPackages)
                                    )
                                    putExtra(FocusMonitorService.EXTRA_ENDS_AT, sessionEndsAtMillis)
                                    putExtra(FocusMonitorService.EXTRA_TOTAL_SECONDS, selectedDuration * 60L)
                                }
                            )
                        }
                    }
                },
                selectedAppNames = installedApps.filter { it.packageName in selectedPackages }.map { it.label },
                pinConfigured = PinManager(context).hasPin(),
                onConfigurePin = { showPinSetup = true },
                onOpenHistory = { showHistory = true },
                modifier = Modifier.padding(padding)
            )
            BannerAd()
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            usageGranted = PermissionManager(context).hasUsageAccess(),
            overlayGranted = PermissionManager(context).hasOverlayAccess(),
            onOpenUsageAccess = { context.startActivity(PermissionManager(context).usageAccessIntent()) },
            onOpenOverlayAccess = { context.startActivity(PermissionManager(context).overlayAccessIntent()) },
            onDismiss = { showPermissionDialog = false }
        )
    }

    if (showPinSetup) {
        AlertDialog(
            onDismissRequest = { showPinSetup = false },
            title = { Text("Set emergency PIN") },
            text = {
                OutlinedTextField(
                    value = pinDraft,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pinDraft = it },
                    label = { Text("4-digit PIN") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        PinManager(context).savePin(pinDraft)
                        pinDraft = ""
                        showPinSetup = false
                    },
                    enabled = pinDraft.length == 4
                ) { Text("Save PIN") }
            },
            dismissButton = { TextButton(onClick = { showPinSetup = false }) { Text("Cancel") } }
        )
    }

    if (showAppPicker) {
        AppPickerDialog(
            apps = installedApps,
            selectedPackages = selectedPackages,
            onToggle = { packageName ->
                if (!isSessionActive) {
                    selectedPackages = if (packageName in selectedPackages) selectedPackages - packageName else selectedPackages + packageName
                }
            },
            onDismiss = { showAppPicker = false }
        )
    }
}

@Composable
private fun DashboardScreen(
    selectedDuration: Int,
    selectedApps: Int,
    selectedAppNames: List<String>,
    onDurationSelected: (Int) -> Unit,
    onSelectApps: () -> Unit,
    onStartFocus: () -> Unit,
    pinConfigured: Boolean,
    onConfigurePin: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("FOCUS ON WORK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Reclaim your focus", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onOpenHistory) { Text("▥", fontSize = 24.sp, color = FocusIndigo) }
        }

        TimerCard(selectedDuration)

        Text("Focus duration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(2, 5, 10, 15, 25, 45, 60, 120)) { minutes ->
                FilterChip(
                    selected = selectedDuration == minutes,
                    onClick = { onDurationSelected(minutes) },
                    label = { Text(if (minutes >= 60) "${minutes / 60}h" else "${minutes}m") }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = FocusLavender)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("APPS TO BLOCK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("$selectedApps apps selected", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onSelectApps) { Text("Select") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedAppNames.take(3).forEach { app -> AppPill(app) }
                    if (selectedApps > 3) AppPill("+${selectedApps - 3}")
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Emergency PIN", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onConfigurePin) {
                Text(if (pinConfigured) "••••  Configured" else "Set PIN", color = MaterialTheme.colorScheme.primary)
            }
        }

        Button(
            onClick = onStartFocus,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) { Text("START FOCUS MODE", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.weight(1f))
        Text("Today's focus  ·  2h 45m     •     4 day streak", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<InstalledApp>,
    selectedPackages: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredApps = apps.filter { it.label.contains(query, ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select apps to block") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search installed apps") }
                )
                Spacer(Modifier.height(12.dp))
                Text("${selectedPackages.size} selected", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) {
                            Checkbox(
                                checked = app.packageName in selectedPackages,
                                onCheckedChange = { onToggle(app.packageName) }
                            )
                            AppIcon(icon = app.icon)
                            Text(
                                app.label,
                                modifier = Modifier.weight(1f).padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Save selection") } }
    )
}

@Composable
private fun AppIcon(icon: Drawable) {
    val drawable = remember(icon) {
        val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)
        bitmap.asImageBitmap()
    }
    Image(
        bitmap = drawable,
        contentDescription = "App icon",
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
    )
}

@Composable
private fun TimerCard(minutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(FocusIndigo, FocusViolet, FocusPink)
                    )
                )
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(Color.White.copy(alpha = .10f), radius = 170f, center = Offset(size.width * .92f, size.height * .08f))
                drawCircle(FocusAmber.copy(alpha = .25f), radius = 90f, center = Offset(size.width * .10f, size.height * .92f))
            }
            Column(
                Modifier.padding(vertical = 30.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(54.dp).clip(CircleShape).background(Color.White.copy(alpha = .18f)),
                    contentAlignment = Alignment.Center
                ) { Text("✦", color = Color.White, fontSize = 28.sp) }
                Spacer(Modifier.height(10.dp))
                Text("READY TO FOCUS", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .9f), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(String.format("%02d : 00 : 00", minutes), fontSize = 42.sp, fontWeight = FontWeight.Light, color = Color.White)
                Text("A quiet space for meaningful work", color = Color.White.copy(alpha = .78f))
            }
        }
    }
}

@Composable
private fun AppPill(name: String) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = .8f)) {
        Text(name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = FocusIndigo)
    }
}

@Composable
private fun ActiveSessionScreen(
    secondsRemaining: Long,
    totalSeconds: Long,
    blockedApps: Int,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = CircleShape, color = Color(0xFFDCFCE7)) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                Text("  FOCUS IS RUNNING", color = Color(0xFF166534), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(290.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color(0xFFE0E7FF), style = Stroke(width = 24f))
                drawArc(
                    brush = Brush.sweepGradient(listOf(FocusIndigo, FocusViolet, FocusPink)),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatRemaining(secondsRemaining), fontSize = 38.sp, fontWeight = FontWeight.Bold, color = FocusIndigo)
                Text("remaining", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Your attention is protected", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("$blockedApps apps silenced", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(36.dp))
        TextButton(onClick = onEndSession) { Text("End session") }
        NativeAdBanner()
    }
}

@Composable
private fun BannerAd() {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { it.resume() }
    )
}

@Composable
private fun NativeAdBanner() {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad -> nativeAd?.destroy(); nativeAd = ad }
            .withAdListener(object : AdListener() {})
            .build()
            .loadAd(AdRequest.Builder().build())
    }
    DisposableEffect(nativeAd) {
        onDispose { nativeAd?.destroy() }
    }
    nativeAd?.let { ad ->
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(92.dp).padding(horizontal = 12.dp),
            factory = { viewContext ->
                NativeAdView(viewContext).apply {
                    val content = LinearLayout(viewContext).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(16, 8, 16, 8)
                        setBackgroundColor(AndroidColor.argb(18, 70, 70, 90))
                    }
                    val headline = TextView(viewContext).apply { textSize = 16f; setTextColor(AndroidColor.BLACK) }
                    val body = TextView(viewContext).apply { textSize = 12f; setTextColor(AndroidColor.DKGRAY) }
                    content.addView(headline)
                    content.addView(body)
                    addView(content, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                    headlineView = headline
                    bodyView = body
                }
            },
            update = { view ->
                (view.headlineView as? TextView)?.text = ad.headline
                (view.bodyView as? TextView)?.text = ad.body
                view.setNativeAd(ad)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusOnWorkPreview() {
    FocusOnWorkTheme { FocusOnWorkApp() }
}

private const val NOTIFICATION_PERMISSION_REQUEST = 7001