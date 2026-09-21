package com.proto.focusonwork.presentation.blocker

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proto.focusonwork.MainActivity
import com.proto.focusonwork.presentation.security.EmergencyUnlockScreen
import com.proto.focusonwork.security.PinManager
import com.proto.focusonwork.service.FocusMonitorService
import com.proto.focusonwork.ui.theme.FocusOnWorkTheme
import kotlinx.coroutines.delay

class BlockerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        val pinManager = PinManager(this)
        setContent {
            FocusOnWorkTheme {
                BlockerScreen(
                    blockedPackage = blockedPackage,
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
    blockedPackage: String,
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
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("✦", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
            Text("Protect your flow", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(
                text = if (blockedPackage.isBlank()) "This app is paused during your focus session." else "$blockedPackage is paused during your focus session.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(onClick = onBackToFocus) { Text("Back to Focus") }
            TextButton(onClick = { showUnlock = true }) { Text("Emergency Unlock") }
        }
    }
}
