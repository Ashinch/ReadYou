package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.ExternalFonts
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.theme.SystemTypography

sealed class BasicFontsPreference(val value: Int) : Preference() {
    object System : BasicFontsPreference(0)
    object External : BasicFontsPreference(5)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.BasicFonts, value)
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            System -> context.getString(R.string.system_default)
            External -> context.getString(R.string.external_fonts)
        }

    fun asFontFamily(context: Context): FontFamily =
        when (this) {
            System -> FontFamily.Default
            External -> ExternalFonts.loadBasicTypography(context).displayLarge.fontFamily ?: FontFamily.Default
        }

    fun asTypography(context: Context): Typography =
        when (this) {
            System -> SystemTypography
            External -> ExternalFonts.loadBasicTypography(context)
        }

    companion object {

        val default = System
        val values = listOf(System, External)

        fun fromPreferences(preferences: Preferences): BasicFontsPreference =
            when (preferences[DataStoreKeys.BasicFonts.key]) {
                0 -> System
                5 -> External
                else -> default
            }
    }
}
