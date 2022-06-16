/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.oklab

import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz
import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz.Companion.asXyz
import me.ash.reader.ui.theme.palette.util.Matrix3
import kotlin.math.pow

data class Oklab(
    val L: Double,
    val a: Double,
    val b: Double,
) {

    fun toXyz(): CieXyz = (lmsToXyz * (oklabToLms * doubleArrayOf(L, a, b)).map { it.pow(3.0) }
        .toDoubleArray()).asXyz()

    companion object {

        private val xyzToLms: Matrix3 = Matrix3(
            doubleArrayOf(0.8189330101, 0.3618667424, -0.1288597137),
            doubleArrayOf(0.0329845436, 0.9293118715, 0.0361456387),
            doubleArrayOf(0.0482003018, 0.2643662691, 0.6338517070),
        )
        private val lmsToXyz: Matrix3 = xyzToLms.inverse()
        private val lmsToOklab: Matrix3 = Matrix3(
            doubleArrayOf(0.2104542553, 0.7936177850, -0.0040720468),
            doubleArrayOf(1.9779984951, -2.4285922050, 0.4505937099),
            doubleArrayOf(0.0259040371, 0.7827717662, -0.8086757660),
        )
        private val oklabToLms: Matrix3 = lmsToOklab.inverse()

        fun CieXyz.toOklab(): Oklab =
            (lmsToOklab * (xyzToLms * xyz).map { it.pow(1.0 / 3.0) }.toDoubleArray()).asOklab()

        internal fun DoubleArray.asOklab(): Oklab = Oklab(this[0], this[1], this[2])
    }
}
