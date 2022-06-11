/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.cielab

import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz
import kotlin.math.pow

// TODO: test
data class CieLab(
    val L: Double,
    val a: Double,
    val b: Double,
) {

    fun toXyz(
        whitePoint: CieXyz,
        luminance: Double,
    ): CieXyz {
        val lp = (L + 16.0) / 116.0
        val absoluteWhitePoint = whitePoint * luminance
        return CieXyz(
            x = absoluteWhitePoint.x * fInv(lp + (a / 500.0)),
            y = absoluteWhitePoint.y * fInv(lp),
            z = absoluteWhitePoint.z * fInv(lp - (b / 200.0)),
        )
    }

    companion object {

        private fun f(x: Double) = when {
            x > 216.0 / 24389.0 -> x.pow(1.0 / 3.0)
            else -> x / (108.0 / 841.0) + 4.0 / 29.0
        }

        private fun fInv(x: Double): Double = when {
            x > 6.0 / 29.0 -> x.pow(3.0)
            else -> 108.0 / 841.0 * (x - 4.0 / 29.0)
        }

        fun CieXyz.toCieLab(
            whitePoint: CieXyz,
            luminance: Double,
        ): CieLab {
            val relativeWhitePoint = whitePoint / luminance
            return CieLab(
                L = 116.0 * f(y / relativeWhitePoint.y) - 16.0,
                a = 500.0 * (f(x / relativeWhitePoint.x) - f(y / relativeWhitePoint.y)),
                b = 200.0 * (f(y / relativeWhitePoint.y) - f(z / relativeWhitePoint.z)),
            )
        }
    }
}
