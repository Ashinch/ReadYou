package me.ash.reader.ui.ext

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

val Context.initialPage: Int
    get() = this.dataStore.get(DataStoreKeys.InitialPage) ?: 0
val Context.initialFilter: Int
    get() = this.dataStore.get(DataStoreKeys.InitialFilter) ?: 2

val Context.languages: Int
    get() = this.dataStore.get(DataStoreKeys.Languages) ?: 0

suspend fun <T> DataStore<Preferences>.put(dataStoreKeys: DataStoreKeys<T>, value: T) {
    this.edit {
        withContext(Dispatchers.IO) {
            it[dataStoreKeys.key] = value
        }
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

    object DarkTheme : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("darkTheme")
    }

    object AmoledDarkTheme : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("amoledDarkTheme")
    }

    object FeedsFilterBarStyle : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("feedsFilterBarStyle")
    }

    object FeedsFilterBarFilled : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("feedsFilterBarFilled")
    }

    object FeedsFilterBarPadding : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("feedsFilterBarPadding")
    }

    object FeedsFilterBarTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("feedsFilterBarTonalElevation")
    }

    object FeedsTopBarTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("feedsTopBarTonalElevation")
    }

    object FeedsGroupListExpand : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("feedsGroupListExpand")
    }

    object FeedsGroupListTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("feedsGroupListTonalElevation")
    }

    object FlowFilterBarStyle : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("flowFilterBarStyle")
    }

    object FlowFilterBarFilled : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowFilterBarFilled")
    }

    object FlowFilterBarPadding : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("flowFilterBarPadding")
    }

    object FlowFilterBarTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("flowFilterBarTonalElevation")
    }

    object FlowTopBarTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("flowTopBarTonalElevation")
    }

    object FlowArticleListFeedIcon : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowArticleListFeedIcon")
    }

    object FlowArticleListFeedName : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowArticleListFeedName")
    }

    object FlowArticleListImage : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowArticleListImage")
    }

    object FlowArticleListDesc : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowArticleListDesc")
    }

    object FlowArticleListTime : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowArticleListTime")
    }

    object FlowArticleListDateStickyHeader : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowArticleListDateStickyHeader")
    }

    object FlowArticleListTonalElevation : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("flowArticleListTonalElevation")
    }

    object InitialPage : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("initialPage")
    }

    object InitialFilter : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("initialFilter")
    }

    object Languages : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("languages")
    }
}