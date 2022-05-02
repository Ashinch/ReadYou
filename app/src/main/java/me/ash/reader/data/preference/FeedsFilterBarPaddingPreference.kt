package me.ash.reader.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

object FeedsFilterBarPaddingPreference {
    const val default = 60

    fun put(context: Context, scope: CoroutineScope, value: Int) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.FeedsFilterBarPadding, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKeys.FeedsFilterBarPadding.key] ?: default
}