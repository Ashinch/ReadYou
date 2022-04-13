/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import me.ash.reader.ui.theme.palette.colorspace.cielab.CieLab
import me.ash.reader.ui.theme.palette.colorspace.zcam.Izazbz.Companion.toIzazbz
import me.ash.reader.ui.theme.palette.core.*

/**
 * The L from CIELab
 */
typealias TonalValue = Int

typealias TonalPalette = MutableMap<TonalValue, Color>

val LocalTonalPalettes = compositionLocalOf {
    TonalPalettes(238.36, 15.0)
}

@Composable
private fun TonalValue.toZcamLightness(): Double =
    CieLab(L = if (this != 50) toDouble() else 49.6, a = 0.0, b = 0.0).toXyz().toIzazbz()
        .toZcam().Jz

data class TonalPalettes(
    val hue: Double,
    val primaryChroma: Double,
    val primary: TonalPalette = mutableMapOf(),
    val secondary: TonalPalette = mutableMapOf(),
    val tertiary: TonalPalette = mutableMapOf(),
    val neutral: TonalPalette = mutableMapOf(),
    val neutralVariant: TonalPalette = mutableMapOf(),
    val error: TonalPalette = mutableMapOf(),
) {
    @Composable
    infix fun primary(tone: TonalValue): Color = primary.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = (1.2 * primaryChroma / MaterialYouStandard.sRGBLightnessChromaMap.maxOf { it.value })
                .coerceAtLeast(1.0) * MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone),
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun secondary(tone: TonalValue): Color = secondary.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) / 3.0,
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun tertiary(tone: TonalValue): Color = tertiary.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) * 2.0 / 3.0,
            h = hue + 60.0,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun neutral(tone: TonalValue): Color = neutral.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) / 12.0,
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun neutralVariant(tone: TonalValue): Color = neutralVariant.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) / 6.0,
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun error(tone: TonalValue): Color = error.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone),
            h = 33.44,
        ).clampToRgb().toColor()
    }

    companion object {
        @Composable
        fun Color.toTonalPalettes(): TonalPalettes {
            val zcam = toRgb().toZcam()
            return TonalPalettes(hue = zcam.hz, primaryChroma = zcam.Cz)
        }
    }
}
