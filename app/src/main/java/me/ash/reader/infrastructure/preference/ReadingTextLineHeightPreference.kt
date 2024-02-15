package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

data object ReadingTextLineHeightPreference {
    const val default = 1f
    private val range = 0.8f..2f

    fun put(context: Context, scope: CoroutineScope, value: Float) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.ReadingLineHeight, value)
        }
    }

    fun Float.coerceToRange() = coerceIn(range)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKeys.ReadingLineHeight.key] ?: default
}