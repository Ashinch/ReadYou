package me.ash.reader.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import me.ash.reader.ui.theme.color.PurpleColor

private val LightThemeColors = PurpleColor.lightColorScheme
private val DarkThemeColors = PurpleColor.darkColorScheme

val LocalLightThemeColors = staticCompositionLocalOf { LightThemeColors }
val LocalDarkThemeColors = staticCompositionLocalOf { DarkThemeColors }

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color is available on Android 12+
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val light = when {
        dynamicColor -> dynamicLightColorScheme(LocalContext.current)
        else -> LightThemeColors
    }
    val dark = when {
        dynamicColor -> dynamicDarkColorScheme(LocalContext.current)
        else -> DarkThemeColors
    }
    val colorScheme = when {
        useDarkTheme -> dark
        else -> light
    }

    CompositionLocalProvider(
        LocalLightThemeColors provides light,
        LocalDarkThemeColors provides dark,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}