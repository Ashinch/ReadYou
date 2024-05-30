package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.feedsFilterBarFilled
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFeedsFilterBarFilled =
    compositionLocalOf<FeedsFilterBarFilledPreference> { FeedsFilterBarFilledPreference.default }

sealed class FeedsFilterBarFilledPreference(val value: Boolean) : Preference() {
    object ON : FeedsFilterBarFilledPreference(true)
    object OFF : FeedsFilterBarFilledPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.feedsFilterBarFilled, value)
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[feedsFilterBarFilled]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FeedsFilterBarFilledPreference.not(): FeedsFilterBarFilledPreference =
    when (value) {
        true -> FeedsFilterBarFilledPreference.OFF
        false -> FeedsFilterBarFilledPreference.ON
    }
