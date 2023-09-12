package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

object ReadingLetterSpacingPreference {

    const val default = 0.5

    fun put(context: Context, scope: CoroutineScope, value: Double) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.ReadingLetterSpacing, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKeys.ReadingLetterSpacing.key] ?: default
}
