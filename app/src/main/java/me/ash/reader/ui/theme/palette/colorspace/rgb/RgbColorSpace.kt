/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.rgb

import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz
import me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction.GammaTransferFunction
import me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction.HLGTransferFunction
import me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction.PQTransferFunction
import me.ash.reader.ui.theme.palette.colorspace.rgb.transferfunction.TransferFunction
import me.ash.reader.ui.theme.palette.data.Illuminant
import me.ash.reader.ui.theme.palette.util.Matrix3

data class RgbColorSpace(
    val name: String,
    val componentRange: ClosedRange<Double>,
    val whitePoint: CieXyz,
    internal val primaries: Matrix3,
    internal val transferFunction: TransferFunction,
) {
    internal val rgbToXyzMatrix: Matrix3
        get() {
            val M1 = Matrix3(
                doubleArrayOf(
                    primaries[0][0] / primaries[0][1],
                    primaries[1][0] / primaries[1][1],
                    primaries[2][0] / primaries[2][1],
                ),
                doubleArrayOf(1.0, 1.0, 1.0),
                doubleArrayOf(
                    primaries[0][2] / primaries[0][1],
                    primaries[1][2] / primaries[1][1],
                    primaries[2][2] / primaries[2][1],
                )
            )
            val M2 = M1.inverse() * whitePoint.xyz
            return Matrix3(
                doubleArrayOf(M1[0][0] * M2[0], M1[0][1] * M2[1], M1[0][2] * M2[2]),
                doubleArrayOf(M1[1][0] * M2[0], M1[1][1] * M2[1], M1[1][2] * M2[2]),
                doubleArrayOf(M1[2][0] * M2[0], M1[2][1] * M2[1], M1[2][2] * M2[2]),
            )
        }

    companion object {
        /**
         * Standard: IEC 61966-2-1
         * - [Wikipedia: sRGB](https://en.wikipedia.org/wiki/SRGB)
         */
        val Srgb: RgbColorSpace = RgbColorSpace(
            name = "sRGB",
            componentRange = 0.0..1.0,
            whitePoint = Illuminant.D65,
            primaries = Matrix3(
                doubleArrayOf(0.64, 0.33, 0.03),
                doubleArrayOf(0.30, 0.60, 0.10),
                doubleArrayOf(0.15, 0.06, 0.79),
            ),
            transferFunction = GammaTransferFunction.sRGB,
        )

        /**
         * Standard: SMPTE EG 432-1
         * - [Wikipedia: DCI-P3](https://en.wikipedia.org/wiki/DCI-P3)
         */
        val DisplayP3: RgbColorSpace = RgbColorSpace(
            name = "Display P3",
            componentRange = 0.0..1.0,
            whitePoint = Illuminant.D65,
            primaries = Matrix3(
                doubleArrayOf(0.68, 0.32, 0.00),
                doubleArrayOf(0.265, 0.690, 0.045),
                doubleArrayOf(0.15, 0.06, 0.79),
            ),
            transferFunction = GammaTransferFunction.sRGB,
        )

        /**
         * Standard: ITU-R BT.2020
         * - [Wikipedia: Rec. 2020](https://en.wikipedia.org/wiki/Rec._2020)
         */
        val BT2020: RgbColorSpace = RgbColorSpace(
            name = "BT.2020",
            componentRange = 0.0..1.0,
            whitePoint = Illuminant.D65,
            primaries = Matrix3(
                doubleArrayOf(0.708, 0.292, 0.000),
                doubleArrayOf(0.170, 0.797, 0.033),
                doubleArrayOf(0.131, 0.046, 0.823),
            ),
            transferFunction = GammaTransferFunction.Rec709,
        )

        /**
         * Standard: ITU-R BT.2100
         * - [Wikipedia: Rec. 2100](https://en.wikipedia.org/wiki/Rec._2100)
         */
        val BT2100PQ: RgbColorSpace = RgbColorSpace(
            name = "BT.2100 (PQ)",
            componentRange = 0.0..1.0,
            whitePoint = Illuminant.D65,
            primaries = Matrix3(
                doubleArrayOf(0.708, 0.292, 0.000),
                doubleArrayOf(0.170, 0.797, 0.033),
                doubleArrayOf(0.131, 0.046, 0.823),
            ),
            transferFunction = PQTransferFunction(),
        )

        /**
         * Standard: ITU-R BT.2100
         * - [Wikipedia: Rec. 2100](https://en.wikipedia.org/wiki/Rec._2100)
         */
        val BT2100HLG: RgbColorSpace = RgbColorSpace(
            name = "BT.2100 (HLG)",
            componentRange = 0.0..1.0,
            whitePoint = Illuminant.D65,
            primaries = Matrix3(
                doubleArrayOf(0.708, 0.292, 0.000),
                doubleArrayOf(0.170, 0.797, 0.033),
                doubleArrayOf(0.131, 0.046, 0.823),
            ),
            transferFunction = HLGTransferFunction(),
        )
    }
}
