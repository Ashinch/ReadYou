package me.ash.reader.ui.page.home.flow

import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.animation.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.size.Precision
import coil.size.Scale
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.constant.ElevationTokens
import me.ash.reader.infrastructure.preference.FlowArticleReadIndicatorPreference
import me.ash.reader.infrastructure.preference.LocalArticleListSwipeEndAction
import me.ash.reader.infrastructure.preference.LocalArticleListSwipeStartAction
import me.ash.reader.infrastructure.preference.LocalFlowArticleListDesc
import me.ash.reader.infrastructure.preference.LocalFlowArticleListFeedIcon
import me.ash.reader.infrastructure.preference.LocalFlowArticleListFeedName
import me.ash.reader.infrastructure.preference.LocalFlowArticleListImage
import me.ash.reader.infrastructure.preference.LocalFlowArticleListReadIndicator
import me.ash.reader.infrastructure.preference.LocalFlowArticleListTime
import me.ash.reader.infrastructure.preference.SwipeEndActionPreference
import me.ash.reader.infrastructure.preference.SwipeStartActionPreference
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.RYAsyncImage
import me.ash.reader.ui.component.base.SIZE_1000
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.Shape20
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun ArticleItem(
    articleWithFeed: ArticleWithFeed,
    onClick: (ArticleWithFeed) -> Unit = {},
) {
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListFeedName = LocalFlowArticleListFeedName.current
    val articleListImage = LocalFlowArticleListImage.current
    val articleListDesc = LocalFlowArticleListDesc.current
    val articleListDate = LocalFlowArticleListTime.current
    val articleListReadIndicator = LocalFlowArticleListReadIndicator.current

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clip(Shape20)
            .clickable { onClick(articleWithFeed) }
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .alpha(articleWithFeed.article.run {
                when (articleListReadIndicator) {
                    FlowArticleReadIndicatorPreference.AllRead -> {
                        if (isUnread) 1f else 0.5f
                    }

                    FlowArticleReadIndicatorPreference.ExcludingStarred -> {
                        if (isUnread || isStarred) 1f else 0.5f
                    }
                }
            }),
    ) {
        // Top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Feed name
            if (articleListFeedName.value) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (articleListFeedIcon.value) 30.dp else 0.dp),
                    text = articleWithFeed.feed.name,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Right
            if (articleListDate.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!articleListFeedName.value) {
                        Spacer(Modifier.width(if (articleListFeedIcon.value) 30.dp else 0.dp))
                    }
                    // Starred
                    if (articleWithFeed.article.isStarred) {
                        Icon(
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 2.dp),
                            imageVector = Icons.Rounded.Star,
                            contentDescription = stringResource(R.string.starred),
                            tint = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }

                    // Date
                    Text(
                        modifier = Modifier,
                        text = articleWithFeed.article.dateString ?: "",
                        color = MaterialTheme.colorScheme.outlineVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        // Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            // Feed icon
            if (articleListFeedIcon.value) {
                FeedIcon(articleWithFeed.feed.name, iconUrl = articleWithFeed.feed.icon)
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Article
            Column(
                modifier = Modifier.weight(1f),
            ) {

                // Title
                Text(
                    text = articleWithFeed.article.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = if (articleListDesc.value) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                )

                // Description
                if (articleListDesc.value && articleWithFeed.article.shortDescription.isNotBlank()) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = articleWithFeed.article.shortDescription,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Image
            if (articleWithFeed.article.img != null && articleListImage.value) {
                RYAsyncImage(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(80.dp)
                        .clip(Shape20),
                    data = articleWithFeed.article.img,
                    scale = Scale.FILL,
                    precision = Precision.INEXACT,
                    size = SIZE_1000,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

private const val PositionalThresholdFraction = 0.15f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableArticleItem(
    articleWithFeed: ArticleWithFeed,
    isFilterUnread: Boolean,
    articleListTonalElevation: Int,
    onClick: (ArticleWithFeed) -> Unit = {},
    isScrollInProgress: () -> Boolean = { false },
    onSwipeStartToEnd: ((ArticleWithFeed) -> Unit)? = null,
    onSwipeEndToStart: ((ArticleWithFeed) -> Unit)? = null,
) {
    val swipeToStartAction = LocalArticleListSwipeStartAction.current
    val swipeToEndAction = LocalArticleListSwipeEndAction.current

    val density = LocalDensity.current
    val confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = {
        when (it) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onSwipeStartToEnd?.invoke(articleWithFeed)
                swipeToEndAction == SwipeEndActionPreference.ToggleRead && isFilterUnread
            }

            SwipeToDismissBoxValue.EndToStart -> {
                onSwipeEndToStart?.invoke(articleWithFeed)
                swipeToStartAction == SwipeStartActionPreference.ToggleRead && isFilterUnread
            }

            SwipeToDismissBoxValue.Settled -> {
                true
            }
        }
    }
    val positionalThreshold: (totalDistance: Float) -> Float = {
        it * PositionalThresholdFraction
    }
    val velocityThreshold: () -> Float = { Float.POSITIVE_INFINITY }
    val animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
    val swipeState = rememberSaveable(
        articleWithFeed.article, saver = SwipeToDismissBoxState.Saver(
            confirmValueChange = confirmValueChange,
            density = density,
            animationSpec = animationSpec,
            velocityThreshold = velocityThreshold,
            positionalThreshold = positionalThreshold
        )
    ) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            animationSpec = animationSpec,
            confirmValueChange = confirmValueChange,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold
        )
    }
    val view = LocalView.current
    var isActive by remember(articleWithFeed) { mutableStateOf(false) }
    LaunchedEffect(swipeState.progress > PositionalThresholdFraction) {
        if (swipeState.progress > PositionalThresholdFraction && swipeState.targetValue != SwipeToDismissBoxValue.Settled) {
            isActive = true
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE)

        } else {
            isActive = false
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        enabled = !isScrollInProgress(),
        /***  create dismiss alert background box */
        backgroundContent = {
            SwipeToDismissBoxBackgroundContent(
                direction = swipeState.dismissDirection,
                isActive = isActive,
                isStarred = articleWithFeed.article.isStarred,
                isRead = !articleWithFeed.article.isUnread
            )
        },
        /**** Dismiss Content */
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            articleListTonalElevation.dp
                        ) onDark MaterialTheme.colorScheme.surface
                    )
            ) {
                ArticleItem(articleWithFeed, onClick)
            }
        },
        /*** Set Direction to dismiss */
        enableDismissFromEndToStart = onSwipeEndToStart != null,
        enableDismissFromStartToEnd = onSwipeStartToEnd != null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.SwipeToDismissBoxBackgroundContent(
    direction: SwipeToDismissBoxValue,
    isActive: Boolean,
    isStarred: Boolean,
    isRead: Boolean,
) {
    val containerColor = MaterialTheme.colorScheme.surface
    val containerColorElevated = MaterialTheme.colorScheme.tertiaryContainer
    val backgroundColor = remember { Animatable(containerColor) }

    LaunchedEffect(isActive) {
        backgroundColor.animateTo(
            if (isActive) {
                containerColorElevated
            } else {
                containerColor
            }
        )
    }
    // FIXME: Remove this once SwipeToDismissBox has proper RTL support
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> if (isRtl) Alignment.CenterEnd else Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> if (isRtl) Alignment.CenterStart else Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    val swipeToStartAction = LocalArticleListSwipeStartAction.current
    val swipeToEndAction = LocalArticleListSwipeEndAction.current

    val starImageVector =
        remember(isStarred) { if (isStarred) Icons.Outlined.StarOutline else Icons.Rounded.Star }

    val readImageVector =
        remember(isRead) { if (isRead) Icons.Outlined.Circle else Icons.Rounded.CheckCircleOutline }

    val starText =
        stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred)

    val readText =
        stringResource(if (isRead) R.string.mark_as_unread else R.string.mark_as_read)

    val imageVector = remember(direction) {
        when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> {

                when (swipeToEndAction) {
                    SwipeEndActionPreference.None -> null
                    SwipeEndActionPreference.ToggleRead -> readImageVector
                    SwipeEndActionPreference.ToggleStarred -> starImageVector
                }
            }

            SwipeToDismissBoxValue.EndToStart -> {
                when (swipeToStartAction) {
                    SwipeStartActionPreference.None -> null
                    SwipeStartActionPreference.ToggleRead -> readImageVector
                    SwipeStartActionPreference.ToggleStarred -> starImageVector
                }
            }

            SwipeToDismissBoxValue.Settled -> null
        }
    }

    val text = remember(direction) {
        when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> {
                when (swipeToEndAction) {
                    SwipeEndActionPreference.None -> null
                    SwipeEndActionPreference.ToggleRead -> readText
                    SwipeEndActionPreference.ToggleStarred -> starText
                }
            }

            SwipeToDismissBoxValue.EndToStart -> {
                when (swipeToStartAction) {
                    SwipeStartActionPreference.None -> null
                    SwipeStartActionPreference.ToggleRead -> readText
                    SwipeStartActionPreference.ToggleStarred -> starText
                }
            }

            SwipeToDismissBoxValue.Settled -> null
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(backgroundColor.value) },
    ) {
        Column(modifier = Modifier.align(alignment = alignment)) {
            imageVector?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp)
                )
            }
        }
    }


}