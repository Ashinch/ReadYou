package me.ash.reader.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import me.ash.reader.ui.data.Filter
import me.ash.reader.ui.data.NavigationBarItem
import kotlin.math.absoluteValue

@ExperimentalPagerApi
@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    filter: Filter,
    filterOnClick: (Filter) -> Unit = {},
    disabled: Boolean,
    isUnread: Boolean,
    isStarred: Boolean,
    isFullContent: Boolean,
    unreadOnClick: (afterIsUnread: Boolean) -> Unit = {},
    starredOnClick: (afterIsStarred: Boolean) -> Unit = {},
    fullContentOnClick: (afterIsFullContent: Boolean) -> Unit = {},
) {
    val transition = updateTransition(targetState = pagerState, label = "")
    val readerBarAlpha by transition.animateFloat(
        label = "",
        transitionSpec = {
            tween(
                easing = FastOutLinearInEasing,
            )
        }
    ) {
        if (it.currentPage < 2) {
            if (it.currentPage == it.targetPage) {
                0f
            } else {
                if (it.targetPage == 2) {
                    it.currentPageOffset.absoluteValue
                } else {
                    0f
                }
            }
        } else {
            if (it.currentPage == it.targetPage) {
                1f
            } else {
                if (it.targetPage == 1) {
                    1f - it.currentPageOffset.absoluteValue
                } else {
                    0f
                }
            }
        }
    }

    Divider(modifier = Modifier.alpha(0.3f))
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AnimatedVisibility(
            visible = readerBarAlpha < 1f,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .animateContentSize()
                    .alpha(1 - readerBarAlpha),
            ) {
//            Log.i("RLog", "AppNavigationBar: ${readerBarAlpha}, ${1f - readerBarAlpha}")
                FilterBar(
                    modifier = modifier,
                    filter = filter,
                    onSelected = filterOnClick,
                )
            }
        }
        AnimatedVisibility(
            visible = readerBarAlpha > 0f,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .animateContentSize()
                    .alpha(readerBarAlpha),
            ) {
                ReaderBar(
                    modifier = modifier,
                    disabled = disabled,
                    isUnread = isUnread,
                    isStarred = isStarred,
                    isFullContent = isFullContent,
                    unreadOnClick = unreadOnClick,
                    starredOnClick = starredOnClick,
                    fullContentOnClick = fullContentOnClick,
                )
            }
        }
    }
}

@Composable
private fun FilterBar(
    modifier: Modifier = Modifier,
    filter: Filter,
    onSelected: (Filter) -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            NavigationBarItem.Starred,
            NavigationBarItem.Unread,
            NavigationBarItem.All
        ).forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .animateContentSize(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(30.dp)
                        .defaultMinSize(
                            minWidth = 82.dp
                        )
                        .clip(CircleShape)
                        .clickable(onClick = {
                            onSelected(
                                when (index) {
                                    0 -> Filter.Starred
                                    1 -> Filter.Unread
                                    else -> Filter.All
                                }
                            )
                        })
                        .background(
                            if (filter.index == index) {
                                MaterialTheme.colorScheme.inverseOnSurface
                            } else {
                                Color.Unspecified
                            }
                        )
                ) {
                    if (filter.index == index) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            modifier = Modifier.size(
                                if (Filter.Unread.index == index) {
                                    15
                                } else {
                                    19
                                }.dp
                            ),
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Icon(
                            modifier = Modifier.size(
                                if (Filter.Unread.index == index) {
                                    15
                                } else {
                                    19
                                }.dp
                            ),
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderBar(
    modifier: Modifier = Modifier,
    disabled: Boolean,
    isUnread: Boolean,
    isStarred: Boolean,
    isFullContent: Boolean,
    unreadOnClick: (afterIsUnread: Boolean) -> Unit = {},
    starredOnClick: (afterIsStarred: Boolean) -> Unit = {},
    fullContentOnClick: (afterIsFullContent: Boolean) -> Unit = {},
) {
    var fullContent by remember { mutableStateOf(isFullContent) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        CanBeDisabledIconButton(
            disabled = disabled,
            imageVector = if (isUnread) {
                Icons.Rounded.Circle
            } else {
                Icons.Outlined.Circle
            },
            contentDescription = "Mark Unread",
            tint = MaterialTheme.colorScheme.primary,
        ) {
            unreadOnClick(!isUnread)
        }
        CanBeDisabledIconButton(
            disabled = disabled,
            modifier = Modifier.size(28.dp),
            imageVector = if (isStarred) {
                Icons.Rounded.Star
            } else {
                Icons.Rounded.StarBorder
            },
            contentDescription = "Starred",
            tint = MaterialTheme.colorScheme.primary,
        ) {
            starredOnClick(!isStarred)
        }
        CanBeDisabledIconButton(
            disabled = disabled,
            modifier = Modifier.size(30.dp),
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = "Next Article",
            tint = MaterialTheme.colorScheme.primary,
        ) {

        }
        CanBeDisabledIconButton(
            disabled = disabled,
            imageVector = Icons.Outlined.Sell,
            contentDescription = "Add Tag",
            tint = MaterialTheme.colorScheme.primary,
        ) {

        }
        CanBeDisabledIconButton(
            disabled = disabled,
            modifier = Modifier.size(26.dp),
            imageVector = if (fullContent) {
                Icons.Rounded.Article
            } else {
                Icons.Outlined.Article
            },
            contentDescription = "Full Content Parsing",
            tint = MaterialTheme.colorScheme.primary,
        ) {
            val afterIsFullContent = !fullContent
            fullContent = afterIsFullContent
            fullContentOnClick(afterIsFullContent)
        }
    }
}