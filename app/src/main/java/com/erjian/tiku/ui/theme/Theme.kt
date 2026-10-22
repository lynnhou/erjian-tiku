package com.erjian.tiku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BrandBlue = Color(0xFF1565C0)
val BrandTeal = Color(0xFF00897B)
val CorrectGreen = Color(0xFF2E7D32)
val WrongRed = Color(0xFFC62828)
val CorrectGreenBg = Color(0xFFE8F5E9)
val WrongRedBg = Color(0xFFFFEBEE)
val MultiPurple = Color(0xFF7B1FA2)
val SingleBlue = Color(0xFF1565C0)
val JudgeOrange = Color(0xFFEF6C00)

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3F7),
    onPrimaryContainer = Color(0xFF0A2E5C),
    secondary = BrandTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0EAE6),
    onSecondaryContainer = Color(0xFF06463E),
    background = Color(0xFFF5F6F8),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFEEF0F3),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFE2E4E9),
    error = WrongRed,
    onError = Color.White,
)

@Composable
fun ErJianTiKuTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
