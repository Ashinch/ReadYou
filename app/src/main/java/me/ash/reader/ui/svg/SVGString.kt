package me.ash.reader.ui.svg

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.ash.reader.ui.theme.palette.TonalPalettes

object SVGString

fun String.parseDynamicColor(tonalPalettes: TonalPalettes, isDarkTheme: Boolean): String =
    replace("fill=\"(.+?)\"".toRegex()) {
        val value = it.groupValues[1]
        Log.i("RLog", "parseDynamicColor: $value")
        if (value.startsWith("#")) return@replace it.value
        try {
            val (scheme, tone) = value.split("(?<=\\d)(?=\\D)|(?=\\d)(?<=\\D)".toRegex())
            val argb = when (scheme) {
                "p" -> tonalPalettes.primary[tone.toInt().autoToDarkTone(isDarkTheme)]
                "s" -> tonalPalettes.secondary[tone.toInt().autoToDarkTone(isDarkTheme)]
                "t" -> tonalPalettes.tertiary[tone.toInt().autoToDarkTone(isDarkTheme)]
                "n" -> tonalPalettes.neutral[tone.toInt().autoToDarkTone(isDarkTheme)]
                "nv" -> tonalPalettes.neutralVariant[tone.toInt().autoToDarkTone(isDarkTheme)]
                "e" -> tonalPalettes.error[tone.toInt().autoToDarkTone(isDarkTheme)]
                else -> Color.Transparent
            }?.toArgb() ?: 0xFFFFFF
            "fill=\"${String.format("#%06X", 0xFFFFFF and argb)}\""
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "parseDynamicColor: ${e.message}")
            it.value
        }
    }

internal fun Int.autoToDarkTone(isDarkTheme: Boolean): Int =
    if (!isDarkTheme) this
    else when (this) {
        10 -> 99
        20 -> 95
        25 -> 90
        30 -> 90
        40 -> 80
        50 -> 60
        60 -> 50
        70 -> 40
        80 -> 40
        90 -> 30
        95 -> 20
        98 -> 10
        99 -> 10
        100 -> 20
        else -> this
    }