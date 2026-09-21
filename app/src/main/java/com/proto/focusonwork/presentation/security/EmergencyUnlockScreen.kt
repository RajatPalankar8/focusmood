package com.proto.focusonwork.presentation.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun EmergencyUnlockScreen(
    secondsRemaining: Int,
    pin: String,
    errorMessage: String?,
    onPinChanged: (String) -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Take a breath", style = MaterialTheme.typography.headlineMedium)
        if (secondsRemaining > 0) {
            Text("Emergency unlock available in ${secondsRemaining}s")
        } else {
            Text("Enter your 4-digit emergency PIN")
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) onPinChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Button(onClick = onUnlock, enabled = pin.length == 4, modifier = Modifier.fillMaxWidth()) {
                Text("Unlock focus mode")
            }
        }
    }
}
