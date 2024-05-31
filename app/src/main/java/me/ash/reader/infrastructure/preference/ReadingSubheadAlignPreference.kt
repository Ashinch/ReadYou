package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingSubheadAlign
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingSubheadAlign =
    compositionLocalOf<ReadingSubheadAlignPreference> { ReadingSubheadAlignPreference.default }

sealed class ReadingSubheadAlignPreference(val value: Int) : Preference() {
    object Left : ReadingSubheadAlignPreference(0)
    object Right : ReadingSubheadAlignPreference(1)
    object Center : ReadingSubheadAlignPreference(2)
    object Justify : ReadingSubheadAlignPreference(3)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.readingSubheadAlign,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Left -> context.getString(R.string.align_left)
            Right -> context.getString(R.string.align_right)
            Center -> context.getString(R.string.center_text)
            Justify -> context.getString(R.string.justify)
        }

    fun toTextAlign(): TextAlign =
        when (this) {
            Left -> TextAlign.Start
            Right -> TextAlign.End
            Center -> TextAlign.Center
            Justify -> TextAlign.Justify
        }

    companion object {

        val default = Left
        val values = listOf(Left, Right, Center, Justify)

        fun fromPreferences(preferences: Preferences): ReadingSubheadAlignPreference =
            when (preferences[DataStoreKey.keys[readingSubheadAlign]?.key as Preferences.Key<Int>]) {
                0 -> Left
                1 -> Right
                2 -> Center
                3 -> Justify
                else -> default
            }
    }
}
