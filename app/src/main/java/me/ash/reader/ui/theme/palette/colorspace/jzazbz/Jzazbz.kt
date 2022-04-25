/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.jzazbz

import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz
import me.ash.reader.ui.theme.palette.util.Matrix3
import kotlin.math.pow

data class Jzazbz(
    val Jz: Double,
    val az: Double,
    val bz: Double,
) {
    fun toXyz(): CieXyz {
        val (x_, y_, z) = lmsToXyz * (
                IzazbzToLms * doubleArrayOf(
                    (Jz + d_0) / (1.0 + d - d * (Jz + d_0)),
                    az,
                    bz,
                )
                ).map {
                10000.0 * ((c_1 - it.pow(1.0 / p)) / (c_3 * it.pow(1.0 / p) - c_2)).pow(1.0 / n)
            }.toDoubleArray()
        val x = (x_ + (b - 1.0) * z) / b
        val y = (y_ + (g - 1.0) * x) / g
        return CieXyz(
            x = x,
            y = y,
            z = z,
        )
    }

    companion object {
        private const val b = 1.15
        private const val g = 0.66
        private const val c_1 = 3424.0 / 4096.0
        private const val c_2 = 2413.0 / 128.0
        private const val c_3 = 2392.0 / 128.0
        private const val n = 2610.0 / 16384.0
        private const val p = 1.7 * 2523.0 / 32.0
        private const val d = -0.56
        private const val d_0 = 1.6295499532821566E-11

        private val xyzToLms: Matrix3 = Matrix3(
            doubleArrayOf(0.41478972, 0.579999, 0.01464800),
            doubleArrayOf(-0.2015100, 1.120649, 0.05310080),
            doubleArrayOf(-0.0166008, 0.264800, 0.66847990),
        )
        private val lmsToXyz: Matrix3 = xyzToLms.inverse()
        private val lmsToIzazbz: Matrix3 = Matrix3(
            doubleArrayOf(0.5, 0.5, 0.0),
            doubleArrayOf(3.524000, -4.066708, 0.542708),
            doubleArrayOf(0.199076, 1.096799, -1.295875),
        )
        private val IzazbzToLms: Matrix3 = lmsToIzazbz.inverse()

        fun CieXyz.toJzazbz(): Jzazbz {
            val (Iz, az, bz) = lmsToIzazbz * (
                    xyzToLms * doubleArrayOf(
                        b * x - (b - 1.0) * z,
                        g * y - (g - 1.0) * x,
                        z,
                    )
                    ).map {
                    ((c_1 + c_2 * (it / 10000.0).pow(n)) / (1.0 + c_3 * (it / 10000.0).pow(n))).pow(
                        p
                    )
                }.toDoubleArray()
            return Jzazbz(
                Jz = (1.0 + d) * Iz / (1.0 + d * Iz) - d_0,
                az = az,
                bz = bz,
            )
        }
    }
}
