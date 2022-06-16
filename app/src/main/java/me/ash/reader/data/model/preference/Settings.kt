package me.ash.reader.data.model.preference

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import me.ash.reader.data.model.general.Version
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.dataStore

data class Settings(
    val newVersionNumber: Version = NewVersionNumberPreference.default,
    val skipVersionNumber: Version = SkipVersionNumberPreference.default,
    val newVersionPublishDate: String = NewVersionPublishDatePreference.default,
    val newVersionLog: String = NewVersionLogPreference.default,
    val newVersionSize: String = NewVersionSizePreference.default,
    val newVersionDownloadUrl: String = NewVersionDownloadUrlPreference.default,

    val themeIndex: Int = ThemeIndexPreference.default,
    val customPrimaryColor: String = CustomPrimaryColorPreference.default,
    val darkTheme: DarkThemePreference = DarkThemePreference.default,
    val amoledDarkTheme: AmoledDarkThemePreference = AmoledDarkThemePreference.default,

    val feedsFilterBarStyle: FeedsFilterBarStylePreference = FeedsFilterBarStylePreference.default,
    val feedsFilterBarFilled: FeedsFilterBarFilledPreference = FeedsFilterBarFilledPreference.default,
    val feedsFilterBarPadding: Int = FeedsFilterBarPaddingPreference.default,
    val feedsFilterBarTonalElevation: FeedsFilterBarTonalElevationPreference = FeedsFilterBarTonalElevationPreference.default,
    val feedsTopBarTonalElevation: FeedsTopBarTonalElevationPreference = FeedsTopBarTonalElevationPreference.default,
    val feedsGroupListExpand: FeedsGroupListExpandPreference = FeedsGroupListExpandPreference.default,
    val feedsGroupListTonalElevation: FeedsGroupListTonalElevationPreference = FeedsGroupListTonalElevationPreference.default,

    val flowFilterBarStyle: FlowFilterBarStylePreference = FlowFilterBarStylePreference.default,
    val flowFilterBarFilled: FlowFilterBarFilledPreference = FlowFilterBarFilledPreference.default,
    val flowFilterBarPadding: Int = FlowFilterBarPaddingPreference.default,
    val flowFilterBarTonalElevation: FlowFilterBarTonalElevationPreference = FlowFilterBarTonalElevationPreference.default,
    val flowTopBarTonalElevation: FlowTopBarTonalElevationPreference = FlowTopBarTonalElevationPreference.default,
    val flowArticleListFeedIcon: FlowArticleListFeedIconPreference = FlowArticleListFeedIconPreference.default,
    val flowArticleListFeedName: FlowArticleListFeedNamePreference = FlowArticleListFeedNamePreference.default,
    val flowArticleListImage: FlowArticleListImagePreference = FlowArticleListImagePreference.default,
    val flowArticleListDesc: FlowArticleListDescPreference = FlowArticleListDescPreference.default,
    val flowArticleListTime: FlowArticleListTimePreference = FlowArticleListTimePreference.default,
    val flowArticleListDateStickyHeader: FlowArticleListDateStickyHeaderPreference = FlowArticleListDateStickyHeaderPreference.default,
    val flowArticleListTonalElevation: FlowArticleListTonalElevationPreference = FlowArticleListTonalElevationPreference.default,

    val initialPage: InitialPagePreference = InitialPagePreference.default,
    val initialFilter: InitialFilterPreference = InitialFilterPreference.default,

    val languages: LanguagesPreference = LanguagesPreference.default,
)

@Composable
fun SettingsProvider(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember {
        context.dataStore.data.map {
            Log.i("RLog", "AppTheme: ${it}")
            it.toSettings()
        }
    }.collectAsStateValue(initial = Settings())

    CompositionLocalProvider(
        LocalNewVersionNumber provides settings.newVersionNumber,
        LocalSkipVersionNumber provides settings.skipVersionNumber,
        LocalNewVersionPublishDate provides settings.newVersionPublishDate,
        LocalNewVersionLog provides settings.newVersionLog,
        LocalNewVersionSize provides settings.newVersionSize,
        LocalNewVersionDownloadUrl provides settings.newVersionDownloadUrl,

        LocalThemeIndex provides settings.themeIndex,
        LocalCustomPrimaryColor provides settings.customPrimaryColor,
        LocalDarkTheme provides settings.darkTheme,
        LocalAmoledDarkTheme provides settings.amoledDarkTheme,

        LocalFeedsTopBarTonalElevation provides settings.feedsTopBarTonalElevation,
        LocalFeedsGroupListExpand provides settings.feedsGroupListExpand,
        LocalFeedsGroupListTonalElevation provides settings.feedsGroupListTonalElevation,
        LocalFeedsFilterBarStyle provides settings.feedsFilterBarStyle,
        LocalFeedsFilterBarFilled provides settings.feedsFilterBarFilled,
        LocalFeedsFilterBarPadding provides settings.feedsFilterBarPadding,
        LocalFeedsFilterBarTonalElevation provides settings.feedsFilterBarTonalElevation,

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

        LocalInitialPage provides settings.initialPage,
        LocalInitialFilter provides settings.initialFilter,

        LocalLanguages provides settings.languages,
    ) {
        content()
    }
}

