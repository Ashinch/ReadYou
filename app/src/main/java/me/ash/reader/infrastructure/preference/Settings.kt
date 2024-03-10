package me.ash.reader.infrastructure.preference

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import me.ash.reader.domain.model.general.Version
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.dataStore

data class Settings(
    // Version
    val newVersionNumber: Version = NewVersionNumberPreference.default,
    val skipVersionNumber: Version = SkipVersionNumberPreference.default,
    val newVersionPublishDate: String = NewVersionPublishDatePreference.default,
    val newVersionLog: String = NewVersionLogPreference.default,
    val newVersionSize: String = NewVersionSizePreference.default,
    val newVersionDownloadUrl: String = NewVersionDownloadUrlPreference.default,

    // Theme
    val themeIndex: Int = ThemeIndexPreference.default,
    val customPrimaryColor: String = CustomPrimaryColorPreference.default,
    val darkTheme: DarkThemePreference = DarkThemePreference.default,
    val amoledDarkTheme: AmoledDarkThemePreference = AmoledDarkThemePreference.default,
    val basicFonts: BasicFontsPreference = BasicFontsPreference.default,

    // Feeds page
    val feedsFilterBarStyle: FeedsFilterBarStylePreference = FeedsFilterBarStylePreference.default,
    val feedsFilterBarFilled: FeedsFilterBarFilledPreference = FeedsFilterBarFilledPreference.default,
    val feedsFilterBarPadding: Int = FeedsFilterBarPaddingPreference.default,
    val feedsFilterBarTonalElevation: FeedsFilterBarTonalElevationPreference = FeedsFilterBarTonalElevationPreference.default,
    val feedsTopBarTonalElevation: FeedsTopBarTonalElevationPreference = FeedsTopBarTonalElevationPreference.default,
    val feedsGroupListExpand: FeedsGroupListExpandPreference = FeedsGroupListExpandPreference.default,
    val feedsGroupListTonalElevation: FeedsGroupListTonalElevationPreference = FeedsGroupListTonalElevationPreference.default,

    // Flow page
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
    val flowArticleListReadIndicator: FlowArticleReadIndicatorPreference = FlowArticleReadIndicatorPreference.default,

    // Reading page
    val readingTheme: ReadingThemePreference = ReadingThemePreference.default,
    val readingDarkTheme: ReadingDarkThemePreference = ReadingDarkThemePreference.default,
    val readingPageTonalElevation: ReadingPageTonalElevationPreference = ReadingPageTonalElevationPreference.default,
    val readingAutoHideToolbar: ReadingAutoHideToolbarPreference = ReadingAutoHideToolbarPreference.default,
    val readingTextFontSize: Int = ReadingTextFontSizePreference.default,
    val readingTextLineHeight: Float = ReadingTextLineHeightPreference.default,
    val readingLetterSpacing: Double = ReadingLetterSpacingPreference.default,
    val readingTextHorizontalPadding: Int = ReadingTextHorizontalPaddingPreference.default,
    val readingTextAlign: ReadingTextAlignPreference = ReadingTextAlignPreference.default,
    val readingTextBold: ReadingTextBoldPreference = ReadingTextBoldPreference.default,
    val readingTitleAlign: ReadingTitleAlignPreference = ReadingTitleAlignPreference.default,
    val readingSubheadAlign: ReadingSubheadAlignPreference = ReadingSubheadAlignPreference.default,
    val readingFonts: ReadingFontsPreference = ReadingFontsPreference.default,
    val readingTitleBold: ReadingTitleBoldPreference = ReadingTitleBoldPreference.default,
    val readingSubheadBold: ReadingSubheadBoldPreference = ReadingSubheadBoldPreference.default,
    val readingTitleUpperCase: ReadingTitleUpperCasePreference = ReadingTitleUpperCasePreference.default,
    val readingSubheadUpperCase: ReadingSubheadUpperCasePreference = ReadingSubheadUpperCasePreference.default,
    val readingImageHorizontalPadding: Int = ReadingImageHorizontalPaddingPreference.default,
    val readingImageRoundedCorners: Int = ReadingImageRoundedCornersPreference.default,
    val readingImageMaximize: ReadingImageMaximizePreference = ReadingImageMaximizePreference.default,

    // Interaction
    val initialPage: InitialPagePreference = InitialPagePreference.default,
    val initialFilter: InitialFilterPreference = InitialFilterPreference.default,
    val swipeStartAction: SwipeStartActionPreference = SwipeStartActionPreference.default,
    val swipeEndAction: SwipeEndActionPreference = SwipeEndActionPreference.default,
    val pullToSwitchArticle: PullToSwitchArticlePreference = PullToSwitchArticlePreference.default,
    val openLink: OpenLinkPreference = OpenLinkPreference.default,
    val openLinkSpecificBrowser: OpenLinkSpecificBrowserPreference = OpenLinkSpecificBrowserPreference.default,

    // Languages
    val languages: LanguagesPreference = LanguagesPreference.default,
)

// Version
val LocalNewVersionNumber = compositionLocalOf { NewVersionNumberPreference.default }
val LocalSkipVersionNumber = compositionLocalOf { SkipVersionNumberPreference.default }
val LocalNewVersionPublishDate = compositionLocalOf { NewVersionPublishDatePreference.default }
val LocalNewVersionLog = compositionLocalOf { NewVersionLogPreference.default }
val LocalNewVersionSize = compositionLocalOf { NewVersionSizePreference.default }
val LocalNewVersionDownloadUrl = compositionLocalOf { NewVersionDownloadUrlPreference.default }

