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

val Context.newVersionPublishDate: String
    get() = this.dataStore.get(DataStoreKeys.NewVersionPublishDate) ?: ""
val Context.newVersionLog: String
    get() = this.dataStore.get(DataStoreKeys.NewVersionLog) ?: ""
val Context.newVersionSize: Int
    get() = this.dataStore.get(DataStoreKeys.NewVersionSize) ?: 0
val Context.newVersionDownloadUrl: String
    get() = this.dataStore.get(DataStoreKeys.NewVersionDownloadUrl) ?: ""
val Context.newVersionNumber: String
    get() = this.dataStore.get(DataStoreKeys.NewVersionNumber) ?: ""
val Context.skipVersionNumber: String
    get() = this.dataStore.get(DataStoreKeys.SkipVersionNumber) ?: ""
val Context.isFirstLaunch: Boolean
    get() = this.dataStore.get(DataStoreKeys.IsFirstLaunch) ?: true
val Context.currentAccountId: Int
    get() = this.dataStore.get(DataStoreKeys.CurrentAccountId)!!
val Context.currentAccountType: Int
    get() = this.dataStore.get(DataStoreKeys.CurrentAccountType)!!
val Context.themeIndex: Int
    get() = this.dataStore.get(DataStoreKeys.ThemeIndex) ?: 5

val Context.customPrimaryColor: String
    get() = this.dataStore.get(DataStoreKeys.CustomPrimaryColor) ?: ""
val Context.initialPage: Int
    get() = this.dataStore.get(DataStoreKeys.InitialPage) ?: 0
val Context.initialFilter: Int
    get() = this.dataStore.get(DataStoreKeys.InitialFilter) ?: 2

suspend fun <T> DataStore<Preferences>.put(dataStoreKeys: DataStoreKeys<T>, value: T) {
    this.edit {
        it[dataStoreKeys.key] = value
    }
}

fun <T> DataStore<Preferences>.putBlocking(dataStoreKeys: DataStoreKeys<T>, value: T) {
    runBlocking {
        this@putBlocking.edit {
            it[dataStoreKeys.key] = value
        }
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

    object NewVersionPublishDate : DataStoreKeys<String>() {
        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("newVersionPublishDate")
    }

    object NewVersionLog : DataStoreKeys<String>() {
        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("newVersionLog")
    }

    object NewVersionSize : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("newVersionSize")
    }

    object NewVersionDownloadUrl : DataStoreKeys<String>() {
        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("newVersionDownloadUrl")
    }

    object NewVersionNumber : DataStoreKeys<String>() {
        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("newVersionNumber")
    }

    object SkipVersionNumber : DataStoreKeys<String>() {
        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("skipVersionNumber")
    }

    object CurrentAccountId : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("currentAccountId")
    }

    object CurrentAccountType : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("currentAccountType")
    }

    object ThemeIndex : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("themeIndex")
    }

    object CustomPrimaryColor : DataStoreKeys<String>() {
        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("customPrimaryColor")
    }

    object FilterBarStyle : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("filterBarStyle")
    }

    object FilterBarFilled : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("filterBarFilled")
    }

    object FilterBarPadding : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("filterBarPadding")
    }

    object FilterBarTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("filterBarTonalElevation")
    }

    object ArticleListFeedIcon : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("articleListFeedIcon")
    }

    object ArticleListFeedName : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("articleListFeedName")
    }

    object ArticleListImage : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("articleListImage")
    }

    object ArticleListDesc : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("articleListDesc")
    }

    object ArticleListDate : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("articleListDate")
    }

    object ArticleListTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("articleListTonalElevation")
    }

    object InitialPage : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("initialPage")
    }

    object InitialFilter : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("initialFilter")
    }
}