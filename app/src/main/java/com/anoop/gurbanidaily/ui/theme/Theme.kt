package com.anoop.gurbanidaily.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = BurntSaffron,
    onPrimary = CreamSurface,
    primaryContainer = PeachAccent,
    onPrimaryContainer = DeepSaffronText,
    secondary = InkNavy,
    onSecondary = CreamSurface,
    background = CreamBackground,
    onBackground = InkText,
    surface = CreamSurface,
    onSurface = InkText,
    surfaceVariant = CreamBackground,
    onSurfaceVariant = WarmMutedText,
    outline = SoftHairline,
    outlineVariant = SoftHairline
)

private val DarkColors = darkColorScheme(
    primary = WarmSaffron,
    onPrimary = InkBackground,
    primaryContainer = SaffronGlow,
    onPrimaryContainer = WarmSaffron,
    secondary = MistedBlue,
    onSecondary = InkBackground,
    background = InkBackground,
    onBackground = CreamText,
    surface = CharcoalSurface,
    onSurface = CreamText,
    surfaceVariant = CharcoalSurface,
    onSurfaceVariant = DimText,
    outline = DarkHairline,
    outlineVariant = DarkHairline
)

@Composable
fun GurbaniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GurbaniTypography,
        content = content
    )
}
