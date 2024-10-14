package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingBionicReading
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingBionicReading =
    compositionLocalOf<ReadingBionicReadingPreference> { ReadingBionicReadingPreference.default }

sealed class ReadingBionicReadingPreference(val value: Boolean) : Preference() {
    object ON : ReadingBionicReadingPreference(true)
    object OFF : ReadingBionicReadingPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(readingBionicReading, value)
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingBionicReading]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingBionicReadingPreference.not(): ReadingBionicReadingPreference =
    when (value) {
        true -> ReadingBionicReadingPreference.OFF
        false -> ReadingBionicReadingPreference.ON
    }
