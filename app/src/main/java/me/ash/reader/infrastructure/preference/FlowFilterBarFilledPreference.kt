package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowFilterBarFilled
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowFilterBarFilled =
    compositionLocalOf<FlowFilterBarFilledPreference> { FlowFilterBarFilledPreference.default }

sealed class FlowFilterBarFilledPreference(val value: Boolean) : Preference() {
    object ON : FlowFilterBarFilledPreference(true)
    object OFF : FlowFilterBarFilledPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowFilterBarFilled,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowFilterBarFilled]?.key as Preferences.Key<Boolean>]) {
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
