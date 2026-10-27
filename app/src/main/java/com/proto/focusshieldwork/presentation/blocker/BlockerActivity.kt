package com.proto.focusshieldwork.presentation.blocker

import android.content.Intent
import android.graphics.Color
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button as AndroidButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.proto.focusshieldwork.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.proto.focusshieldwork.MainActivity
import com.proto.focusshieldwork.presentation.security.EmergencyUnlockScreen
import com.proto.focusshieldwork.security.PinManager
import com.proto.focusshieldwork.service.FocusMonitorService
import com.proto.focusshieldwork.ui.theme.FocusOnWorkTheme
import kotlinx.coroutines.delay

class BlockerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        val blockedAppName = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(blockedPackage, 0)).toString()
        }.getOrElse { blockedPackage }
        val pinManager = PinManager(this)
        setContent {
            FocusOnWorkTheme {
                BlockerScreen(
                    blockedAppName = blockedAppName,
                    pinManager = pinManager,
                    onBackToFocus = {
                        startActivity(Intent(this@BlockerActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        })
                        finish()
                    },
                    onUnlocked = {
                        stopService(Intent(this, FocusMonitorService::class.java))
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
    }
}

@Composable
private fun BlockerScreen(
    blockedAppName: String,
    pinManager: PinManager,
    onBackToFocus: () -> Unit,
    onUnlocked: () -> Unit
) {
    var showUnlock by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(15) }
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showUnlock) {
        LaunchedEffect(Unit) {
            while (secondsRemaining > 0) {
                delay(1_000)
                secondsRemaining--
            }
        }
        EmergencyUnlockScreen(
            secondsRemaining = secondsRemaining,
            pin = pin,
            errorMessage = errorMessage,
            onPinChanged = { pin = it; errorMessage = null },
            onUnlock = {
                if (pinManager.verifyPin(pin)) onUnlocked()
                else errorMessage = "Incorrect PIN"
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Card(
                Modifier.fillMaxWidth().padding(24.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("✦", Modifier.padding(horizontal = 24.dp, vertical = 14.dp), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Protect your flow", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(
                        text = if (blockedAppName.isBlank()) "This app is paused during your focus session." else "$blockedAppName is paused during your focus session.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onBackToFocus, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Text("Back to Focus") }
                    TextButton(onClick = { showUnlock = true }) { Text("Emergency Unlock") }
                    Spacer(Modifier.height(4.dp))
                    NativeAdBanner()
                }
            }
        }
    }
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
            modifier = Modifier.fillMaxWidth().height(120.dp),
            factory = { viewContext ->
                val density = viewContext.resources.displayMetrics.density
                fun dp(value: Int) = (value * density).toInt()

                NativeAdView(viewContext).apply {
                    val card = LinearLayout(viewContext).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(12), dp(8), dp(12), dp(8))
                        background = GradientDrawable().apply {
                            setColor(AndroidColor.argb(12, 70, 70, 90))
                            cornerRadius = dp(16).toFloat()
                        }
                    }
                    val adLabel = TextView(viewContext).apply {
                        text = "Ad"
                        textSize = 10f
                        setTextColor(AndroidColor.rgb(75, 65, 190))
                        setPadding(dp(5), dp(2), dp(5), dp(2))
                        background = GradientDrawable().apply {
                            setColor(AndroidColor.rgb(238, 235, 255))
                            cornerRadius = dp(4).toFloat()
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
                        setPadding(dp(8), 0, dp(6), 0)
                    }
                    val headline = TextView(viewContext).apply {
                        textSize = 14f
                        setTextColor(AndroidColor.rgb(28, 27, 36))
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }
                    val body = TextView(viewContext).apply {
                        textSize = 11f
                        setTextColor(AndroidColor.rgb(95, 93, 105))
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }
                    val callToAction = AndroidButton(viewContext).apply {
                        textSize = 11f
                        isAllCaps = false
                        minHeight = dp(36)
                        setPadding(dp(10), 0, dp(10), 0)
                    }
                    copy.addView(headline, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                    copy.addView(body, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dp(2)
                    })
                    row.addView(icon, LinearLayout.LayoutParams(dp(38), dp(38)))
                    row.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                    row.addView(callToAction, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(36)))
                    card.addView(adLabel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                    card.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                        topMargin = dp(6)
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
