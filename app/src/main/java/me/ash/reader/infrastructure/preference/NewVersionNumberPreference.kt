package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.general.Version
import me.ash.reader.domain.model.general.toVersion
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.newVersionNumber
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalNewVersionNumber = compositionLocalOf { NewVersionNumberPreference.default }

object NewVersionNumberPreference {

    val provide: (Settings) -> ProvidedValue<Version> =
        fun(settings: Settings) = LocalNewVersionNumber provides settings.newVersionNumber

    val default = Version()

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(newVersionNumber, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[newVersionNumber]?.key as Preferences.Key<String>].toVersion()
}
