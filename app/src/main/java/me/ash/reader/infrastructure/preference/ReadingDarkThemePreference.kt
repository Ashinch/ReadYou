package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingDarkTheme
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingDarkTheme =
    compositionLocalOf<ReadingDarkThemePreference> { ReadingDarkThemePreference.default }

sealed class ReadingDarkThemePreference(val value: Int) : Preference() {
    object UseAppTheme : ReadingDarkThemePreference(0)
    object ON : ReadingDarkThemePreference(1)
    object OFF : ReadingDarkThemePreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.readingDarkTheme,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            UseAppTheme -> context.getString(R.string.use_app_theme)
            ON -> context.getString(R.string.on)
            OFF -> context.getString(R.string.off)
        }

    @Composable
    @ReadOnlyComposable
    fun isDarkTheme(): Boolean = when (this) {
        UseAppTheme -> LocalDarkTheme.current.isDarkTheme()
        ON -> true
        OFF -> false
    }

    companion object {

        val default = UseAppTheme
        val values = listOf(UseAppTheme, ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingDarkTheme]?.key as Preferences.Key<Int>]) {
                0 -> UseAppTheme
                1 -> ON
                2 -> OFF
                else -> default
            }
    }
}

@Stable
@Composable
@ReadOnlyComposable
operator fun ReadingDarkThemePreference.not(): ReadingDarkThemePreference =
    when (this) {
        ReadingDarkThemePreference.UseAppTheme -> if (LocalDarkTheme.current.isDarkTheme()) {
            ReadingDarkThemePreference.OFF
        } else {
            ReadingDarkThemePreference.ON
        }

        ReadingDarkThemePreference.ON -> ReadingDarkThemePreference.OFF
        ReadingDarkThemePreference.OFF -> ReadingDarkThemePreference.ON
    }
