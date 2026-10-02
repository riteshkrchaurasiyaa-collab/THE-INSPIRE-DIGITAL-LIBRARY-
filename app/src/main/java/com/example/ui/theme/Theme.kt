package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavySurface,
    onPrimaryContainer = Color.White,
    secondary = GoldPrimary,
    onSecondary = NavyDark,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = EmeraldPresent,
    onTertiary = Color.White,
    background = SlateBackground,
    onBackground = SlateTextPrimary,
    surface = SlateCard,
    onSurface = SlateTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateTextMuted,
    outline = SlateBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldLight,
    onPrimary = NavyDark,
    primaryContainer = NavySurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = GoldPrimary,
    onSecondary = NavyDark,
    secondaryContainer = NavySurface,
    onSecondaryContainer = GoldLight,
    tertiary = EmeraldPresent,
    onTertiary = NavyDark,
    background = NavyDark,
    onBackground = Color.White,
    surface = NavyPrimary,
    onSurface = Color.White,
    surfaceVariant = NavySurface,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = NavySurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
