package me.ash.reader.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import me.ash.reader.data.preference.*
import me.ash.reader.data.preference.FeedsFilterBarFilledPreference.Companion.feedsFilterBarFilled
import me.ash.reader.data.preference.FeedsFilterBarPaddingPreference.feedsFilterBarPadding
import me.ash.reader.data.preference.FeedsFilterBarStylePreference.Companion.feedsFilterBarStyle
import me.ash.reader.data.preference.FeedsFilterBarTonalElevationPreference.Companion.feedsFilterBarTonalElevation
import me.ash.reader.data.preference.FeedsGroupListExpandPreference.Companion.feedsGroupListExpand
import me.ash.reader.data.preference.FeedsGroupListTonalElevationPreference.Companion.feedsGroupListTonalElevation
import me.ash.reader.data.preference.FeedsTopBarTonalElevationPreference.Companion.feedsTopBarTonalElevation
import me.ash.reader.data.preference.FlowArticleListDatePreference.Companion.flowArticleListDate
import me.ash.reader.data.preference.FlowArticleListDescPreference.Companion.flowArticleListDesc
import me.ash.reader.data.preference.FlowArticleListFeedIconPreference.Companion.flowArticleListFeedIcon
import me.ash.reader.data.preference.FlowArticleListFeedNamePreference.Companion.flowArticleListFeedName
import me.ash.reader.data.preference.FlowArticleListImagePreference.Companion.flowArticleListImage
import me.ash.reader.data.preference.FlowArticleListTonalElevationPreference.Companion.flowArticleListTonalElevation
import me.ash.reader.data.preference.FlowFilterBarFilledPreference.Companion.flowFilterBarFilled
import me.ash.reader.data.preference.FlowFilterBarPaddingPreference.flowFilterBarPadding
import me.ash.reader.data.preference.FlowFilterBarStylePreference.Companion.flowFilterBarStyle
import me.ash.reader.data.preference.FlowFilterBarTonalElevationPreference.Companion.flowFilterBarTonalElevation
import me.ash.reader.data.preference.FlowTopBarTonalElevationPreference.Companion.flowTopBarTonalElevation
import me.ash.reader.data.preference.ThemePreference.Theme
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.theme.palette.LocalTonalPalettes
import me.ash.reader.ui.theme.palette.TonalPalettes
import me.ash.reader.ui.theme.palette.core.ProvideZcamViewingConditions
import me.ash.reader.ui.theme.palette.dynamic.extractTonalPalettesFromUserWallpaper
import me.ash.reader.ui.theme.palette.dynamicDarkColorScheme
import me.ash.reader.ui.theme.palette.dynamicLightColorScheme

val LocalUseDarkTheme = compositionLocalOf { false }
val LocalTheme = compositionLocalOf { ThemePreference.default }

val LocalFeedsFilterBarStyle = compositionLocalOf { FeedsFilterBarStylePreference.default.value }
val LocalFeedsFilterBarFilled = compositionLocalOf { FeedsFilterBarFilledPreference.default.value }
val LocalFeedsFilterBarPadding = compositionLocalOf { FeedsFilterBarPaddingPreference.default }
val LocalFeedsFilterBarTonalElevation =
    compositionLocalOf { FeedsFilterBarTonalElevationPreference.default.value }
val LocalFeedsTopBarTonalElevation =
    compositionLocalOf { FeedsTopBarTonalElevationPreference.default.value }
val LocalFeedsGroupListExpand =
    compositionLocalOf { FeedsGroupListExpandPreference.default.value }
val LocalFeedsGroupListTonalElevation =
    compositionLocalOf { FeedsGroupListTonalElevationPreference.default.value }

val LocalFlowFilterBarStyle = compositionLocalOf { FlowFilterBarStylePreference.default.value }
val LocalFlowFilterBarFilled = compositionLocalOf { FlowFilterBarFilledPreference.default.value }
val LocalFlowFilterBarPadding = compositionLocalOf { FlowFilterBarPaddingPreference.default }
val LocalFlowFilterBarTonalElevation =
    compositionLocalOf { FlowFilterBarTonalElevationPreference.default.value }
val LocalFlowTopBarTonalElevation =
    compositionLocalOf { FlowTopBarTonalElevationPreference.default.value }
val LocalFlowArticleListFeedIcon =
    compositionLocalOf { FlowArticleListFeedIconPreference.default.value }
val LocalFlowArticleListFeedName =
    compositionLocalOf { FlowArticleListFeedNamePreference.default.value }