val LocalNewVersionNumber = compositionLocalOf { NewVersionNumberPreference.default }
val LocalSkipVersionNumber = compositionLocalOf { SkipVersionNumberPreference.default }
val LocalNewVersionPublishDate = compositionLocalOf { NewVersionPublishDatePreference.default }
val LocalNewVersionLog = compositionLocalOf { NewVersionLogPreference.default }
val LocalNewVersionSize = compositionLocalOf { NewVersionSizePreference.default }
val LocalNewVersionDownloadUrl = compositionLocalOf { NewVersionDownloadUrlPreference.default }

val LocalThemeIndex =
    compositionLocalOf { ThemeIndexPreference.default }
val LocalCustomPrimaryColor =
    compositionLocalOf { CustomPrimaryColorPreference.default }
val LocalDarkTheme =
    compositionLocalOf<DarkThemePreference> { DarkThemePreference.default }
val LocalAmoledDarkTheme =
    compositionLocalOf<AmoledDarkThemePreference> { AmoledDarkThemePreference.default }

val LocalFeedsFilterBarStyle =
    compositionLocalOf<FeedsFilterBarStylePreference> { FeedsFilterBarStylePreference.default }
val LocalFeedsFilterBarFilled =
    compositionLocalOf<FeedsFilterBarFilledPreference> { FeedsFilterBarFilledPreference.default }
val LocalFeedsFilterBarPadding =
    compositionLocalOf { FeedsFilterBarPaddingPreference.default }
val LocalFeedsFilterBarTonalElevation =
    compositionLocalOf<FeedsFilterBarTonalElevationPreference> { FeedsFilterBarTonalElevationPreference.default }
val LocalFeedsTopBarTonalElevation =
    compositionLocalOf<FeedsTopBarTonalElevationPreference> { FeedsTopBarTonalElevationPreference.default }
val LocalFeedsGroupListExpand =
    compositionLocalOf<FeedsGroupListExpandPreference> { FeedsGroupListExpandPreference.default }
val LocalFeedsGroupListTonalElevation =
    compositionLocalOf<FeedsGroupListTonalElevationPreference> { FeedsGroupListTonalElevationPreference.default }

val LocalFlowFilterBarStyle =
    compositionLocalOf<FlowFilterBarStylePreference> { FlowFilterBarStylePreference.default }
val LocalFlowFilterBarFilled =
    compositionLocalOf<FlowFilterBarFilledPreference> { FlowFilterBarFilledPreference.default }
val LocalFlowFilterBarPadding =
    compositionLocalOf { FlowFilterBarPaddingPreference.default }
val LocalFlowFilterBarTonalElevation =
    compositionLocalOf<FlowFilterBarTonalElevationPreference> { FlowFilterBarTonalElevationPreference.default }
val LocalFlowTopBarTonalElevation =
    compositionLocalOf<FlowTopBarTonalElevationPreference> { FlowTopBarTonalElevationPreference.default }
val LocalFlowArticleListFeedIcon =
    compositionLocalOf<FlowArticleListFeedIconPreference> { FlowArticleListFeedIconPreference.default }
val LocalFlowArticleListFeedName =
    compositionLocalOf<FlowArticleListFeedNamePreference> { FlowArticleListFeedNamePreference.default }
val LocalFlowArticleListImage =
    compositionLocalOf<FlowArticleListImagePreference> { FlowArticleListImagePreference.default }
val LocalFlowArticleListDesc =
    compositionLocalOf<FlowArticleListDescPreference> { FlowArticleListDescPreference.default }
val LocalFlowArticleListTime =
    compositionLocalOf<FlowArticleListTimePreference> { FlowArticleListTimePreference.default }
val LocalFlowArticleListDateStickyHeader =
    compositionLocalOf<FlowArticleListDateStickyHeaderPreference> { FlowArticleListDateStickyHeaderPreference.default }
val LocalFlowArticleListTonalElevation =
    compositionLocalOf<FlowArticleListTonalElevationPreference> { FlowArticleListTonalElevationPreference.default }

val LocalInitialPage = compositionLocalOf<InitialPagePreference> { InitialPagePreference.default }
val LocalInitialFilter =
    compositionLocalOf<InitialFilterPreference> { InitialFilterPreference.default }

val LocalLanguages =
    compositionLocalOf<LanguagesPreference> { LanguagesPreference.default }
