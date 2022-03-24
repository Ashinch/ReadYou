package me.ash.reader.ui.page.home

import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import me.ash.reader.R
import me.ash.reader.data.constant.Filter
import me.ash.reader.ui.extension.getName
import me.ash.reader.ui.theme.LocalLightThemeColors
import me.ash.reader.ui.widget.CanBeDisabledIconButton
import kotlin.math.absoluteValue

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeBottomNavBar(
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

    Divider(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
    )
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
                Log.i("RLog", "AppNavigationBar: ${readerBarAlpha}, ${1f - readerBarAlpha}")
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
        FlowRow(
            mainAxisSize = SizeMode.Expand,
            mainAxisAlignment = MainAxisAlignment.Center,
            crossAxisAlignment = FlowCrossAxisAlignment.Center,
            crossAxisSpacing = 0.dp,
            mainAxisSpacing = 20.dp,
        ) {
            listOf(
                Filter.Starred,
                Filter.Unread,
                Filter.All
            ).forEach { item ->
                Item(
                    icon = if (filter == item) item.filledIcon else item.icon,
                    name = item.getName(),
                    selected = filter == item,
                ) {
                    onSelected(item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Item(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val view = LocalView.current
    val lightThemeColors = LocalLightThemeColors.current
    val lightPrimaryContainer = lightThemeColors.primaryContainer
    val lightOnSurface = lightThemeColors.onSurface

    FilterChip(
        modifier = Modifier
            .height(36.dp)
            .animateContentSize(),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.outline,
            leadingIconColor = MaterialTheme.colorScheme.outline,
            disabledBackgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            selectedBackgroundColor = lightPrimaryContainer,
            selectedContentColor = lightOnSurface,
            selectedLeadingIconColor = lightOnSurface
        ),
        selected = selected,
        selectedIcon = {
            Icon(
                imageVector = icon,
                contentDescription = name,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp),
                tint = lightOnSurface,
            )
        },
        onClick = {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            onClick()
        },
        content = {
            if (selected) {
                Text(
                    modifier = modifier.padding(
                        start = 0.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    ),
                    text = if (selected) name.uppercase() else "",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) {
                        lightOnSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        },
    )

//    Row(
//        modifier = Modifier
//            .animateContentSize()
//            .height(40.dp)
//            .width(if (selected) Dp.Unspecified else 40.dp)
//            .padding(vertical = if (selected) 2.dp else 0.dp)
//            .clip(CircleShape)
//            .pointerInput(Unit) {
//                detectTapGestures(
//                    onTap = {
//                        view.playSoundEffect(SoundEffectConstants.CLICK)
//                        onClick()
//                    }
//                )
//            }
//            .background(
//                if (selected) {
//                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f)
//                } else {
//                    Color.Transparent
//                }
//            ),
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Spacer(modifier = Modifier.width(8.dp))
//        Icon(
//            modifier = Modifier.size(20.dp),
//            imageVector = icon,
//            contentDescription = name,
//            tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
//        )
//        if (selected) {
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                modifier = Modifier.padding(horizontal = 8.dp),
//                text = name.uppercase(),
//                style = MaterialTheme.typography.titleSmall,
//                color = if (selected) {
//                    MaterialTheme.colorScheme.onSurface
//                } else {
//                    MaterialTheme.colorScheme.outline
//                },
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//        }
//    }
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
    val view = LocalView.current
    var fullContent by remember { mutableStateOf(isFullContent) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        CanBeDisabledIconButton(
            modifier = Modifier.size(18.dp),
            disabled = disabled,
            imageVector = if (isUnread) {
                Icons.Rounded.Circle
            } else {
                Icons.Outlined.Circle
            },
            contentDescription = stringResource(if (isUnread) R.string.mark_as_read else R.string.mark_as_unread),
            tint = MaterialTheme.colorScheme.primary,
        ) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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
            contentDescription = stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred),
            tint = MaterialTheme.colorScheme.primary,
        ) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            starredOnClick(!isStarred)
        }
        CanBeDisabledIconButton(
            disabled = disabled,
            modifier = Modifier.size(30.dp),
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = "Next Article",
            tint = MaterialTheme.colorScheme.primary,
        ) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        CanBeDisabledIconButton(
            disabled = disabled,
            imageVector = Icons.Outlined.Sell,
            contentDescription = "Add Tag",
            tint = MaterialTheme.colorScheme.primary,
        ) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        CanBeDisabledIconButton(
            disabled = disabled,
            modifier = Modifier.size(26.dp),
            imageVector = if (fullContent) {
                Icons.Rounded.Article
            } else {
                Icons.Outlined.Article
            },
            contentDescription = stringResource(R.string.parse_full_content),
            tint = MaterialTheme.colorScheme.primary,
        ) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val afterIsFullContent = !fullContent
            fullContent = afterIsFullContent
            fullContentOnClick(afterIsFullContent)
        }
    }
}