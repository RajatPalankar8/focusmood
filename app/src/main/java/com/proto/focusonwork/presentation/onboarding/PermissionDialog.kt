package com.proto.focusonwork.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(
    usageGranted: Boolean,
    overlayGranted: Boolean,
    onOpenUsageAccess: () -> Unit,
    onOpenOverlayAccess: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Prepare your focus space", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Two switches keep your focus session protected", style = MaterialTheme.typography.bodyMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = Color.Transparent
                ) {
                    Column(
                        Modifier
                            .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFFDB2777))))
                            .padding(18.dp)
                    ) {
                        Text("Almost ready", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Turn on both permissions below, then come back to start focus mode.", color = Color.White.copy(alpha = .85f))
                    }
                }
                Text("Required permissions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AccessRow(
                    number = "1",
                    title = "Usage access",
                    description = "Detect when a blocked app is opened.",
                    granted = usageGranted,
                    onClick = onOpenUsageAccess
                )
                AccessRow(
                    number = "2",
                    title = "Display over other apps",
                    description = "Show the focus screen immediately.",
                    granted = overlayGranted,
                    onClick = onOpenOverlayAccess
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("I’ll do this later") }
        }
    )
}

@Composable
private fun AccessRow(
    number: String,
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(40.dp)
            ) {
                Text(number, modifier = Modifier.padding(10.dp), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            if (granted) {
                Text("Ready", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            } else {
                Button(onClick = onClick) { Text("Allow") }
            }
        }
    }
}
