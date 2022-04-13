/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction

import kotlin.math.pow

class GammaTransferFunction(
    val gamma: Double, // decoding gamma γ
    val alpha: Double, // offset a, α = a + 1
    val beta: Double, // linear-domain threshold β = K_0 / φ = E_t
    val delta: Double, // linear gain δ = φ
) : TransferFunction {
    override fun EOTF(x: Double): Double = when {
        x >= beta * delta -> ((x + alpha - 1.0) / alpha).pow(gamma) // transition point βδ = K_0
        else -> x / delta
    }

    override fun OETF(x: Double): Double = when {
        x >= beta -> alpha * (x.pow(1.0 / gamma) - 1.0) + 1.0
        else -> x * delta
    }

    companion object {
        /**
         * [Wikipedia: sRGB - Computing the transfer function](https://en.wikipedia.org/wiki/SRGB#Computing_the_transfer_function)
         */
        val sRGB = GammaTransferFunction(
            gamma = 2.4,
            alpha = 1.055,
            beta = 0.055 / 1.4 / ((1.055 / 2.4).pow(2.4) * (1.4 / 0.055).pow(1.4)),
            // ~0.03928571428571429 / ~12.923210180787857 = ~0.0030399346397784314 -> 0.003130804935
            delta = (1.055 / 2.4).pow(2.4) * (1.4 / 0.055).pow(1.4),
            // ~12.923210180787857 -> 12.920020442059
        )

        /**
         * [Rec. 709](https://www.itu.int/rec/R-REC-BT.709)
         */
        val Rec709 = GammaTransferFunction(
            gamma = 2.4,
            alpha = 1.0 + 5.5 * 0.018053968510807, // ~1.09929682680944
            beta = 0.018053968510807,
            delta = 4.5,
        )
    }
}
