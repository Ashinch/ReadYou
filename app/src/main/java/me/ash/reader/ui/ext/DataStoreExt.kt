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

    // Version
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

    object NewVersionSize : DataStoreKeys<String>() {

        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("newVersionSizeString")
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

    object BasicFonts : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("basicFonts")
    }

    // Feeds page
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

    // Flow page
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

    object FlowArticleListReadIndicator : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("flowArticleListReadIndicator")
    }

    // Reading page
    object ReadingDarkTheme : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingDarkTheme")
    }

    object ReadingPageTonalElevation : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("ReadingPageTonalElevation")
    }

    object ReadingTextFontSize : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingTextFontSize")
    }

    object ReadingLineHeight : DataStoreKeys<Float>() {

        override val key: Preferences.Key<Float>
            get() = floatPreferencesKey("readingTextLineHeight")
    }

    object ReadingLetterSpacing : DataStoreKeys<Double>() {

        override val key: Preferences.Key<Double>
            get() = doublePreferencesKey("readingLetterSpacing")
    }

    object ReadingTextHorizontalPadding : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingTextHorizontalPadding")
    }

    object ReadingTextBold : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("readingTextBold")
    }

    object ReadingTextAlign : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingTextAlign")
    }

    object ReadingTitleAlign : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingTitleAlign")
    }

    object ReadingSubheadAlign : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingSubheadAlign")
    }

    object ReadingTheme : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingTheme")
    }

    object ReadingFonts : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingFonts")
    }

    object ReadingAutoHideToolbar : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("readingAutoHideToolbar")
    }

    object ReadingTitleBold : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("readingTitleBold")
    }

    object ReadingSubheadBold : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("ReadingSubheadBold")
    }

    object ReadingTitleUpperCase : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("readingTitleUpperCase")
    }

    object ReadingSubheadUpperCase : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("ReadingSubheadUpperCase")
    }

    object ReadingImageMaximize : DataStoreKeys<Boolean>() {

        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("readingImageMaximize")
    }

    object ReadingImageHorizontalPadding : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingImageHorizontalPadding")
    }

    object ReadingImageRoundedCorners : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("readingImageRoundedCorners")
    }

    // Interaction
    object InitialPage : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("initialPage")
    }

    object InitialFilter : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("initialFilter")
    }

    data object SwipeStartAction : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("swipeStartAction")
    }

    data object SwipeEndAction : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("swipeEndAction")
    }

    data object PullToSwitchArticle : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("pullToSwitchArticle")
    }

    object OpenLink : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("openLink")
    }

    object OpenLinkAppSpecificBrowser : DataStoreKeys<String>() {

        override val key: Preferences.Key<String>
            get() = stringPreferencesKey("openLppSpecificBrowser")
    }

    // Languages
    object Languages : DataStoreKeys<Int>() {

        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("languages")
    }
}
