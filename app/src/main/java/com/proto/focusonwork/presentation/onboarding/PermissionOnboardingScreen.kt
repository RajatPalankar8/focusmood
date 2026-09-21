package com.proto.focusonwork.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PermissionOnboardingScreen(
    usageAccessGranted: Boolean,
    overlayAccessGranted: Boolean,
    onRequestUsageAccess: () -> Unit,
    onRequestOverlayAccess: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Protect your focus", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Allow access only when you are ready to block distracting apps during a focus session.")
        PermissionRow("Usage access", "Detect when a selected app comes to the foreground", usageAccessGranted, onRequestUsageAccess)
        PermissionRow("Display over other apps", "Show the blocker screen when needed", overlayAccessGranted, onRequestOverlayAccess)
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }
}

@Composable
private fun PermissionRow(title: String, description: String, granted: Boolean, onRequest: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(if (granted) "Ready" else "Needed", color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium)
            if (!granted) Button(onClick = onRequest) { Text("Allow access") }
        }
    }
}
