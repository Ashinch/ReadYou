package me.ash.reader.infrastructure.preference

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
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
import me.ash.reader.ui.ext.collectAsStateValue
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

    @Composable
    fun ProvidesSettings(content: @Composable () -> Unit) {
        val settings = settingsFlow.collectAsStateValue()
        CompositionLocalProvider(
            LocalSettings provides settings,

            // Version
            NewVersionNumberPreference.provide(settings),
            LocalSkipVersionNumber provides settings.skipVersionNumber,
            LocalNewVersionPublishDate provides settings.newVersionPublishDate,
            LocalNewVersionLog provides settings.newVersionLog,
            LocalNewVersionSize provides settings.newVersionSize,
            LocalNewVersionDownloadUrl provides settings.newVersionDownloadUrl,
            LocalBasicFonts provides settings.basicFonts,

            // Theme
            LocalThemeIndex provides settings.themeIndex,
            LocalCustomPrimaryColor provides settings.customPrimaryColor,
            LocalDarkTheme provides settings.darkTheme,
            LocalAmoledDarkTheme provides settings.amoledDarkTheme,
            LocalBasicFonts provides settings.basicFonts,

            // Feeds page
            LocalFeedsTopBarTonalElevation provides settings.feedsTopBarTonalElevation,
            LocalFeedsGroupListExpand provides settings.feedsGroupListExpand,
            LocalFeedsGroupListTonalElevation provides settings.feedsGroupListTonalElevation,
            LocalFeedsFilterBarStyle provides settings.feedsFilterBarStyle,
            LocalFeedsFilterBarFilled provides settings.feedsFilterBarFilled,
            LocalFeedsFilterBarPadding provides settings.feedsFilterBarPadding,
            LocalFeedsFilterBarTonalElevation provides settings.feedsFilterBarTonalElevation,

            // Flow page
            LocalFlowTopBarTonalElevation provides settings.flowTopBarTonalElevation,
            LocalFlowArticleListFeedIcon provides settings.flowArticleListFeedIcon,
            LocalFlowArticleListFeedName provides settings.flowArticleListFeedName,
            LocalFlowArticleListImage provides settings.flowArticleListImage,
            LocalFlowArticleListDesc provides settings.flowArticleListDesc,
            LocalFlowArticleListTime provides settings.flowArticleListTime,
            LocalFlowArticleListDateStickyHeader provides settings.flowArticleListDateStickyHeader,
            LocalFlowArticleListTonalElevation provides settings.flowArticleListTonalElevation,
            LocalFlowFilterBarStyle provides settings.flowFilterBarStyle,
            LocalFlowFilterBarFilled provides settings.flowFilterBarFilled,
            LocalFlowFilterBarPadding provides settings.flowFilterBarPadding,
            LocalFlowFilterBarTonalElevation provides settings.flowFilterBarTonalElevation,
            LocalFlowArticleListReadIndicator provides settings.flowArticleListReadIndicator,
            LocalSortUnreadArticles provides settings.flowSortUnreadArticles,

            // Reading page
            LocalReadingRenderer provides settings.readingRenderer,
            LocalReadingBionicReading provides settings.readingBionicReading,
            LocalReadingTheme provides settings.readingTheme,
            LocalReadingPageTonalElevation provides settings.readingPageTonalElevation,
            LocalReadingAutoHideToolbar provides settings.readingAutoHideToolbar,
            LocalReadingTextFontSize provides settings.readingTextFontSize,
            LocalReadingTextLineHeight provides settings.readingTextLineHeight,
            LocalReadingTextLetterSpacing provides settings.readingLetterSpacing,
            LocalReadingTextHorizontalPadding provides settings.readingTextHorizontalPadding,
            LocalReadingTextAlign provides settings.readingTextAlign,
            LocalReadingTextBold provides settings.readingTextBold,
            LocalReadingTitleAlign provides settings.readingTitleAlign,
            LocalReadingSubheadAlign provides settings.readingSubheadAlign,
            LocalReadingFonts provides settings.readingFonts,
            LocalReadingTitleBold provides settings.readingTitleBold,
            LocalReadingSubheadBold provides settings.readingSubheadBold,
            LocalReadingTitleUpperCase provides settings.readingTitleUpperCase,
            LocalReadingSubheadUpperCase provides settings.readingSubheadUpperCase,
            LocalReadingImageHorizontalPadding provides settings.readingImageHorizontalPadding,
            LocalReadingImageRoundedCorners provides settings.readingImageRoundedCorners,
            LocalReadingImageMaximize provides settings.readingImageMaximize,

            // Interaction
            LocalInitialPage provides settings.initialPage,
            LocalInitialFilter provides settings.initialFilter,
            LocalArticleListSwipeStartAction provides settings.swipeStartAction,
            LocalArticleListSwipeEndAction provides settings.swipeEndAction,
            LocalMarkAsReadOnScroll provides settings.markAsReadOnScroll,
            LocalHideEmptyGroups provides settings.hideEmptyGroups,
            LocalPullToSwitchArticle provides settings.pullToSwitchArticle,
            LocalOpenLink provides settings.openLink,
            LocalOpenLinkSpecificBrowser provides settings.openLinkSpecificBrowser,
            LocalSharedContent provides settings.sharedContent,

            // Languages
            LocalLanguages provides settings.languages,
        ) {
            content()
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