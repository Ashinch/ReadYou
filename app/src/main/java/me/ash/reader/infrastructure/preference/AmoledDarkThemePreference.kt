package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.amoledDarkTheme
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalAmoledDarkTheme =
    compositionLocalOf<AmoledDarkThemePreference> { AmoledDarkThemePreference.default }

sealed class AmoledDarkThemePreference(val value: Boolean) : Preference() {
    object ON : AmoledDarkThemePreference(true)
    object OFF : AmoledDarkThemePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.amoledDarkTheme, value)
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[amoledDarkTheme]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun AmoledDarkThemePreference.not(): AmoledDarkThemePreference =
    when (value) {
        true -> AmoledDarkThemePreference.OFF
        false -> AmoledDarkThemePreference.ON
    }
