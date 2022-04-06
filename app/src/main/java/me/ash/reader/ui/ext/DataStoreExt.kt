package me.ash.reader.ui.ext

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val Context.isFirstLaunch: Boolean
    get() = this.dataStore.get(DataStoreKeys.IsFirstLaunch) ?: true
val Context.currentAccountId: Int
    get() = this.dataStore.get(DataStoreKeys.CurrentAccountId)!!
val Context.currentAccountType: Int
    get() = this.dataStore.get(DataStoreKeys.CurrentAccountType)!!

suspend fun <T> DataStore<Preferences>.put(dataStoreKeys: DataStoreKeys<T>, value: T) {
    this.edit {
        it[dataStoreKeys.key] = value
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> DataStore<Preferences>.get(dataStoreKeys: DataStoreKeys<T>): T? {
    return runBlocking {
        this@get.data.catch { exception ->
            if (exception is IOException) {
                Log.e("RLog", "Get data store error $exception")
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            it[dataStoreKeys.key]
        }.first() as T
    }
}

sealed class DataStoreKeys<T> {
    abstract val key: Preferences.Key<T>

    object IsFirstLaunch : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("isFirstLaunch")
    }

    object CurrentAccountId : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("currentAccountId")
    }

    object CurrentAccountType : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("currentAccountType")
    }
}