package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005143),
    onPrimaryContainer = Color(0xFF5BFFDC),
    secondary = CyberBlue,
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF004C71),
    onSecondaryContainer = Color(0xFFBBE9FF),
    tertiary = CyberAmber,
    onTertiary = Color(0xFF432C00),
    background = CyberNavy,
    onBackground = TextWhite,
    surface = CyberNavySurface,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF14244B),
    onSurfaceVariant = TextMuted,
    outline = CyberCardBorder,
    error = CyberRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C5D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF73F8DE),
    onPrimaryContainer = Color(0xFF00201B),
    secondary = Color(0xFF006494),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCBE6FF),
    onSecondaryContainer = Color(0xFF001E30),
    tertiary = Color(0xFF7F5B00),
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Cybersecurity Dark Theme for professional security SOC look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
