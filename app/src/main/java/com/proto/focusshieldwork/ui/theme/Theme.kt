package com.proto.focusshieldwork.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val FocusShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = FocusPrimaryDark,
    onPrimary = FocusOnPrimaryDark,
    secondary = FocusViolet,
    onSecondary = FocusOnPrimaryLight,
    tertiary = FocusPink,
    background = FocusBackgroundDark,
    onBackground = FocusOnSurfaceDark,
    surface = FocusSurfaceDark,
    onSurface = FocusOnSurfaceDark,
    surfaceVariant = FocusSurfaceVariantDark,
    onSurfaceVariant = FocusOnSurfaceVariantDark,
    error = FocusError
)

private val LightColorScheme = lightColorScheme(
    primary = FocusPrimaryLight,
    onPrimary = FocusOnPrimaryLight,
    secondary = FocusViolet,
    onSecondary = FocusOnPrimaryLight,
    tertiary = FocusPink,
    background = FocusBackgroundLight,
    onBackground = FocusOnSurfaceLight,
    surface = FocusSurfaceLight,
    onSurface = FocusOnSurfaceLight,
    surfaceVariant = FocusSurfaceVariantLight,
    onSurfaceVariant = FocusOnSurfaceVariantLight,
    error = FocusError
)

@Composable
fun FocusOnWorkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor) {
        // Keep brand colors deterministic until a user-selectable dynamic theme is added.
        if (darkTheme) DarkColorScheme else LightColorScheme
    } else if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FocusShapes,
        content = content
    )
}