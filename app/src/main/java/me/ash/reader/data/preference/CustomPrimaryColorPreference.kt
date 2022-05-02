package me.ash.reader.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

object CustomPrimaryColorPreference {
    const val default = ""

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.CustomPrimaryColor, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKeys.CustomPrimaryColor.key] ?: default
}