package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTitleBold
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingTitleBold =
    compositionLocalOf<ReadingTitleBoldPreference> { ReadingTitleBoldPreference.default }

sealed class ReadingTitleBoldPreference(val value: Boolean) : Preference() {
    object ON : ReadingTitleBoldPreference(true)
    object OFF : ReadingTitleBoldPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.readingTitleBold,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingTitleBold]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingTitleBoldPreference.not(): ReadingTitleBoldPreference =
    when (value) {
        true -> ReadingTitleBoldPreference.OFF
        false -> ReadingTitleBoldPreference.ON
    }
