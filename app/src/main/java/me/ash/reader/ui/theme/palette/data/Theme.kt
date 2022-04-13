/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.data

import me.ash.reader.ui.theme.palette.TonalPalettes

data class Theme(
    val hue: Double,
    val primaryChroma: Double,
) {
    fun toTonalPalettes(): TonalPalettes = TonalPalettes(
        hue = hue,
        primaryChroma = primaryChroma,
    )

    companion object {
        fun TonalPalettes.toTheme(): Theme = Theme(
            hue = hue,
            primaryChroma = primaryChroma,
        )
    }
}
