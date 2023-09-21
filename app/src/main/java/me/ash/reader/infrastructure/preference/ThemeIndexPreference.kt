package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

object ThemeIndexPreference {

    const val default = 5

    fun put(context: Context, scope: CoroutineScope, value: Int) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(DataStoreKeys.ThemeIndex, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKeys.ThemeIndex.key] ?: default
}