// Theme
val LocalThemeIndex =
    compositionLocalOf { ThemeIndexPreference.default }
val LocalCustomPrimaryColor =
    compositionLocalOf { CustomPrimaryColorPreference.default }
val LocalDarkTheme =
    compositionLocalOf<DarkThemePreference> { DarkThemePreference.default }
val LocalAmoledDarkTheme =
    compositionLocalOf<AmoledDarkThemePreference> { AmoledDarkThemePreference.default }
val LocalBasicFonts = compositionLocalOf<BasicFontsPreference> { BasicFontsPreference.default }

// Feeds page
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

// Flow page
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
val LocalFlowArticleListReadIndicator =
    compositionLocalOf<FlowArticleReadIndicatorPreference> { FlowArticleReadIndicatorPreference.default }

// Reading page
val LocalReadingTheme =
    compositionLocalOf<ReadingThemePreference> { ReadingThemePreference.default }
val LocalReadingDarkTheme =
    compositionLocalOf<ReadingDarkThemePreference> { ReadingDarkThemePreference.default }
val LocalReadingPageTonalElevation =
    compositionLocalOf<ReadingPageTonalElevationPreference> { ReadingPageTonalElevationPreference.default }
val LocalReadingAutoHideToolbar =
    compositionLocalOf<ReadingAutoHideToolbarPreference> { ReadingAutoHideToolbarPreference.default }
val LocalReadingTextFontSize = compositionLocalOf { ReadingTextFontSizePreference.default }
val LocalReadingTextLineHeight = compositionLocalOf { ReadingTextLineHeightPreference.default }
val LocalReadingLetterSpacing = compositionLocalOf { ReadingLetterSpacingPreference.default }
val LocalReadingTextHorizontalPadding =
    compositionLocalOf { ReadingTextHorizontalPaddingPreference.default }
val LocalReadingTextAlign =
    compositionLocalOf<ReadingTextAlignPreference> { ReadingTextAlignPreference.default }
val LocalReadingTextBold =
    compositionLocalOf<ReadingTextBoldPreference> { ReadingTextBoldPreference.default }
val LocalReadingTitleAlign =
    compositionLocalOf<ReadingTitleAlignPreference> { ReadingTitleAlignPreference.default }
val LocalReadingSubheadAlign =
    compositionLocalOf<ReadingSubheadAlignPreference> { ReadingSubheadAlignPreference.default }
val LocalReadingFonts =
    compositionLocalOf<ReadingFontsPreference> { ReadingFontsPreference.default }
val LocalReadingTitleBold =
    compositionLocalOf<ReadingTitleBoldPreference> { ReadingTitleBoldPreference.default }
val LocalReadingSubheadBold =
    compositionLocalOf<ReadingSubheadBoldPreference> { ReadingSubheadBoldPreference.default }
val LocalReadingTitleUpperCase =
    compositionLocalOf<ReadingTitleUpperCasePreference> { ReadingTitleUpperCasePreference.default }
val LocalReadingSubheadUpperCase =
    compositionLocalOf<ReadingSubheadUpperCasePreference> { ReadingSubheadUpperCasePreference.default }
val LocalReadingImageHorizontalPadding =
    compositionLocalOf { ReadingImageHorizontalPaddingPreference.default }
val LocalReadingImageRoundedCorners =
    compositionLocalOf { ReadingImageRoundedCornersPreference.default }
val LocalReadingImageMaximize =
    compositionLocalOf<ReadingImageMaximizePreference> { ReadingImageMaximizePreference.default }

// Interaction
val LocalInitialPage = compositionLocalOf<InitialPagePreference> { InitialPagePreference.default }
val LocalInitialFilter =
    compositionLocalOf<InitialFilterPreference> { InitialFilterPreference.default }
val LocalArticleListSwipeEndAction = compositionLocalOf { SwipeEndActionPreference.default }
val LocalArticleListSwipeStartAction = compositionLocalOf { SwipeStartActionPreference.default }
val LocalPullToSwitchArticle = compositionLocalOf { PullToSwitchArticlePreference.default }
val LocalOpenLink =
    compositionLocalOf<OpenLinkPreference> { OpenLinkPreference.default }
val LocalOpenLinkSpecificBrowser =
    compositionLocalOf { OpenLinkSpecificBrowserPreference.default }

// Languages
val LocalLanguages =
    compositionLocalOf<LanguagesPreference> { LanguagesPreference.default }

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
        // Version
        LocalNewVersionNumber provides settings.newVersionNumber,
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

        // Reading page
        LocalReadingTheme provides settings.readingTheme,
        LocalReadingDarkTheme provides settings.readingDarkTheme,
        LocalReadingPageTonalElevation provides settings.readingPageTonalElevation,
        LocalReadingAutoHideToolbar provides settings.readingAutoHideToolbar,
        LocalReadingTextFontSize provides settings.readingTextFontSize,
        LocalReadingTextLineHeight provides settings.readingTextLineHeight,
        LocalReadingLetterSpacing provides settings.readingLetterSpacing,
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
        LocalPullToSwitchArticle provides settings.pullToSwitchArticle,
        LocalOpenLink provides settings.openLink,
        LocalOpenLinkSpecificBrowser provides settings.openLinkSpecificBrowser,

        // Languages
        LocalLanguages provides settings.languages,
    ) {
        content()
    }
}

