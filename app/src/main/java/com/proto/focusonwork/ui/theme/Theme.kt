package com.proto.focusonwork.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.proto.focusonwork.ui.theme.FocusBackgroundLight
import com.proto.focusonwork.ui.theme.FocusOnPrimaryDark
import com.proto.focusonwork.ui.theme.FocusOnPrimaryLight
import com.proto.focusonwork.ui.theme.FocusPink
import com.proto.focusonwork.ui.theme.FocusPrimaryDark
import com.proto.focusonwork.ui.theme.FocusPrimaryLight
import com.proto.focusonwork.ui.theme.FocusSurfaceDark
import com.proto.focusonwork.ui.theme.FocusSurfaceLight
import com.proto.focusonwork.ui.theme.FocusViolet

private val DarkColorScheme = darkColorScheme(
    primary = FocusPrimaryDark,
    onPrimary = FocusOnPrimaryDark,
    secondary = FocusViolet,
    tertiary = FocusPink,
    background = FocusBackgroundDark,
    surface = FocusSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = FocusPrimaryLight,
    onPrimary = FocusOnPrimaryLight,
    secondary = FocusViolet,
    tertiary = FocusPink,
    background = FocusBackgroundLight,
    surface = FocusSurfaceLight
)

@Composable
fun FocusOnWorkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}