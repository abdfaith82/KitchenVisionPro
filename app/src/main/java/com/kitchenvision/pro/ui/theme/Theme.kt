package com.kitchenvision.pro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    secondary = Color(0xFFFF6F00),
    onSecondary = Color.White,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEEF1F5),
    outline = Color(0xFFDADCE0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0D47A1),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFF3E2723),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF2C2C2C),
    outline = Color(0xFF5F6368)
)

@Composable
fun KitchenVisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
