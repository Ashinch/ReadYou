/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction

interface TransferFunction {
    // nonlinear -> linear
    fun EOTF(x: Double): Double

    // linear -> nonlinear
    fun OETF(x: Double): Double
}
