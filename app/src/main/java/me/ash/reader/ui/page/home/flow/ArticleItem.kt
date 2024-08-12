package me.ash.reader.ui.page.home.flow

import android.view.HapticFeedbackConstants
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.size.Precision
import coil.size.Scale
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
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
import me.ash.reader.ui.component.menu.AnimatedDropdownMenu
import me.ash.reader.ui.ext.requiresBidi
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.settings.color.flow.generateArticleWithFeedPreview
import me.ash.reader.ui.theme.Shape20
import me.ash.reader.ui.theme.applyTextDirection
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun ArticleItem(
    modifier: Modifier = Modifier,
    articleWithFeed: ArticleWithFeed,
    onClick: (ArticleWithFeed) -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    val feed = articleWithFeed.feed
    val article = articleWithFeed.article

    ArticleItem(
        modifier = modifier,
        feedName = feed.name,
        feedIconUrl = feed.icon,
        title = article.title,
        shortDescription = article.shortDescription,
        dateString = article.dateString,
        imgData = article.img,
        isStarred = article.isStarred,
        isUnread = article.isUnread,
        onClick = { onClick(articleWithFeed) },
        onLongClick = onLongClick
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleItem(
    modifier: Modifier = Modifier,
    feedName: String = "",
    feedIconUrl: String? = null,
    title: String = "",
    shortDescription: String = "",
    dateString: String? = null,
    imgData: Any? = null,
    isStarred: Boolean = false,
    isUnread: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListFeedName = LocalFlowArticleListFeedName.current
    val articleListImage = LocalFlowArticleListImage.current
    val articleListDesc = LocalFlowArticleListDesc.current
    val articleListDate = LocalFlowArticleListTime.current
    val articleListReadIndicator = LocalFlowArticleListReadIndicator.current

    Column(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .clip(Shape20)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .alpha(
                when (articleListReadIndicator) {
                    FlowArticleReadIndicatorPreference.AllRead -> {
                        if (isUnread) 1f else 0.5f
                    }

                    FlowArticleReadIndicatorPreference.ExcludingStarred -> {
                        if (isUnread || isStarred) 1f else 0.5f
                    }
                }
            ),
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
                        .padding(
                            start = if (articleListFeedIcon.value) 30.dp else 0.dp,
                            end = 10.dp,
                        ),
                    text = feedName,
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
                    if (isStarred) {
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
                        text = dateString ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                FeedIcon(feedName = feedName, iconUrl = feedIconUrl)
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Article
            Column(
                modifier = Modifier.weight(1f),
            ) {

                // Title
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.applyTextDirection(title.requiresBidi()),
                    maxLines = if (articleListDesc.value) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                )

                // Description
                if (articleListDesc.value && shortDescription.isNotBlank()) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = shortDescription,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.applyTextDirection(
                            shortDescription.requiresBidi()
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Image
            if (imgData != null && articleListImage.value) {
                RYAsyncImage(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(80.dp)
                        .clip(Shape20),
                    data = imgData,
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
private const val SwipeActionDelay = 300L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SwipeableArticleItem(
    articleWithFeed: ArticleWithFeed,
    isFilterUnread: Boolean = false,
    articleListTonalElevation: Int = 0,
    onClick: (ArticleWithFeed) -> Unit = {},
    isSwipeEnabled: () -> Boolean = { false },
    isMenuEnabled: Boolean = true,
    onToggleStarred: (ArticleWithFeed, Long) -> Unit = { _, _ -> },
    onToggleRead: (ArticleWithFeed, Long) -> Unit = { _, _ -> },
    onMarkAboveAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onShare: ((ArticleWithFeed) -> Unit)? = null,
) {
    val swipeToStartAction = LocalArticleListSwipeStartAction.current
    val swipeToEndAction = LocalArticleListSwipeEndAction.current

    val onSwipeEndToStart = when (swipeToStartAction) {
        SwipeStartActionPreference.None -> null
        SwipeStartActionPreference.ToggleRead -> onToggleRead
        SwipeStartActionPreference.ToggleStarred -> onToggleStarred
    }

    val onSwipeStartToEnd = when (swipeToEndAction) {
        SwipeEndActionPreference.None -> null
        SwipeEndActionPreference.ToggleRead -> onToggleRead
        SwipeEndActionPreference.ToggleStarred -> onToggleStarred
    }
    val density = LocalDensity.current
    val confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = {
        when (it) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onSwipeStartToEnd?.invoke(articleWithFeed, SwipeActionDelay)
                swipeToEndAction == SwipeEndActionPreference.ToggleRead && isFilterUnread
            }

            SwipeToDismissBoxValue.EndToStart -> {
                onSwipeEndToStart?.invoke(articleWithFeed, SwipeActionDelay)
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
    var isThresholdPassed by remember(articleWithFeed) { mutableStateOf(false) }

    LaunchedEffect(swipeState.progress > PositionalThresholdFraction) {
        if (swipeState.progress > PositionalThresholdFraction && swipeState.targetValue != SwipeToDismissBoxValue.Settled) {
            isThresholdPassed = true
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } else {
            isThresholdPassed = false
        }
    }

    var expanded by remember { mutableStateOf(false) }


    val onLongClick = if (isMenuEnabled) {
        {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            expanded = true
        }
    } else {
        null
    }
    var menuOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    SwipeToDismissBox(
        state = swipeState,
        enabled = !isSwipeEnabled(),
        /***  create dismiss alert background box */
        backgroundContent = {
            SwipeToDismissBoxBackgroundContent(
                direction = swipeState.dismissDirection,
                isActive = isThresholdPassed,
                isStarred = articleWithFeed.article.isStarred,
                isRead = !articleWithFeed.article.isUnread
            )
        },
        /**** Dismiss Content */
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(expanded) {
                        awaitEachGesture {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false).let {
                                    menuOffset = it.position
                                }
                            }
                        }
                    }
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            articleListTonalElevation.dp
                        ) onDark MaterialTheme.colorScheme.surface
                    )
                    .wrapContentSize()
            ) {
                ArticleItem(
                    articleWithFeed = articleWithFeed,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                with(articleWithFeed.article) {
                    if (isMenuEnabled) {
                        AnimatedDropdownMenu(
                            modifier = Modifier.padding(12.dp),
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            offset = density.run {
                                DpOffset(menuOffset.x.toDp(), 0.dp)
                            },
                        ) {
                            ArticleItemMenuContent(
                                articleWithFeed = articleWithFeed,
                                isStarred = isStarred,
                                isRead = !isUnread,
                                onToggleStarred = onToggleStarred,
                                onToggleRead = onToggleRead,
                                onMarkAboveAsRead = onMarkAboveAsRead,
                                onMarkBelowAsRead = onMarkBelowAsRead,
                                onShare = onShare
                            ) { expanded = false }
                        }
                    }
                }
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
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp)
                )
            }
        }
    }
}


@Composable
fun ArticleItemMenuContent(
    articleWithFeed: ArticleWithFeed,
    iconSize: DpSize = DpSize(width = 20.dp, height = 20.dp),
    isStarred: Boolean = false,
    isRead: Boolean = false,
    onToggleStarred: (ArticleWithFeed, Long) -> Unit = { _, _ -> },
    onToggleRead: (ArticleWithFeed, Long) -> Unit = { _, _ -> },
    onMarkAboveAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onShare: ((ArticleWithFeed) -> Unit)? = null,
    onItemClick: (() -> Unit)? = null,
) {
    val starImageVector =
        remember(isStarred) { if (isStarred) Icons.Outlined.StarOutline else Icons.Rounded.Star }

    val readImageVector =
        remember(isRead) { if (isRead) Icons.Outlined.FiberManualRecord else Icons.Rounded.FiberManualRecord }

    val starText =
        stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred)

    val readText =
        stringResource(if (isRead) R.string.mark_as_unread else R.string.mark_as_read)

    DropdownMenuItem(text = { Text(text = readText) }, onClick = {
        onToggleRead(articleWithFeed, 0)
        onItemClick?.invoke()
    }, leadingIcon = {
        Icon(
            imageVector = readImageVector,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    })
    DropdownMenuItem(
        text = { Text(text = starText) },
        onClick = {
            onToggleStarred(articleWithFeed, 0)
            onItemClick?.invoke()
        },
        leadingIcon = {
            Icon(
                imageVector = starImageVector,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        })

    if (onMarkAboveAsRead != null || onMarkBelowAsRead != null) {
        HorizontalDivider()
    }
    onMarkAboveAsRead?.let {
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.mark_above_as_read)) },
            onClick = {
                onMarkAboveAsRead(articleWithFeed)
                onItemClick?.invoke()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            })
    }
    onMarkBelowAsRead?.let {
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.mark_below_as_read)) },
            onClick = {
                onMarkBelowAsRead(articleWithFeed)
                onItemClick?.invoke()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            })
    }
    onShare?.let {
        HorizontalDivider()
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.share)) }, onClick = {
            onShare(articleWithFeed)
            onItemClick?.invoke()
        }, leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Share, contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        })
    }
}

@Preview
@Composable
fun MenuContentPreview() {
    MaterialTheme {
        Surface() {
            Column(modifier = Modifier.padding()) {
                ArticleItemMenuContent(
                    articleWithFeed = generateArticleWithFeedPreview(),
                    onMarkBelowAsRead = {},
                    onMarkAboveAsRead = {},
                    onShare = {})
            }
        }
    }
}
