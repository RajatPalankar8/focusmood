package com.proto.focusonwork

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.ApplicationInfo
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.Button as AndroidButton
import android.widget.FrameLayout
import android.widget.ImageView
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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
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

private data class RestoredSession(
    val active: Boolean,
    val packages: Set<String>,
    val durationMinutes: Int,
    val startedAtMillis: Long,
    val endsAtMillis: Long
)

private const val SESSION_PREFERENCES = "focus_session"
private const val KEY_SESSION_ACTIVE = "session_active"
private const val KEY_SESSION_STARTED_AT = "session_started_at"
private const val KEY_SESSION_ENDS_AT = "session_ends_at"
private const val KEY_SELECTED_DURATION = "selected_duration"
private const val KEY_SELECTED_PACKAGES = "selected_packages"

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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionManager = remember(context) { PermissionManager(context) }
    var usageGranted by remember { mutableStateOf(permissionManager.hasUsageAccess()) }
    var overlayGranted by remember { mutableStateOf(permissionManager.hasOverlayAccess()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val refreshPermissions = {
        usageGranted = permissionManager.hasUsageAccess()
        overlayGranted = permissionManager.hasOverlayAccess()
    }
    val usagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshPermissions()
    }
    val overlayPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshPermissions()
    }
    LaunchedEffect(showPermissionDialog, usageGranted, overlayGranted) {
        if (showPermissionDialog && usageGranted && overlayGranted) {
            delay(900L)
            showPermissionDialog = false
        }
    }
    DisposableEffect(lifecycleOwner, permissionManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val installedApps = remember { loadInstalledApps(context) }
    val sessionLogRepository = remember { SessionLogRepository(context) }
    val completedSessions by sessionLogRepository.completedSessions.collectAsState(initial = emptyList())
    val sessionPreferences = remember(context) {
        context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)
    }
    val restoredSession = remember(sessionPreferences) {
        val endsAt = sessionPreferences.getLong(KEY_SESSION_ENDS_AT, 0L)
        val active = sessionPreferences.getBoolean(KEY_SESSION_ACTIVE, false) && endsAt > System.currentTimeMillis()
        val packages = sessionPreferences.getStringSet(KEY_SELECTED_PACKAGES, emptySet()).orEmpty().toSet()
        RestoredSession(
            active = active && packages.isNotEmpty(),
            packages = packages,
            durationMinutes = sessionPreferences.getInt(KEY_SELECTED_DURATION, 45),
            startedAtMillis = sessionPreferences.getLong(KEY_SESSION_STARTED_AT, 0L),
            endsAtMillis = endsAt
        )
    }
    var selectedDuration by remember { mutableIntStateOf(restoredSession.durationMinutes) }
    val savedPackages = remember(sessionPreferences) {
        sessionPreferences.getStringSet(KEY_SELECTED_PACKAGES, emptySet()).orEmpty().toSet()
    }
    var selectedPackages by remember { mutableStateOf(savedPackages) }
    var lockedPackages by remember { mutableStateOf(if (restoredSession.active) restoredSession.packages else emptySet()) }
    var isSessionActive by remember { mutableStateOf(restoredSession.active) }
    var sessionEndsAtMillis by remember { mutableLongStateOf(if (restoredSession.active) restoredSession.endsAtMillis else 0L) }
    var sessionStartedAtMillis by remember { mutableLongStateOf(if (restoredSession.active) restoredSession.startedAtMillis else 0L) }
    var secondsRemaining by remember { mutableLongStateOf(0L) }
    var showAppPicker by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var pinDraft by remember { mutableStateOf("") }

    LaunchedEffect(isSessionActive, sessionEndsAtMillis) {
        while (isSessionActive) {
            secondsRemaining = remainingSeconds(sessionEndsAtMillis, System.currentTimeMillis())
            if (secondsRemaining <= 0L) {
                val completedBlockedAppCount = lockedPackages.size
                isSessionActive = false
                lockedPackages = emptySet()
                sessionPreferences.edit().putBoolean(KEY_SESSION_ACTIVE, false).apply()
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
    val selectedApps = selectedPackages.size
    val totalSessionSeconds = selectedDuration * 60L

    BackHandler(
        enabled = showHistory ||
            (!showPermissionDialog && !showPinSetup && !showAppPicker && !showExitDialog)
    ) {
        if (showHistory) {
            showHistory = false
        } else {
            showExitDialog = true
        }
    }

    if (showHistory) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { historyPadding ->
            Column(Modifier.fillMaxSize().padding(historyPadding).consumeWindowInsets(historyPadding)) {
                FocusStatsScreen(
                    sessions = completedSessions,
                    protectedAppNames = installedApps.filter { it.packageName in selectedPackages }.map { it.label },
                    onBack = { showHistory = false },
                    modifier = Modifier.weight(1f)
                )
                BannerAd()
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        if (isSessionActive) {
            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                ActiveSessionScreen(
                    secondsRemaining = secondsRemaining,
                    totalSeconds = totalSessionSeconds,
                    blockedApps = lockedPackages.size,
                    onEndSession = {
                        isSessionActive = false
                        lockedPackages = emptySet()
                        sessionPreferences.edit().putBoolean(KEY_SESSION_ACTIVE, false).apply()
                        context.stopService(Intent(context, FocusMonitorService::class.java))
                    },
                    modifier = Modifier.weight(1f)
                )
                BannerAd()
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            DashboardScreen(
                selectedDuration = selectedDuration,
                selectedApps = selectedApps,
                onDurationSelected = {
                    selectedDuration = it
                    sessionPreferences.edit().putInt(KEY_SELECTED_DURATION, it).apply()
                },
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
                            sessionPreferences.edit()
                                .putBoolean(KEY_SESSION_ACTIVE, true)
                                .putLong(KEY_SESSION_STARTED_AT, sessionStartedAtMillis)
                                .putLong(KEY_SESSION_ENDS_AT, sessionEndsAtMillis)
                                .putInt(KEY_SELECTED_DURATION, selectedDuration)
                                .putStringSet(KEY_SELECTED_PACKAGES, lockedPackages)
                                .apply()
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
                modifier = Modifier.weight(1f)
            )
            BannerAd()
            }
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            usageGranted = usageGranted,
            overlayGranted = overlayGranted,
            onOpenUsageAccess = { usagePermissionLauncher.launch(permissionManager.usageAccessIntent()) },
            onOpenOverlayAccess = { overlayPermissionLauncher.launch(permissionManager.overlayAccessIntent()) },
            onDismiss = { showPermissionDialog = false }
        )
    }

    if (showPinSetup) {
        AlertDialog(
            onDismissRequest = { showPinSetup = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Set your emergency PIN", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Keep a private way to end a session in an emergency.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = pinDraft,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pinDraft = it },
                        label = { Text("4-digit PIN") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done)
                    )
                }
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
            onApplySelection = {
                selectedPackages = it.toSet()
                sessionPreferences.edit().putStringSet(KEY_SELECTED_PACKAGES, it.toSet()).apply()
            },
            onDismiss = { showAppPicker = false }
        )
    }

    if (showExitDialog) {
        ExitDialog(
            onDismiss = { showExitDialog = false },
            onExit = { (context as? Activity)?.finish() }
        )
    }
}

@Composable
private fun ExitDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val pulseTransition = rememberInfiniteTransition(label = "exitDialogPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "exitDialogPulseScale"
    )
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 10.dp,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(colors.primaryContainer, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", color = colors.primary, fontSize = 29.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Leaving Focus On Work?",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your focus settings are saved. Come back whenever you’re ready to focus on your work or take back your time from distractions.",
                    color = colors.onSurfaceVariant,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Surface(
                    onClick = {
                        onDismiss()
                        openDeveloperPage(context)
                    },
                    modifier = Modifier.fillMaxWidth().scale(pulseScale),
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.35f)))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("▶", color = colors.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "DISCOVER MORE APPS",
                                fontWeight = FontWeight.Black,
                                color = colors.primary,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.size(6.dp))
                            Box(
                                Modifier.background(colors.primary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("MORE", color = colors.onPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Explore more apps from the Proto Coders Point.",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        Box(
                            Modifier.background(
                                Brush.horizontalGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.72f))),
                                RoundedCornerShape(20.dp)
                            ).padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "VIEW ON GOOGLE PLAY",
                                color = colors.onPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.7.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Enjoying Focus On Work? Rate the app!",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            openAppListing(context)
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Text(
                            text = "★",
                            color = colors.primary,
                            fontSize = 30.sp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("KEEP FOCUSING", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onExit, modifier = Modifier.height(48.dp)) {
                Text("EXIT APP", color = colors.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            }
        }
    )
}

