package me.ash.reader.data.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ReadingSubheadUpperCasePreference(val value: Boolean) : Preference() {
    object ON : ReadingSubheadUpperCasePreference(true)
    object OFF : ReadingSubheadUpperCasePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.ReadingSubheadUpperCase,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.ReadingSubheadUpperCase.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingSubheadUpperCasePreference.not(): ReadingSubheadUpperCasePreference =
    when (value) {
        true -> ReadingSubheadUpperCasePreference.OFF
        false -> ReadingSubheadUpperCasePreference.ON
    }
