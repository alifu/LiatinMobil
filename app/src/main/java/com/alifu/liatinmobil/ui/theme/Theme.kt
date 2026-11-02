package com.alifu.liatinmobil.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Always light, so the app stays green & white regardless of system settings. */
private val LiatinColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary = GreenDark,
    onSecondary = White,
    secondaryContainer = GreenLight,
    onSecondaryContainer = GreenDark,
    background = White,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,
    outline = DividerGray,
    outlineVariant = DividerGray,
)

@Composable
fun LiatinMobilTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LiatinColorScheme,
        typography = Typography,
        content = content,
    )
}
