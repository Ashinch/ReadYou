package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.general.Version
import me.ash.reader.domain.model.general.toVersion
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.skipVersionNumber
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalSkipVersionNumber = compositionLocalOf { SkipVersionNumberPreference.default }

object SkipVersionNumberPreference {

    val default = Version()

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(DataStoreKey.skipVersionNumber, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[skipVersionNumber]?.key as Preferences.Key<String>].toVersion()
}
