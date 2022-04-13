/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction

import kotlin.math.pow

/**
 * [Rec. 2100](https://www.itu.int/rec/R-REC-BT.2100)
 */
class PQTransferFunction : TransferFunction {
    companion object {
        private val m_1 = 2610.0 / 16384.0 // 0.1593017578125
        private val m_2 = 2523.0 / 4096.0 * 128.0 // 78.84375
        private val c_1 = 3424.0 / 4096.0 // 0.8359375 = c_3 − c_2 + 1
        private val c_2 = 2413.0 / 4096.0 * 32.0 // 18.8515625
        private val c_3 = 2392.0 / 4096.0 * 32.0 // 18.6875
    }

    override fun EOTF(x: Double): Double =
        10000.0 * ((x.pow(1.0 / m_2).coerceAtLeast(0.0)) / (c_2 - c_3 * x.pow(1.0 / m_2))).pow(1.0 / m_1)

    override fun OETF(x: Double): Double = ((c_1 + c_2 * x / 10000.0) / (1 + c_3 * x / 10000.0)).pow(
        m_2
    )
}
