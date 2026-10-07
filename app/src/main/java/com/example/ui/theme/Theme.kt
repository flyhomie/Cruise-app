package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = CruizeNeonCyan,
    onPrimary = CruizeDarkBackground,
    primaryContainer = CruizeDarkSurfaceVariant,
    onPrimaryContainer = CruizeTextPrimary,
    secondary = CruizeElectricBlue,
    onSecondary = CruizeDarkBackground,
    tertiary = CruizeWarningAmber,
    background = CruizeDarkBackground,
    onBackground = CruizeTextPrimary,
    surface = CruizeDarkSurface,
    onSurface = CruizeTextPrimary,
    surfaceVariant = CruizeDarkSurfaceVariant,
    onSurfaceVariant = CruizeTextSecondary,
    error = CruizeDangerCrimson,
    outline = CruizeDarkCardBorder
)

private val LightColorScheme = darkColorScheme(
    primary = CruizeNeonCyan,
    onPrimary = CruizeDarkBackground,
    primaryContainer = CruizeDarkSurfaceVariant,
    onPrimaryContainer = CruizeTextPrimary,
    secondary = CruizeElectricBlue,
    onSecondary = CruizeDarkBackground,
    tertiary = CruizeWarningAmber,
    background = CruizeDarkBackground,
    onBackground = CruizeTextPrimary,
    surface = CruizeDarkSurface,
    onSurface = CruizeTextPrimary,
    surfaceVariant = CruizeDarkSurfaceVariant,
    onSurfaceVariant = CruizeTextSecondary,
    error = CruizeDangerCrimson,
    outline = CruizeDarkCardBorder
)

@Composable
fun CruizeTheme(
    darkTheme: Boolean = true, // Default to sleek automotive night mode
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CruizeDarkBackground.toArgb()
            window.navigationBarColor = CruizeDarkBackground.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    CruizeTheme(content = content)
}
