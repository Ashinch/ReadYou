package me.ash.reader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import me.ash.reader.data.preference.LocalThemeIndex
import me.ash.reader.ui.theme.palette.LocalTonalPalettes
import me.ash.reader.ui.theme.palette.TonalPalettes
import me.ash.reader.ui.theme.palette.core.ProvideZcamViewingConditions
import me.ash.reader.ui.theme.palette.dynamic.extractTonalPalettesFromUserWallpaper
import me.ash.reader.ui.theme.palette.dynamicDarkColorScheme
import me.ash.reader.ui.theme.palette.dynamicLightColorScheme

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    wallpaperPalettes: List<TonalPalettes> = extractTonalPalettesFromUserWallpaper(),
    content: @Composable () -> Unit
) {
    val themeIndex = LocalThemeIndex.current

    val tonalPalettes = wallpaperPalettes[
            if (themeIndex >= wallpaperPalettes.size) {
                when {
                    wallpaperPalettes.size == 5 -> 0
                    wallpaperPalettes.size > 5 -> 5
                    else -> 0
                }
            } else {
                themeIndex
            }
    ]

    ProvideZcamViewingConditions {
        CompositionLocalProvider(
            LocalTonalPalettes provides tonalPalettes.also { it.Preheating() },
        ) {
            MaterialTheme(
                colorScheme =
                if (useDarkTheme) dynamicDarkColorScheme()
                else dynamicLightColorScheme(),
                typography = AppTypography,
                content = content
            )
        }
    }
}