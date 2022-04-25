/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.rgb

import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz
import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz.Companion.asXyz
import me.ash.reader.ui.theme.palette.util.div

data class Rgb(
    val r: Double,
    val g: Double,
    val b: Double,
    val colorSpace: RgbColorSpace,
) {
    inline val rgb: DoubleArray
        get() = doubleArrayOf(r, g, b)

    fun isInGamut(): Boolean = rgb.map { it in colorSpace.componentRange }.all { it }

    fun clamp(): Rgb =
        rgb.map { it.coerceIn(colorSpace.componentRange) }.toDoubleArray().asRgb(colorSpace)

    fun toXyz(luminance: Double): CieXyz = (
            colorSpace.rgbToXyzMatrix * rgb.map {
                colorSpace.transferFunction.EOTF(it)
            }.toDoubleArray()
            ).asXyz() * luminance

    override fun toString(): String = "Rgb(r=$r, g=$g, b=$b, colorSpace=${colorSpace.name})"

    companion object {
        fun CieXyz.toRgb(
            luminance: Double,
            colorSpace: RgbColorSpace
        ): Rgb = (colorSpace.rgbToXyzMatrix.inverse() * (xyz / luminance))
            .map { colorSpace.transferFunction.OETF(it) }
            .toDoubleArray().asRgb(colorSpace)

        internal fun DoubleArray.asRgb(colorSpace: RgbColorSpace): Rgb =
            Rgb(this[0], this[1], this[2], colorSpace)
    }
}
