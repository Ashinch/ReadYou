package me.ash.reader.infrastructure.preference

import android.content.Context
import android.util.Log
import androidx.compose.ui.util.unpackInt1
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import java.io.IOException
import javax.inject.Inject

class SettingsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope coroutineScope: CoroutineScope,
    @IODispatcher ioDispatcher: CoroutineDispatcher
) {
    private val _settingsFlow = MutableStateFlow(Settings())
    val settingsFlow: StateFlow<Settings> = _settingsFlow
    val settings: Settings get() = settingsFlow.value

    val dataStore = context.dataStore

    val preferences get() = runBlocking { dataStore.data.first() }

    inline fun <reified T> get(key: String): T? = preferences.get(key)

    inline fun <reified T> getOrDefault(key: String, default: T): T =
        preferences.getOrDefault(key, default) ?: default


    init {
        coroutineScope.launch(ioDispatcher) {
            dataStore.data.collect {
                _settingsFlow.value = it.toSettings()
            }
        }
    }
}

inline fun <reified T> Preferences.get(key: String): T? {
    val dataStoreKey = DataStoreKey.keys[key] ?: return null
    val key = if (dataStoreKey.type::class == T::class) {
        dataStoreKey.key as Preferences.Key<T>
    } else {
        return null
    }
    return get(key)
}

inline fun <reified T> Preferences.getOrDefault(key: String, default: T): T {
    val dataStoreKey = DataStoreKey.keys[key] ?: return default
    val key = if (dataStoreKey.type::class == T::class) {
        dataStoreKey.key as Preferences.Key<T>
    } else {
        return default
    }
    return get(key) ?: default
}