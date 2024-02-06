package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.size.Precision
import coil.size.Scale
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.preference.*
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
            .alpha(
                articleWithFeed.article.run {
                    when (articleListReadIndicator) {
                        FlowArticleReadIndicatorPreference.AllRead -> {
                            if (isUnread) 1f else 0.5f
                        }

                        FlowArticleReadIndicatorPreference.ExcludingStarred -> {
                            if (isUnread || isStarred) 1f else 0.5f
                        }
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
                                .alpha(0.7f)
                                .size(14.dp)
                                .padding(end = 2.dp),
                            imageVector = Icons.Rounded.Star,
                            contentDescription = stringResource(R.string.starred),
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }

                    // Date
                    Text(
                        modifier = Modifier.alpha(0.7f),
                        text = articleWithFeed.article.dateString ?: "",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        // Bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.alpha(0.7f),
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

private const val PositionalThresholdFraction = 0.2f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableArticleItem(
    articleWithFeed: ArticleWithFeed,
    isFilterUnread: Boolean,
    articleListTonalElevation: Int,
    onClick: (ArticleWithFeed) -> Unit = {},
    onSwipeStartToEnd: ((ArticleWithFeed) -> Unit)? = null,
    onSwipeEndToStart: ((ArticleWithFeed) -> Unit)? = null,
) {
    var isArticleVisible by remember { mutableStateOf(true) }

    val density = LocalDensity.current
    val confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = {
        when (it) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onSwipeStartToEnd?.invoke(articleWithFeed)
                isFilterUnread
            }

            SwipeToDismissBoxValue.EndToStart -> {
                onSwipeEndToStart?.invoke(articleWithFeed)
                false
            }

            SwipeToDismissBoxValue.Settled -> {
                true
            }
        }
    }
    val positionalThreshold: (totalDistance: Float) -> Float = {
        it * PositionalThresholdFraction
    }
    val swipeState = rememberSaveable(
        articleWithFeed,
        saver = SwipeToDismissBoxState.Saver(
            confirmValueChange = confirmValueChange,
            density = density,
            positionalThreshold = positionalThreshold
        )
    ) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            confirmValueChange = confirmValueChange,
            positionalThreshold = positionalThreshold
        )
    }
//    val swipeState = rememberSwipeToDismissBoxState(positionalThreshold =, confirmValueChange =)
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(swipeState.progress > 0.15f) {
        if (swipeState.progress > 0.15f && swipeState.targetValue != SwipeToDismissBoxValue.Settled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

//    val dismissState =
//        rememberDismissState(initialValue = DismissValue.Default, confirmStateChange = {
//            if (it == DismissValue.DismissedToEnd) {
//                isArticleVisible = !isFilterUnread
//                onSwipeOut(articleWithFeed)
//            }
//            isFilterUnread
//        })
    if (isArticleVisible) {
        SwipeToDismissBox(
            state = swipeState,
            /***  create dismiss alert background box */
            backgroundContent = {
                if (swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Column(modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(
                                imageVector = if (articleWithFeed.article.isUnread) Icons.Rounded.CheckCircleOutline else Icons.Outlined.Circle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = stringResource(if (articleWithFeed.article.isUnread) R.string.mark_as_read else R.string.mark_as_unread),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                } else if (swipeState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                            Icon(
                                imageVector = if (articleWithFeed.article.isStarred) Icons.Rounded.Star else Icons.Rounded.Circle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = stringResource(if (articleWithFeed.article.isStarred) R.string.mark_as_unstar else R.string.mark_as_starred),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
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
}
