package me.ash.reader.ui.page.settings.color.feeds

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.model.group.Group
import me.ash.reader.infrastructure.preference.FeedsGroupListExpandPreference
import me.ash.reader.infrastructure.preference.FeedsGroupListTonalElevationPreference
import me.ash.reader.infrastructure.preference.FeedsTopBarTonalElevationPreference
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.ext.alphaLN
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.home.feeds.FeedItem
import me.ash.reader.ui.page.home.feeds.GroupItem
import me.ash.reader.ui.theme.palette.onDark
import kotlin.math.ln

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsPagePreview(
    topBarTonalElevation: FeedsTopBarTonalElevationPreference,
    groupListExpand: FeedsGroupListExpandPreference,
    groupListTonalElevation: FeedsGroupListTonalElevationPreference,
    filterBarStyle: Int,
    filterBarFilled: Boolean,
    filterBarPadding: Dp,
    filterBarTonalElevation: Dp,
) {
    var filter by remember { mutableStateOf(Filter.Unread) }
    val feedBadgeAlpha by remember { derivedStateOf { (ln(groupListTonalElevation.value + 1.4f) + 2f) / 100f } }
    val groupAlpha by remember { derivedStateOf { groupListTonalElevation.value.dp.alphaLN(weight = 1.2f) } }
    val groupIndicatorAlpha by remember {
        derivedStateOf {
            groupListTonalElevation.value.dp.alphaLN(
                weight = 1.4f
            )
        }
    }

    Column(
        modifier = Modifier
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    groupListTonalElevation.value.dp
                ) onDark MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                FeedbackIconButton(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            actions = {
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.refresh),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.subscribe),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    topBarTonalElevation.value.dp
                ),
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        GroupItem(
            roundedBottomCorner = { false },
            isExpanded = { groupListExpand.value },
            group = generateGroupPreview(),
            alpha = groupAlpha,
            indicatorAlpha = groupIndicatorAlpha,
        )
        FeedItemExpandSwitcher(
            groupAlpha = groupAlpha,
            feedBadgeAlpha = feedBadgeAlpha,
            isExpanded = groupListExpand.value
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterBar(
            filter = filter,
            filterBarStyle = filterBarStyle,
            filterBarFilled = filterBarFilled,
            filterBarPadding = filterBarPadding,
            filterBarTonalElevation = filterBarTonalElevation,
        ) {
            filter = it
        }
    }
}

@Stable
@Composable
fun FeedItemExpandSwitcher(groupAlpha: Float, feedBadgeAlpha: Float, isExpanded: Boolean) {
    FeedPreview(
        groupAlpha = groupAlpha,
        feedBadgeAlpha = feedBadgeAlpha,
        isExpanded = isExpanded
    )
}

@Stable
@Composable
fun FeedPreview(groupAlpha: Float, feedBadgeAlpha: Float, isExpanded: Boolean) {
    FeedItem(
        feed = generateFeedPreview(),
        alpha = groupAlpha,
        badgeAlpha = feedBadgeAlpha,
        isEnded = { true },
        isExpanded = { isExpanded }
    )
}

@Stable
@Composable
fun generateFeedPreview(): Feed =
    Feed(
        id = "",
        name = stringResource(R.string.preview_feed_name),
        icon = "",
        accountId = 0,
        groupId = "",
        url = "",
    ).apply {
        important = 100
    }

@Stable
@Composable
fun generateGroupPreview(): Group =
    Group(
        id = "",
        name = stringResource(R.string.defaults),
        accountId = 0,
    )
