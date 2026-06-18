package com.anoop.gurbanidaily

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Saffron = Color(0xFFE0792B)
private val DeepSaffron = Color(0xFFB85C16)
private val NavyAccent = Color(0xFF1A3A5C)

private val LightColors = lightColorScheme(
    primary = DeepSaffron,
    secondary = NavyAccent,
    background = Color(0xFFFCF8F3),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Saffron,
    secondary = Color(0xFF8FB4D6),
    background = Color(0xFF14110D),
    surface = Color(0xFF1F1A14)
)

@Composable
fun GurbaniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