private fun openAppListing(context: Context) {
    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=${context.packageName}")
    )
    try {
        context.startActivity(marketIntent)
    } catch (_: android.content.ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            )
        )
    }
}

private fun openDeveloperPage(context: Context) {
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Palankar.R"))
    try {
        context.startActivity(marketIntent)
    } catch (_: android.content.ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/search?q=Palankar.R&c=apps")
            )
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
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FOCUS ON WORK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Make room for\nwhat matters.", style = MaterialTheme.typography.headlineSmall)
            }
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.semantics { contentDescription = "Open history and analysis" }
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Canvas(Modifier.padding(12.dp).size(22.dp)) {
                        val strokeWidth = size.width * 0.1f
                        val baseline = size.height * 0.86f
                        val axisColor = FocusIndigo
                        drawLine(
                            color = axisColor,
                            start = Offset(size.width * 0.12f, size.height * 0.08f),
                            end = Offset(size.width * 0.12f, baseline),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = axisColor,
                            start = Offset(size.width * 0.12f, baseline),
                            end = Offset(size.width * 0.94f, baseline),
                            strokeWidth = strokeWidth
                        )
                        val points = listOf(
                            Offset(size.width * 0.28f, size.height * 0.64f),
                            Offset(size.width * 0.50f, size.height * 0.43f),
                            Offset(size.width * 0.70f, size.height * 0.53f),
                            Offset(size.width * 0.91f, size.height * 0.20f)
                        )
                        points.zipWithNext().forEach { (start, end) ->
                            drawLine(axisColor, start, end, strokeWidth, cap = StrokeCap.Round)
                        }
                        points.forEach { point ->
                            drawCircle(axisColor, radius = strokeWidth * 1.15f, center = point)
                        }
                    }
                }
            }
        }

        TimerCard(selectedDuration)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Choose your focus", style = MaterialTheme.typography.titleMedium)
                Text("A small commitment is a good start.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (selectedDuration < 60) "${selectedDuration}m" else "${selectedDuration / 60}h", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
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
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("APPS TO PAUSE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("${if (selectedApps == 1) "1 app" else "$selectedApps apps"} selected", style = MaterialTheme.typography.titleLarge)
                    }
                    OutlinedButton(onClick = onSelectApps, shape = RoundedCornerShape(16.dp)) {
                        Text(if (selectedApps == 0) "Choose apps" else "Edit list")
                    }
                }
                if (selectedAppNames.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedAppNames.take(3).forEach { app -> AppPill(app) }
                        if (selectedApps > 3) AppPill("+${selectedApps - 3}")
                    }
                } else {
                    Text("Choose apps that usually pull your attention away.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Emergency PIN", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = onConfigurePin) {
                    Text(if (pinConfigured) "••••  Ready" else "Set up", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Button(
            onClick = onStartFocus,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) { Text("START FOCUS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }

        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<InstalledApp>,
    selectedPackages: Set<String>,
    onApplySelection: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var draftSelection by remember(selectedPackages) { mutableStateOf(selectedPackages.toSet()) }
    var query by remember { mutableStateOf("") }
    val selectionListState = rememberLazyListState()
    val filteredApps = remember(apps, query) {
        apps.filter { it.label.contains(query.trim(), ignoreCase = true) || it.packageName.contains(query.trim(), ignoreCase = true) }
    }
    val selectedApps = remember(apps, draftSelection) { apps.filter { it.packageName in draftSelection } }
    val appListState = rememberLazyListState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Choose your distractions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Pick the apps you want to pause during focus.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 600.dp).imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text("Search apps") },
                    leadingIcon = { Text("⌕", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary) },
                    trailingIcon = if (query.isNotEmpty()) ({
                        IconButton(onClick = { query = "" }) { Text("×", fontSize = 22.sp) }
                    }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { })
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${draftSelection.size} selected", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Changes apply when you tap Save", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f))
                        }
                        if (draftSelection.isNotEmpty()) TextButton(onClick = { draftSelection = emptySet() }) { Text("Clear all") }
                    }
                }
                if (selectedApps.isNotEmpty()) {
                    LazyRow(state = selectionListState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedApps, key = { "selected-${it.packageName}" }) { app ->
                            Surface(
                                onClick = { draftSelection = draftSelection - app.packageName },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.semantics { contentDescription = "Remove ${app.label} from selected apps" }
                            ) {
                                Row(Modifier.padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    AppIcon(icon = app.icon, size = 24.dp)
                                    Text(app.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text("×", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                if (apps.isEmpty()) {
                    PickerEmptyState("No launchable apps found", "Install an app with a launcher icon and it will appear here.")
                } else if (filteredApps.isEmpty()) {
                    PickerEmptyState("No matching apps", "Try another app name or package name.")
                } else {
                    Text("INSTALLED APPS  ·  ${filteredApps.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyColumn(state = appListState, modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isSelected = app.packageName in draftSelection
                            Surface(
                                onClick = {
                                    draftSelection = if (isSelected) draftSelection - app.packageName else draftSelection + app.packageName
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                                modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                                    contentDescription = "${app.label}, ${if (isSelected) "selected" else "not selected"}"
                                }
                            ) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AppIcon(icon = app.icon, size = 42.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(app.label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1)
                                        Text(if (isSelected) "Added to focus list" else "Tap to add", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Checkbox(checked = isSelected, onCheckedChange = { checked ->
                                        draftSelection = if (checked) draftSelection + app.packageName else draftSelection - app.packageName
                                    })
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApplySelection(draftSelection); onDismiss() }, shape = RoundedCornerShape(18.dp)) {
                Text("Save ${draftSelection.size} apps")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PickerEmptyState(title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("✦", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AppIcon(icon: Drawable, size: Dp = 40.dp) {
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
        modifier = Modifier.size(size).clip(RoundedCornerShape(10.dp))
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
                Text(String.format("%02d:%02d:00", minutes / 60, minutes % 60), fontSize = 42.sp, fontWeight = FontWeight.Light, color = Color.White)
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
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF16865C)))
                Text("  FOCUS IS RUNNING", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
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
private fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(50.dp),
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
    LaunchedEffect(context) {
        AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {})
            .build()
            .loadAd(AdRequest.Builder().build())
    }
    DisposableEffect(Unit) {
        onDispose { nativeAd?.destroy() }
    }
    nativeAd?.let { ad ->
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(132.dp).padding(horizontal = 12.dp),
            factory = { viewContext ->
                val density = viewContext.resources.displayMetrics.density
                fun dp(value: Int) = (value * density).toInt()

                NativeAdView(viewContext).apply {
                    val card = LinearLayout(viewContext).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(14), dp(10), dp(14), dp(10))
                        background = GradientDrawable().apply {
                            setColor(AndroidColor.WHITE)
                            cornerRadius = dp(18).toFloat()
                        }
                    }
                    val adLabel = TextView(viewContext).apply {
                        text = "Ad"
                        textSize = 11f
                        setTextColor(AndroidColor.rgb(75, 65, 190))
                        setPadding(dp(6), dp(2), dp(6), dp(2))
                        background = GradientDrawable().apply {
                            setColor(AndroidColor.rgb(238, 235, 255))
                            cornerRadius = dp(5).toFloat()
                        }
                    }
                    val row = LinearLayout(viewContext).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    val icon = ImageView(viewContext).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    val copy = LinearLayout(viewContext).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(10), 0, dp(8), 0)
                    }
                    val headline = TextView(viewContext).apply {
                        textSize = 15f
                        setTextColor(AndroidColor.rgb(28, 27, 36))
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }
                    val body = TextView(viewContext).apply {
                        textSize = 12f
                        setTextColor(AndroidColor.rgb(95, 93, 105))
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }
                    val callToAction = AndroidButton(viewContext).apply {
                        textSize = 12f
                        isAllCaps = false
                        minHeight = dp(40)
                        setPadding(dp(12), 0, dp(12), 0)
                    }
                    copy.addView(headline, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                    copy.addView(body, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dp(4)
                    })
                    row.addView(icon, LinearLayout.LayoutParams(dp(44), dp(44)))
                    row.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                    row.addView(callToAction, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)))
                    card.addView(adLabel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                    card.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                        topMargin = dp(8)
                    })
                    addView(card, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                    iconView = icon
                    headlineView = headline
                    bodyView = body
                    callToActionView = callToAction
                }
            },
            update = { view ->
                (view.iconView as? ImageView)?.setImageDrawable(ad.icon?.drawable)
                (view.headlineView as? TextView)?.text = ad.headline
                (view.bodyView as? TextView)?.text = ad.body.orEmpty()
                (view.callToActionView as? AndroidButton)?.text = ad.callToAction.orEmpty()
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