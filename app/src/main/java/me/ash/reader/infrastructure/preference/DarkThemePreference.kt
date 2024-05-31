package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.darkTheme
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalDarkTheme =
    compositionLocalOf<DarkThemePreference> { DarkThemePreference.default }

sealed class DarkThemePreference(val value: Int) : Preference() {
    object UseDeviceTheme : DarkThemePreference(0)
    object ON : DarkThemePreference(1)
    object OFF : DarkThemePreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(darkTheme, value)
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            UseDeviceTheme -> context.getString(R.string.use_device_theme)
            ON -> context.getString(R.string.on)
            OFF -> context.getString(R.string.off)
        }

    @Composable
    @ReadOnlyComposable
    fun isDarkTheme(): Boolean = when (this) {
        UseDeviceTheme -> isSystemInDarkTheme()
        ON -> true
        OFF -> false
    }

    companion object {

        val default = UseDeviceTheme
        val values = listOf(UseDeviceTheme, ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[darkTheme]?.key as Preferences.Key<Int>]) {
                0 -> UseDeviceTheme
                1 -> ON
                2 -> OFF
                else -> default
            }
    }
}

@Composable
operator fun DarkThemePreference.not(): DarkThemePreference =
    when (this) {
        DarkThemePreference.UseDeviceTheme -> if (isSystemInDarkTheme()) {
            DarkThemePreference.OFF
        } else {
            DarkThemePreference.ON
        }

        DarkThemePreference.ON -> DarkThemePreference.OFF
        DarkThemePreference.OFF -> DarkThemePreference.ON
    }
