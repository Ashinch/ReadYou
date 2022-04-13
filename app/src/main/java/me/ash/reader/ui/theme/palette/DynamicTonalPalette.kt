package me.ash.reader.ui.theme.palette

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun dynamicLightColorScheme(): ColorScheme {
    val palettes = LocalTonalPalettes.current
    return lightColorScheme(
        primary = palettes primary 40,
        onPrimary = palettes primary 100,
        primaryContainer = palettes primary 90,
        onPrimaryContainer = palettes primary 10,
        inversePrimary = palettes primary 80,
        secondary = palettes secondary 40,
        onSecondary = palettes secondary 100,
        secondaryContainer = palettes secondary 90,
        onSecondaryContainer = palettes secondary 10,
        tertiary = palettes tertiary 40,
        onTertiary = palettes tertiary 100,
        tertiaryContainer = palettes tertiary 90,
        onTertiaryContainer = palettes tertiary 10,
        background = palettes neutral 99,
        onBackground = palettes neutral 10,
        surface = palettes neutral 99,
        onSurface = palettes neutral 10,
        surfaceVariant = palettes neutralVariant 90,
        onSurfaceVariant = palettes neutralVariant 30,
        inverseSurface = palettes neutral 20,
        inverseOnSurface = palettes neutral 95,
        outline = palettes neutralVariant 50,
    )
}

@Composable
fun dynamicDarkColorScheme(): ColorScheme {
    val palettes = LocalTonalPalettes.current
    return darkColorScheme(
        primary = palettes primary 80,
        onPrimary = palettes primary 20,
        primaryContainer = palettes primary 30,
        onPrimaryContainer = palettes primary 90,
        inversePrimary = palettes primary 40,
        secondary = palettes secondary 80,
        onSecondary = palettes secondary 20,
        secondaryContainer = palettes secondary 30,
        onSecondaryContainer = palettes secondary 90,
        tertiary = palettes tertiary 80,
        onTertiary = palettes tertiary 20,
        tertiaryContainer = palettes tertiary 30,
        onTertiaryContainer = palettes tertiary 90,
        background = palettes neutral 10,
        onBackground = palettes neutral 90,
        surface = palettes neutral 10,
        onSurface = palettes neutral 90,
        surfaceVariant = palettes neutralVariant 30,
        onSurfaceVariant = palettes neutralVariant 80,
        inverseSurface = palettes neutral 90,
        inverseOnSurface = palettes neutral 20,
        outline = palettes neutralVariant 60,
    )
}

@Composable
fun ColorScheme.tonalPalettes() = LocalTonalPalettes.current

@Suppress("NOTHING_TO_INLINE")
@Composable
inline infix fun Color.onLight(lightColor: Color): Color =
    if (!isSystemInDarkTheme()) lightColor else this

@Suppress("NOTHING_TO_INLINE")
@Composable
inline infix fun Color.onDark(darkColor: Color): Color =
    if (isSystemInDarkTheme()) darkColor else this

fun String.checkColorHex(): String? {
    var s = this.trim()
    if (s.length > 6) {
        s = s.substring(s.length - 6)
    }
    return "[0-9a-fA-F]{6}".toRegex().find(s)?.value
}

fun String.safeHexToColor(): Color =
    try {
        Color(java.lang.Long.parseLong(this, 16))
    } catch (e: Exception) {
        Color.Transparent
    }
