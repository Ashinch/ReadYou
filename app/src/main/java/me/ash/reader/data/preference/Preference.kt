package me.ash.reader.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope

sealed class Preference {
    abstract fun put(context: Context, scope: CoroutineScope)
}

fun Preferences.toSettings(): Settings {
    return Settings(
        themeIndex = ThemeIndexPreference.fromPreferences(this),
        customPrimaryColor = CustomPrimaryColorPreference.fromPreferences(this),
        darkTheme = DarkThemePreference.fromPreferences(this),
        amoledDarkTheme = AmoledDarkThemePreference.fromPreferences(this),

        feedsFilterBarStyle = FeedsFilterBarStylePreference.fromPreferences(this),
        feedsFilterBarFilled = FeedsFilterBarFilledPreference.fromPreferences(this),
        feedsFilterBarPadding = FeedsFilterBarPaddingPreference.fromPreferences(this),
        feedsFilterBarTonalElevation = FeedsFilterBarTonalElevationPreference.fromPreferences(this),
        feedsTopBarTonalElevation = FeedsTopBarTonalElevationPreference.fromPreferences(this),
        feedsGroupListExpand = FeedsGroupListExpandPreference.fromPreferences(this),
        feedsGroupListTonalElevation = FeedsGroupListTonalElevationPreference.fromPreferences(this),

        flowFilterBarStyle = FlowFilterBarStylePreference.fromPreferences(this),
        flowFilterBarFilled = FlowFilterBarFilledPreference.fromPreferences(this),
        flowFilterBarPadding = FlowFilterBarPaddingPreference.fromPreferences(this),
        flowFilterBarTonalElevation = FlowFilterBarTonalElevationPreference.fromPreferences(this),
        flowTopBarTonalElevation = FlowTopBarTonalElevationPreference.fromPreferences(this),
        flowArticleListFeedIcon = FlowArticleListFeedIconPreference.fromPreferences(this),
        flowArticleListFeedName = FlowArticleListFeedNamePreference.fromPreferences(this),
        flowArticleListImage = FlowArticleListImagePreference.fromPreferences(this),
        flowArticleListDesc = FlowArticleListDescPreference.fromPreferences(this),
        flowArticleListTime = FlowArticleListTimePreference.fromPreferences(this),
        flowArticleListDateStickyHeader = FlowArticleListDateStickyHeaderPreference.fromPreferences(
            this
        ),
        flowArticleListTonalElevation = FlowArticleListTonalElevationPreference.fromPreferences(this),

        languages = LanguagesPreference.fromPreferences(this),
    )
}