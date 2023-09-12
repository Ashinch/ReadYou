package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.ExternalFonts
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ReadingFontsPreference(val value: Int) : Preference() {
    object System : ReadingFontsPreference(0)
    object Serif : ReadingFontsPreference(1)
    object SansSerif : ReadingFontsPreference(2)
    object Monospace : ReadingFontsPreference(3)
    object Cursive : ReadingFontsPreference(4)
    object External : ReadingFontsPreference(5)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.ReadingFonts, value)
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            System -> context.getString(R.string.system_default)
            Serif -> "Serif"
            SansSerif -> "Sans-Serif"
            Monospace -> "Monospace"
            Cursive -> "Cursive"
            External -> context.getString(R.string.external_fonts)
        }

    fun asFontFamily(context: Context): FontFamily =
        when (this) {
            System -> FontFamily.Default
            Serif -> FontFamily.Serif
            SansSerif -> FontFamily.SansSerif
            Monospace -> FontFamily.Monospace
            Cursive -> FontFamily.Cursive
            External -> ExternalFonts.loadReadingTypography(context).displayLarge.fontFamily ?: FontFamily.Default
        }

    companion object {

        val default = System
        val values = listOf(System, Serif, SansSerif, Monospace, Cursive, External)

        fun fromPreferences(preferences: Preferences): ReadingFontsPreference =
            when (preferences[DataStoreKeys.ReadingFonts.key]) {
                0 -> System
                1 -> Serif
                2 -> SansSerif
                3 -> Monospace
                4 -> Cursive
                5 -> External
                else -> default
            }
    }
}
