package me.ash.reader.data.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowFilterBarFilledPreference(val value: Boolean) : Preference() {
    object ON : FlowFilterBarFilledPreference(true)
    object OFF : FlowFilterBarFilledPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowFilterBarFilled,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowFilterBarFilled.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowFilterBarFilledPreference.not(): FlowFilterBarFilledPreference =
    when (value) {
        true -> FlowFilterBarFilledPreference.OFF
        false -> FlowFilterBarFilledPreference.ON
    }
