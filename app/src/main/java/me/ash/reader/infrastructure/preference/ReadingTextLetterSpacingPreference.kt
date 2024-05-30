package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTextLetterSpacing
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingTextLetterSpacing = compositionLocalOf { ReadingTextLetterSpacingPreference.default }

object ReadingTextLetterSpacingPreference {

    const val default = 0.5F

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingTextLetterSpacing, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingTextLetterSpacing]?.key as Preferences.Key<Float>] ?: default
}
