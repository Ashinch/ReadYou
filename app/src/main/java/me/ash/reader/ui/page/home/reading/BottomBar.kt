package me.ash.reader.ui.page.home.reading

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.ui.component.base.CanBeDisabledIconButton
import me.ash.reader.ui.component.base.RYExtensibleVisibility

@Composable
fun BottomBar(
    isShow: Boolean,
    isUnread: Boolean,
    isStarred: Boolean,
    isNextArticleAvailable: Boolean,
    isFullContent: Boolean,
    onUnread: (isUnread: Boolean) -> Unit = {},
    onStarred: (isStarred: Boolean) -> Unit = {},
    onNextArticle: () -> Unit = {},
    onFullContent: (isFullContent: Boolean) -> Unit = {},
) {
    val tonalElevation = LocalReadingPageTonalElevation.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.BottomCenter
    ) {
        RYExtensibleVisibility(visible = isShow) {
            val view = LocalView.current

            Surface(
                tonalElevation = tonalElevation.value.dp,
            ) {
                // TODO: Component styles await refactoring
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CanBeDisabledIconButton(
                        modifier = Modifier.size(40.dp),
                        disabled = false,
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
                        onUnread(!isUnread)
                    }
                    CanBeDisabledIconButton(
                        modifier = Modifier.size(40.dp),
                        disabled = false,
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
                        onStarred(!isStarred)
                    }
                    CanBeDisabledIconButton(
                        disabled = !isNextArticleAvailable,
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "Next Article",
                        tint = MaterialTheme.colorScheme.outline,
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onNextArticle()
                    }
                    CanBeDisabledIconButton(
                        modifier = Modifier.size(36.dp),
                        disabled = true,
                        imageVector = Icons.Outlined.Headphones,
                        contentDescription = "Add Tag",
                        tint = MaterialTheme.colorScheme.outline,
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    CanBeDisabledIconButton(
                        disabled = false,
                        modifier = Modifier.size(40.dp),
                        imageVector = if (isFullContent) {
                            Icons.AutoMirrored.Rounded.Article
                        } else {
                            Icons.AutoMirrored.Outlined.Article
                        },
                        contentDescription = stringResource(R.string.parse_full_content),
                        tint = if (isFullContent) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onFullContent(!isFullContent)
                    }
                }
            }
        }
    }
}
