/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.data

import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz

object Illuminant {
    /** CIE Illuminant D65 - standard 2º observer. 6504 K color temperature.
     * Values are calculated from [this table](https://github.com/gpmarques/colorimetry/blob/master/all_1nm_data.xls).
     */
    val D65: CieXyz by lazy {
        CieXyz(
            x = 10043.7000153676 / 10567.0816669881,
            y = 1.0,
            z = 11505.7421788588 / 10567.0816669881,
        )
    }
}
