package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingSubheadBold
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingSubheadBold =
    compositionLocalOf<ReadingSubheadBoldPreference> { ReadingSubheadBoldPreference.default }

sealed class ReadingSubheadBoldPreference(val value: Boolean) : Preference() {
    object ON : ReadingSubheadBoldPreference(true)
    object OFF : ReadingSubheadBoldPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.readingSubheadBold,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingSubheadBold]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingSubheadBoldPreference.not(): ReadingSubheadBoldPreference =
    when (value) {
        true -> ReadingSubheadBoldPreference.OFF
        false -> ReadingSubheadBoldPreference.ON
    }
