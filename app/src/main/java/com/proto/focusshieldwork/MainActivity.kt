package com.proto.focusshieldwork

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
import com.proto.focusshieldwork.security.PinManager
import com.proto.focusshieldwork.system.PermissionManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.proto.focusshieldwork.ui.theme.FocusAmber
import com.proto.focusshieldwork.ui.theme.FocusIndigo
import com.proto.focusshieldwork.ui.theme.FocusOnWorkTheme
import com.proto.focusshieldwork.ui.theme.FocusPink
import com.proto.focusshieldwork.ui.theme.FocusViolet
import com.proto.focusshieldwork.data.local.SessionLogRepository
import com.proto.focusshieldwork.presentation.session.formatRemaining
import com.proto.focusshieldwork.presentation.session.remainingSeconds
import com.proto.focusshieldwork.presentation.stats.FocusStatsScreen
import com.proto.focusshieldwork.service.FocusMonitorService
import com.proto.focusshieldwork.presentation.onboarding.PermissionDialog
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

private const val SPLASH_DURATION_MILLIS = 1000L
private const val SESSION_PREFERENCES = "focus_session"
private const val KEY_SESSION_ACTIVE = "session_active"
private const val KEY_SESSION_STARTED_AT = "session_started_at"
private const val KEY_SESSION_ENDS_AT = "session_ends_at"
private const val KEY_SELECTED_DURATION = "selected_duration"
private const val KEY_SELECTED_PACKAGES = "selected_packages"

private var interstitialAd: InterstitialAd? = null

private fun loadInterstitialAd(context: Context) {
    val adUnitId = context.getString(R.string.admob_interstitial_ad_unit_id)
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        adUnitId,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                interstitialAd = null
            }
        }
    )
}

private fun showInterstitialAd(context: Context, onAdDismissed: () -> Unit) {
    val activity = context as? Activity
    val ad = interstitialAd
    if (activity != null && ad != null) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd(context)
                onAdDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                loadInterstitialAd(context)
                onAdDismissed()
            }
        }
        ad.show(activity)
    } else {
        loadInterstitialAd(context)
        onAdDismissed()
    }
}

