package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CastCyanAccent,
    onPrimary = Color(0xFF003642),
    primaryContainer = Color(0xFF004E5F),
    onPrimaryContainer = Color(0xFFA6EEFF),
    secondary = CastIndigoPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4338CA),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = CastEmeraldSuccess,
    onTertiary = Color.White,
    background = CastDarkBackground,
    onBackground = CastDarkTextPrimary,
    surface = CastDarkSurface,
    onSurface = CastDarkTextPrimary,
    surfaceVariant = CastDarkSurfaceVariant,
    onSurfaceVariant = CastDarkTextSecondary,
    outline = CastDarkCardBorder
)

private val LightColorScheme = darkColorScheme(
    primary = CastCyanAccent,
    onPrimary = Color(0xFF003642),
    primaryContainer = Color(0xFF004E5F),
    secondary = CastIndigoPrimary,
    background = CastDarkBackground,
    surface = CastDarkSurface,
    onSurface = CastDarkTextPrimary,
    outline = CastDarkCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted cinematic TV Cast theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
