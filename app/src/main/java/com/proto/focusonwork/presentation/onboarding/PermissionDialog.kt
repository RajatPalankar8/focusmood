package com.proto.focusonwork.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.proto.focusonwork.ui.theme.FocusSuccess
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

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
        shape = RoundedCornerShape(30.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Prepare your focus space", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Two switches keep your focus session protected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Transparent
                ) {
                    Column(
                        Modifier
                            .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFFDB2777))))
                            .padding(18.dp)
                    ) {
                        Text(if (usageGranted && overlayGranted) "You're all set" else "Almost ready", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (usageGranted && overlayGranted) "Both permissions are enabled. Continue to start your focus session." else "Allow each permission below. This screen will update when you return.", color = Color.White.copy(alpha = .88f))
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
            if (usageGranted && overlayGranted) {
                Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) { Text("Continue") }
            } else {
                TextButton(onClick = onDismiss) { Text("I’ll do this later") }
            }
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
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                color = if (granted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Text(number, modifier = Modifier.padding(10.dp), color = if (granted) MaterialTheme.colorScheme.onSecondaryContainer else Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Surface(
                onClick = { if (!granted) onClick() },
                color = if (granted) Color(0xFFDDF5E8) else MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.semantics {
                    contentDescription = if (granted) "$title permission allowed" else "Allow $title permission"
                }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(if (granted) "✓" else "+", color = if (granted) FocusSuccess else MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    Text(if (granted) "Allowed" else "Allow", color = if (granted) FocusSuccess else MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
