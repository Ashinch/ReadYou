/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.cielab

import me.ash.reader.ui.theme.palette.util.square
import me.ash.reader.ui.theme.palette.util.toDegrees
import me.ash.reader.ui.theme.palette.util.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CieLch(
    val L: Double,
    val C: Double,
    val h: Double,
) {

    fun toCieLab(): CieLab {
        val hRad = h.toRadians()
        return CieLab(
            L = L,
            a = C * cos(hRad),
            b = C * sin(hRad),
        )
    }

    companion object {

        fun CieLab.toCieLch(): CieLch = CieLch(
            L = L,
            C = sqrt(square(a) + square(b)),
            h = atan2(b, a).toDegrees().mod(360.0),
        )
    }
}