private fun logAnalyticsEvent(context: Context, eventName: String, block: (android.os.Bundle.() -> Unit)? = null) {
    val analytics = FirebaseAnalytics.getInstance(context)
    val bundle = android.os.Bundle()
    if (block != null) {
        bundle.block()
    }
    analytics.logEvent(eventName, bundle)
}

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
    private var splashStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            android.os.SystemClock.uptimeMillis() - splashStartTime < SPLASH_DURATION_MILLIS
        }
        splashStartTime = android.os.SystemClock.uptimeMillis()
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
    var showEmergencyUnlock by remember { mutableStateOf(false) }
    var unlockPinDraft by remember { mutableStateOf("") }
    var unlockError by remember { mutableStateOf<String?>(null) }
    var pinDraft by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loadInterstitialAd(context)
    }

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

    val exitHistoryAction = {
        showInterstitialAd(context) {
            showHistory = false
        }
    }

    BackHandler(
        enabled = showHistory ||
            (!showPermissionDialog && !showPinSetup && !showAppPicker && !showExitDialog)
    ) {
        if (showHistory) {
            exitHistoryAction()
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
                    onBack = exitHistoryAction,
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
                        unlockPinDraft = ""
                        unlockError = null
                        showEmergencyUnlock = true
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
                            showInterstitialAd(context) {
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
                                logAnalyticsEvent(context, "start_focus_session") {
                                    putInt("duration_minutes", selectedDuration)
                                    putInt("blocked_apps_count", lockedPackages.size)
                                }
                            }
                        }
                    }
                },
                selectedAppNames = installedApps.filter { it.packageName in selectedPackages }.map { it.label },
                pinConfigured = PinManager(context).hasPin(),
                onConfigurePin = { showPinSetup = true },
                onOpenHistory = {
                    logAnalyticsEvent(context, "view_history")
                    showHistory = true
                },
                modifier = Modifier.weight(1f)
            )
            BannerAd(Modifier.padding(top = 6.dp, bottom = 2.dp))
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

    if (showEmergencyUnlock) {
        AlertDialog(
            onDismissRequest = { showEmergencyUnlock = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Emergency Session End", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your 4-digit emergency PIN to end this focus session.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = unlockPinDraft,
                        onValueChange = {
                            if (it.length <= 4 && it.all(Char::isDigit)) {
                                unlockPinDraft = it
                                unlockError = null
                            }
                        },
                        label = { Text("Emergency PIN") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (unlockError != null) Text(unlockError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pinManager = PinManager(context)
                        if (pinManager.verifyPin(unlockPinDraft)) {
                            showInterstitialAd(context) {
                                logAnalyticsEvent(context, "end_focus_session")
                                isSessionActive = false
                                lockedPackages = emptySet()
                                sessionPreferences.edit().putBoolean(KEY_SESSION_ACTIVE, false).apply()
                                context.stopService(Intent(context, FocusMonitorService::class.java))
                                showEmergencyUnlock = false
                            }
                        } else {
                            unlockError = "Incorrect PIN"
                        }
                    },
                    enabled = unlockPinDraft.length == 4
                ) { Text("End Session") }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyUnlock = false }) { Text("Cancel") }
            }
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
    val greeting = remember {
        when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning ☀️"
            in 12..16 -> "Good afternoon 🌤️"
            in 17..21 -> "Good evening 🌙"
            else -> "Late focus session 🌌"
        }
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Surface(
                    shape = CircleShape,
                    color = FocusIndigo.copy(alpha = 0.10f)
                ) {
                    Text(
                        greeting,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = FocusIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(1.dp))
                Text(
                    "Make room for what matters.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.semantics { contentDescription = "Open history and analysis" }
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(20.dp)) {
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
        }

        TimerCard(selectedDuration)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Select Focus Duration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Choose how long to pause distractions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        if (selectedDuration < 60) "${selectedDuration} mins" else "${selectedDuration / 60} hour${if (selectedDuration >= 120) "s" else ""}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(2, 5, 10, 15, 25, 45, 60, 120)) { minutes ->
                    val isSelected = selectedDuration == minutes
                    Surface(
                        onClick = { onDurationSelected(minutes) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) FocusIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                when (minutes) {
                                    25 -> "25m 🍅"
                                    45 -> "45m 🎯"
                                    60 -> "1h ⏳"
                                    120 -> "2h 🚀"
                                    else -> "${minutes}m"
                                },
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("APPS TO PAUSE", style = MaterialTheme.typography.labelSmall, color = FocusIndigo, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        Text(
                            if (selectedApps == 0) "No apps selected" else "${if (selectedApps == 1) "1 app" else "$selectedApps apps"} protected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onSelectApps,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusIndigo),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (selectedApps == 0) "Choose Apps" else "Edit List", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
                if (selectedAppNames.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedAppNames) { app ->
                            AppPill(app)
                        }
                    }
                } else {
                    Text(
                        "Pick the social or gaming apps that usually pull your attention away during work.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛡️", fontSize = 18.sp)
                    Column {
                        Text("Emergency PIN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (pinConfigured) "PIN configured for early exit" else "Required to end session early",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onConfigurePin) {
                    Text(
                        if (pinConfigured) "•••• Ready" else "Set Up",
                        color = FocusIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        NativeAdBanner()

        Button(
            onClick = onStartFocus,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FocusIndigo)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✦", fontSize = 16.sp, color = Color.White)
                Text("START FOCUS", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.height(4.dp))
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Choose your distractions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Select the apps you want paused while focusing.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 580.dp).imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text("Search apps") },
                    leadingIcon = { Text("⌕", fontSize = 22.sp, color = FocusIndigo) },
                    trailingIcon = if (query.isNotEmpty()) ({
                        IconButton(onClick = { query = "" }) { Text("×", fontSize = 22.sp) }
                    }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
                Surface(
                    color = FocusIndigo.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${draftSelection.size} apps selected", fontWeight = FontWeight.Bold, color = FocusIndigo)
                            Text("Will be blocked during active focus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (draftSelection.isNotEmpty()) {
                            TextButton(onClick = { draftSelection = emptySet() }) { Text("Clear all", color = FocusIndigo) }
                        }
                    }
                }
                if (selectedApps.isNotEmpty()) {
                    LazyRow(state = selectionListState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedApps, key = { "selected-${it.packageName}" }) { app ->
                            Surface(
                                onClick = { draftSelection = draftSelection - app.packageName },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.semantics { contentDescription = "Remove ${app.label} from selected apps" }
                            ) {
                                Row(
                                    Modifier.padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AppIcon(icon = app.icon, size = 22.dp)
                                    Text(app.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Text("×", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                if (apps.isEmpty()) {
                    PickerEmptyState("No apps found", "No launchable apps were found on this device.")
                } else if (filteredApps.isEmpty()) {
                    PickerEmptyState("No matching apps", "Try typing another app name.")
                } else {
                    Text("INSTALLED APPS • ${filteredApps.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    LazyColumn(state = appListState, modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isSelected = app.packageName in draftSelection
                            Surface(
                                onClick = {
                                    draftSelection = if (isSelected) draftSelection - app.packageName else draftSelection + app.packageName
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) FocusIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                                    contentDescription = "${app.label}, ${if (isSelected) "selected" else "not selected"}"
                                }
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AppIcon(icon = app.icon, size = 40.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(app.label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                                        Text(if (isSelected) "Selected for focus" else "Tap to select", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            draftSelection = if (checked) draftSelection + app.packageName else draftSelection - app.packageName
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplySelection(draftSelection); onDismiss() },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FocusIndigo)
            ) {
                Text("Save ${draftSelection.size} Apps", fontWeight = FontWeight.Bold)
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
        Text("✦", fontSize = 28.sp, color = FocusIndigo)
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
        shape = RoundedCornerShape(32.dp),
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
                drawCircle(Color.White.copy(alpha = .12f), radius = 180f, center = Offset(size.width * .90f, size.height * .10f))
                drawCircle(FocusAmber.copy(alpha = .20f), radius = 100f, center = Offset(size.width * .08f, size.height * .90f))
            }
            Column(
                Modifier.padding(vertical = 18.dp, horizontal = 16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✦", color = Color.White, fontSize = 12.sp)
                        Text("READY TO FOCUS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    String.format("%02d:%02d:00", minutes / 60, minutes % 60),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(2.dp))
                Text("A quiet space for meaningful work", color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AppPill(name: String) {
    Surface(shape = CircleShape, color = FocusIndigo.copy(alpha = 0.12f)) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = FocusIndigo,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Text("FOCUS IS RUNNING", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color(0xFFE0E7FF), style = Stroke(width = 20f))
                drawArc(
                    brush = Brush.sweepGradient(listOf(FocusIndigo, FocusViolet, FocusPink)),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 20f, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatRemaining(secondsRemaining), fontSize = 42.sp, fontWeight = FontWeight.Bold, color = FocusIndigo, letterSpacing = 1.sp)
                Spacer(Modifier.height(2.dp))
                Text("time remaining", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Your attention is protected", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Surface(
            shape = CircleShape,
            color = FocusIndigo.copy(alpha = 0.1f)
        ) {
            Text("$blockedApps apps silenced", modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), color = FocusIndigo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(28.dp))
        OutlinedButton(
            onClick = onEndSession,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Text("End Session", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
        NativeAdBanner()
    }
}

@Composable
private fun BannerAd(modifier: Modifier = Modifier) {
    val bannerAdUnitId = stringResource(R.string.admob_banner_ad_unit_id)
    AndroidView(
        modifier = modifier.fillMaxWidth().height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerAdUnitId
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
    val nativeAdUnitId = stringResource(R.string.admob_native_ad_unit_id)
    LaunchedEffect(context, nativeAdUnitId) {
        AdLoader.Builder(context, nativeAdUnitId)
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