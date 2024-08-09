package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTextLineHeight
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingTextLineHeight = compositionLocalOf { ReadingTextLineHeightPreference.default }

data object ReadingTextLineHeightPreference {
    const val default = 1.5F
    private val range = 0.8F..2F

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingTextLineHeight, value)
        }
    }

    fun Float.coerceToRange() = coerceIn(range)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingTextLineHeight]?.key as Preferences.Key<Float>] ?: default
}
