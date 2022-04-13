/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.theme.palette.dynamic

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.flow.map
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.theme.palette.TonalPalettes
import me.ash.reader.ui.theme.palette.TonalPalettes.Companion.toTonalPalettes
import me.ash.reader.ui.theme.palette.safeHexToColor

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun extractTonalPalettesFromUserWallpaper(): List<TonalPalettes> {
    val context = LocalContext.current
    val customPrimaryColor =
        context.dataStore.data.map { it[DataStoreKeys.CustomPrimaryColor.key] ?: "" }
            .collectAsState(initial = "").value

    val preset = mutableListOf(
        Color(0xFF80BBFF).toTonalPalettes(),
        Color(0xFFFFD8E4).toTonalPalettes(),
        Color(0xFF62539f).toTonalPalettes(),
        Color(0xFFE9B666).toTonalPalettes(),
        customPrimaryColor.safeHexToColor().toTonalPalettes()
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !LocalView.current.isInEditMode) {
        val colors = WallpaperManager.getInstance(LocalContext.current)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        val primary = colors?.primaryColor?.toArgb()
        val secondary = colors?.secondaryColor?.toArgb()
        val tertiary = colors?.tertiaryColor?.toArgb()
        if (primary != null) preset.add(Color(primary).toTonalPalettes())
        if (secondary != null) preset.add(Color(secondary).toTonalPalettes())
        if (tertiary != null) preset.add(Color(tertiary).toTonalPalettes())
    }
    return preset
}
