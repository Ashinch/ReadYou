package me.ash.reader.data.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ReadingImageMaximizePreference(val value: Boolean) : Preference() {
    object ON : ReadingImageMaximizePreference(true)
    object OFF : ReadingImageMaximizePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.ReadingImageMaximize, value)
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.ReadingImageMaximize.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingImageMaximizePreference.not(): ReadingImageMaximizePreference =
    when (value) {
        true -> ReadingImageMaximizePreference.OFF
        false -> ReadingImageMaximizePreference.ON
    }
