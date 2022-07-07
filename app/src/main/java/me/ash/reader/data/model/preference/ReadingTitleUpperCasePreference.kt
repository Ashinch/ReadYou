package me.ash.reader.data.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ReadingTitleUpperCasePreference(val value: Boolean) : Preference() {
    object ON : ReadingTitleUpperCasePreference(true)
    object OFF : ReadingTitleUpperCasePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.ReadingTitleUpperCase,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.ReadingTitleUpperCase.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingTitleUpperCasePreference.not(): ReadingTitleUpperCasePreference =
    when (value) {
        true -> ReadingTitleUpperCasePreference.OFF
        false -> ReadingTitleUpperCasePreference.ON
    }
