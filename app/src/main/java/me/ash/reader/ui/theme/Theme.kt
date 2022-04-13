package me.ash.reader.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.theme.palette.LocalTonalPalettes
import me.ash.reader.ui.theme.palette.TonalPalettes
import me.ash.reader.ui.theme.palette.core.ProvideZcamViewingConditions
import me.ash.reader.ui.theme.palette.dynamic.extractTonalPalettesFromUserWallpaper
import me.ash.reader.ui.theme.palette.dynamicDarkColorScheme
import me.ash.reader.ui.theme.palette.dynamicLightColorScheme

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    wallpaperPalettes: List<TonalPalettes> = extractTonalPalettesFromUserWallpaper(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeIndex = context.dataStore.data.map { it[DataStoreKeys.ThemeIndex.key] ?: 0 }
        .collectAsState(initial = 0).value

    ProvideZcamViewingConditions {
        CompositionLocalProvider(
            LocalTonalPalettes provides wallpaperPalettes[
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