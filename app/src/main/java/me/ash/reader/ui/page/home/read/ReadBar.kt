package me.ash.reader.ui.page.home.read

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.ash.reader.R
import me.ash.reader.ui.component.CanBeDisabledIconButton

@Composable
fun ReadBar(
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

    Surface(
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.height(60.dp)
        ) {
            Box {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .zIndex(1f),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CanBeDisabledIconButton(
                    modifier = Modifier.size(40.dp),
                    disabled = disabled,
                    imageVector = if (isUnread) {
                        Icons.Filled.FiberManualRecord
                    } else {
                        Icons.Outlined.FiberManualRecord
                    },
                    contentDescription = stringResource(if (isUnread) R.string.mark_as_read else R.string.mark_as_unread),
                    tint = if (isUnread) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    unreadOnClick(!isUnread)
                }
                CanBeDisabledIconButton(
                    modifier = Modifier.size(40.dp),
                    disabled = disabled,
                    imageVector = if (isStarred) {
                        Icons.Rounded.Star
                    } else {
                        Icons.Rounded.StarOutline
                    },
                    contentDescription = stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred),
                    tint = if (isStarred) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    starredOnClick(!isStarred)
                }
                CanBeDisabledIconButton(
                    disabled = true,
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Next Article",
                    tint = MaterialTheme.colorScheme.outline,
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                CanBeDisabledIconButton(
                    modifier = Modifier.size(40.dp),
                    disabled = true,
                    imageVector = Icons.Outlined.TextFormat,
                    contentDescription = "Add Tag",
                    tint = MaterialTheme.colorScheme.outline,
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                CanBeDisabledIconButton(
                    disabled = disabled,
                    modifier = Modifier.size(40.dp),
                    imageVector = if (fullContent) {
                        Icons.Rounded.Article
                    } else {
                        Icons.Outlined.Article
                    },
                    contentDescription = stringResource(R.string.parse_full_content),
                    tint = if (fullContent) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    val afterIsFullContent = !fullContent
                    fullContent = afterIsFullContent
                    fullContentOnClick(afterIsFullContent)
                }
            }
        }
    }
}