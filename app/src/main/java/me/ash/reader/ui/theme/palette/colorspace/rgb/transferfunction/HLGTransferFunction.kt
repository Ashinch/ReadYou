/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction

import me.ash.reader.ui.theme.palette.util.square
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * [Rec. 2100](https://www.itu.int/rec/R-REC-BT.2100)
 */
class HLGTransferFunction : TransferFunction {
    companion object {
        private val a = 0.17883277
        private val b = 1.0 - 4.0 * a // 0.28466892
        private val c = 0.5 - a * ln(4.0 * a) // 0.55991073
    }

    override fun EOTF(x: Double): Double = when (x) {
        in 0.0..1.0 / 2.0 -> 3.0 * square(x)
        in 1.0 / 2.0..1.0 -> (exp((x - c) / a) + b) / 12.0
        else -> Double.NaN
    }

    override fun OETF(x: Double): Double = when (x) {
        in 0.0..1.0 / 12.0 -> sqrt(3.0 * x)
        in 1.0 / 12.0..1.0 -> a * ln(12.0 * x - b) + c
        else -> Double.NaN
    }
}
