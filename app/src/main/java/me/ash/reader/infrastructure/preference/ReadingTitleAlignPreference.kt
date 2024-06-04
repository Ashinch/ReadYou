package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTitleAlign
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingTitleAlign =
    compositionLocalOf<ReadingTitleAlignPreference> { ReadingTitleAlignPreference.default }

sealed class ReadingTitleAlignPreference(val value: Int) : Preference() {
    object Start : ReadingTitleAlignPreference(0)
    object End : ReadingTitleAlignPreference(1)
    object Center : ReadingTitleAlignPreference(2)
    object Justify : ReadingTitleAlignPreference(3)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.readingTitleAlign,
                value
            )
        }
    }

    @Stable
    fun toDesc(context: Context): String =
        when (this) {
            Start -> context.getString(R.string.align_start)
            End -> context.getString(R.string.align_end)
            Center -> context.getString(R.string.center_text)
            Justify -> context.getString(R.string.justify)
        }

    @Stable
    fun toTextAlign(): TextAlign =
        when (this) {
            Start -> TextAlign.Start
            End -> TextAlign.End
            Center -> TextAlign.Center
            Justify -> TextAlign.Justify
        }

    companion object {

        val default = Start
        val values = listOf(Start, End, Center, Justify)

        fun fromPreferences(preferences: Preferences): ReadingTitleAlignPreference =
            when (preferences[DataStoreKey.keys[readingTitleAlign]?.key as Preferences.Key<Int>]) {
                0 -> Start
                1 -> End
                2 -> Center
                3 -> Justify
                else -> default
            }
    }
}
