package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTextAlign
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingTextAlign =
    compositionLocalOf<ReadingTextAlignPreference> { ReadingTextAlignPreference.default }

sealed class ReadingTextAlignPreference(val value: Int) : Preference() {
    object Start : ReadingTextAlignPreference(0)
    object End : ReadingTextAlignPreference(1)
    object Center : ReadingTextAlignPreference(2)
    object Justify : ReadingTextAlignPreference(3)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.readingTextAlign,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Start -> context.getString(R.string.align_start)
            End -> context.getString(R.string.align_end)
            Center -> context.getString(R.string.center_text)
            Justify -> context.getString(R.string.justify)
        }

    fun toTextAlign(): TextAlign =
        when (this) {
            Start -> TextAlign.Start
            End -> TextAlign.End
            Center -> TextAlign.Center
            Justify -> TextAlign.Justify
        }

    fun toTextAlignCSS(): String =
        when (this) {
            Start -> "start"
            End -> "end"
            Center -> "center"
            Justify -> "justify"
        }

    fun toAlignment(): Alignment.Horizontal =
        when (this) {
            Start -> Alignment.Start
            End -> Alignment.End
            Center -> Alignment.CenterHorizontally
            Justify -> Alignment.Start
        }

    companion object {

        val default = Start
        val values = listOf(Start, End, Center, Justify)

        fun fromPreferences(preferences: Preferences): ReadingTextAlignPreference =
            when (preferences[DataStoreKey.keys[readingTextAlign]?.key as Preferences.Key<Int>]) {
                0 -> Start
                1 -> End
                2 -> Center
                3 -> Justify
                else -> default
            }
    }
}