val LocalFlowArticleListImage = compositionLocalOf { FlowArticleListImagePreference.default.value }
val LocalFlowArticleListDesc = compositionLocalOf { FlowArticleListDescPreference.default.value }
val LocalFlowArticleListDate = compositionLocalOf { FlowArticleListDatePreference.default.value }
val LocalFlowArticleListTonalElevation =
    compositionLocalOf { FlowArticleListTonalElevationPreference.default.value }

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    wallpaperPalettes: List<TonalPalettes> = extractTonalPalettesFromUserWallpaper(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val theme = context.Theme.collectAsStateValue(initial = ThemePreference.default)

    val tonalPalettes = wallpaperPalettes[
            if (theme >= wallpaperPalettes.size) {
                when {
                    wallpaperPalettes.size == 5 -> 0
                    wallpaperPalettes.size > 5 -> 5
                    else -> 0
                }
            } else {
                theme
            }
    ]

    val feedsFilterBarStyle =
        context.feedsFilterBarStyle.collectAsStateValue(initial = FeedsFilterBarStylePreference.default)
    val feedsFilterBarFilled =
        context.feedsFilterBarFilled.collectAsStateValue(initial = FeedsFilterBarFilledPreference.default)
    val feedsFilterBarPadding =
        context.feedsFilterBarPadding.collectAsStateValue(initial = FeedsFilterBarPaddingPreference.default)
    val feedsFilterBarTonalElevation =
        context.feedsFilterBarTonalElevation.collectAsStateValue(initial = FeedsFilterBarTonalElevationPreference.default)
    val feedsTopBarTonalElevation =
        context.feedsTopBarTonalElevation.collectAsStateValue(initial = FeedsTopBarTonalElevationPreference.default)
    val feedsGroupListExpand =
        context.feedsGroupListExpand.collectAsStateValue(initial = FeedsGroupListExpandPreference.default)
    val feedsGroupListTonalElevation =
        context.feedsGroupListTonalElevation.collectAsStateValue(initial = FeedsGroupListTonalElevationPreference.default)

    val flowFilterBarStyle =
        context.flowFilterBarStyle.collectAsStateValue(initial = FlowFilterBarStylePreference.default)
    val flowFilterBarFilled =
        context.flowFilterBarFilled.collectAsStateValue(initial = FlowFilterBarFilledPreference.default)
    val flowFilterBarPadding =
        context.flowFilterBarPadding.collectAsStateValue(initial = FlowFilterBarPaddingPreference.default)
    val flowFilterBarTonalElevation =
        context.flowFilterBarTonalElevation.collectAsStateValue(initial = FlowFilterBarTonalElevationPreference.default)
    val flowTopBarTonalElevation =
        context.flowTopBarTonalElevation.collectAsStateValue(initial = FlowTopBarTonalElevationPreference.default)
    val flowArticleListFeedIcon =
        context.flowArticleListFeedIcon.collectAsStateValue(initial = FlowArticleListFeedIconPreference.default)
    val flowArticleListFeedName =
        context.flowArticleListFeedName.collectAsStateValue(initial = FlowArticleListFeedNamePreference.default)
    val flowArticleListImage =
        context.flowArticleListImage.collectAsStateValue(initial = FlowArticleListImagePreference.default)
    val flowArticleListDesc =
        context.flowArticleListDesc.collectAsStateValue(initial = FlowArticleListDescPreference.default)
    val flowArticleListDate =
        context.flowArticleListDate.collectAsStateValue(initial = FlowArticleListDatePreference.default)
    val flowArticleListTonalElevation =
        context.flowArticleListTonalElevation.collectAsStateValue(initial = FlowArticleListTonalElevationPreference.default)

    ProvideZcamViewingConditions {
        CompositionLocalProvider(
            LocalTonalPalettes provides tonalPalettes.also { it.Preheating() },
            LocalUseDarkTheme provides useDarkTheme,
            LocalTheme provides theme,

            LocalFeedsFilterBarStyle provides feedsFilterBarStyle.value,
            LocalFeedsFilterBarFilled provides feedsFilterBarFilled.value,
            LocalFeedsFilterBarPadding provides feedsFilterBarPadding,
            LocalFeedsFilterBarTonalElevation provides feedsFilterBarTonalElevation.value,
            LocalFeedsTopBarTonalElevation provides feedsTopBarTonalElevation.value,
            LocalFeedsGroupListExpand provides feedsGroupListExpand.value,
            LocalFeedsGroupListTonalElevation provides feedsGroupListTonalElevation.value,

            LocalFlowFilterBarStyle provides flowFilterBarStyle.value,
            LocalFlowFilterBarFilled provides flowFilterBarFilled.value,
            LocalFlowFilterBarPadding provides flowFilterBarPadding,
            LocalFlowFilterBarTonalElevation provides flowFilterBarTonalElevation.value,
            LocalFlowTopBarTonalElevation provides flowTopBarTonalElevation.value,
            LocalFlowArticleListFeedIcon provides flowArticleListFeedIcon.value,
            LocalFlowArticleListFeedName provides flowArticleListFeedName.value,
            LocalFlowArticleListImage provides flowArticleListImage.value,
            LocalFlowArticleListDesc provides flowArticleListDesc.value,
            LocalFlowArticleListDate provides flowArticleListDate.value,
            LocalFlowArticleListTonalElevation provides flowArticleListTonalElevation.value,
        ) {
            MaterialTheme(
                colorScheme =
                if (useDarkTheme) dynamicDarkColorScheme()
                else dynamicLightColorScheme(),
                typography = AppTypography,
                content = content
            )
        }
    }
}