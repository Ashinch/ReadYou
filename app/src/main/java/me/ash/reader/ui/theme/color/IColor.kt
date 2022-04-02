package me.ash.reader.ui.theme.color

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

interface IColor {
    val md_theme_light_primary: Color
    val md_theme_light_onPrimary: Color
    val md_theme_light_primaryContainer: Color
    val md_theme_light_onPrimaryContainer: Color
    val md_theme_light_secondary: Color
    val md_theme_light_onSecondary: Color
    val md_theme_light_secondaryContainer: Color
    val md_theme_light_onSecondaryContainer: Color
    val md_theme_light_tertiary: Color
    val md_theme_light_onTertiary: Color
    val md_theme_light_tertiaryContainer: Color
    val md_theme_light_onTertiaryContainer: Color
    val md_theme_light_error: Color
    val md_theme_light_errorContainer: Color
    val md_theme_light_onError: Color
    val md_theme_light_onErrorContainer: Color
    val md_theme_light_background: Color
    val md_theme_light_onBackground: Color
    val md_theme_light_surface: Color
    val md_theme_light_onSurface: Color
    val md_theme_light_surfaceVariant: Color
    val md_theme_light_onSurfaceVariant: Color
    val md_theme_light_outline: Color
    val md_theme_light_inverseOnSurface: Color
    val md_theme_light_inverseSurface: Color
    val md_theme_light_inversePrimary: Color
    val md_theme_light_shadow: Color

    val md_theme_dark_primary: Color
    val md_theme_dark_onPrimary: Color
    val md_theme_dark_primaryContainer: Color
    val md_theme_dark_onPrimaryContainer: Color
    val md_theme_dark_secondary: Color
    val md_theme_dark_onSecondary: Color
    val md_theme_dark_secondaryContainer: Color
    val md_theme_dark_onSecondaryContainer: Color
    val md_theme_dark_tertiary: Color
    val md_theme_dark_onTertiary: Color
    val md_theme_dark_tertiaryContainer: Color
    val md_theme_dark_onTertiaryContainer: Color
    val md_theme_dark_error: Color
    val md_theme_dark_errorContainer: Color
    val md_theme_dark_onError: Color
    val md_theme_dark_onErrorContainer: Color
    val md_theme_dark_background: Color
    val md_theme_dark_onBackground: Color
    val md_theme_dark_surface: Color
    val md_theme_dark_onSurface: Color
    val md_theme_dark_surfaceVariant: Color
    val md_theme_dark_onSurfaceVariant: Color
    val md_theme_dark_outline: Color
    val md_theme_dark_inverseOnSurface: Color
    val md_theme_dark_inverseSurface: Color
    val md_theme_dark_inversePrimary: Color
    val md_theme_dark_shadow: Color

    val seed: Color
    val error: Color

    val lightColorScheme: ColorScheme
        get() = lightColorScheme(
            primary = md_theme_light_primary,
            onPrimary = md_theme_light_onPrimary,
            primaryContainer = md_theme_light_primaryContainer,
            onPrimaryContainer = md_theme_light_onPrimaryContainer,
            secondary = md_theme_light_secondary,
            onSecondary = md_theme_light_onSecondary,
            secondaryContainer = md_theme_light_secondaryContainer,
            onSecondaryContainer = md_theme_light_onSecondaryContainer,
            tertiary = md_theme_light_tertiary,
            onTertiary = md_theme_light_onTertiary,
            tertiaryContainer = md_theme_light_tertiaryContainer,
            onTertiaryContainer = md_theme_light_onTertiaryContainer,
            error = md_theme_light_error,
            errorContainer = md_theme_light_errorContainer,
            onError = md_theme_light_onError,
            onErrorContainer = md_theme_light_onErrorContainer,
            background = md_theme_light_background,
            onBackground = md_theme_light_onBackground,
            surface = md_theme_light_surface,
            onSurface = md_theme_light_onSurface,
            surfaceVariant = md_theme_light_surfaceVariant,
            onSurfaceVariant = md_theme_light_onSurfaceVariant,
            outline = md_theme_light_outline,
            inverseOnSurface = md_theme_light_inverseOnSurface,
            inverseSurface = md_theme_light_inverseSurface,
            inversePrimary = md_theme_light_inversePrimary,
        )

    val darkColorScheme: ColorScheme
        get() = darkColorScheme(
            primary = md_theme_dark_primary,
            onPrimary = md_theme_dark_onPrimary,
            primaryContainer = md_theme_dark_primaryContainer,
            onPrimaryContainer = md_theme_dark_onPrimaryContainer,
            secondary = md_theme_dark_secondary,
            onSecondary = md_theme_dark_onSecondary,
            secondaryContainer = md_theme_dark_secondaryContainer,
            onSecondaryContainer = md_theme_dark_onSecondaryContainer,
            tertiary = md_theme_dark_tertiary,
            onTertiary = md_theme_dark_onTertiary,
            tertiaryContainer = md_theme_dark_tertiaryContainer,
            onTertiaryContainer = md_theme_dark_onTertiaryContainer,
            error = md_theme_dark_error,
            errorContainer = md_theme_dark_errorContainer,
            onError = md_theme_dark_onError,
            onErrorContainer = md_theme_dark_onErrorContainer,
            background = md_theme_dark_background,
            onBackground = md_theme_dark_onBackground,
            surface = md_theme_dark_surface,
            onSurface = md_theme_dark_onSurface,
            surfaceVariant = md_theme_dark_surfaceVariant,
            onSurfaceVariant = md_theme_dark_onSurfaceVariant,
            outline = md_theme_dark_outline,
            inverseOnSurface = md_theme_dark_inverseOnSurface,
            inverseSurface = md_theme_dark_inverseSurface,
            inversePrimary = md_theme_dark_inversePrimary,
        )
}