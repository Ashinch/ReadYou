package me.ash.reader.ui.page.home.feeds

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.RYExtensibleVisibility
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionViewModel
import me.ash.reader.ui.theme.ShapeBottom32

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
)
@Composable
fun FeedItem(
    feed: Feed,
    alpha: Float = 1f,
    badgeAlpha: Float = 1f,
    isEnded: () -> Boolean,
    isExpanded: () -> Boolean,
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    onClick: () -> Unit = {},
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    RYExtensibleVisibility(visible = isExpanded()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(if (isEnded()) ShapeBottom32 else RectangleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = alpha))
                .combinedClickable(
                    onClick = {
                        onClick()
                    },
                    onLongClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        feedOptionViewModel.showDrawer(scope, feed.id)
                    }
                )
                .padding(horizontal = 14.dp)
                .padding(top = 14.dp, bottom = if (isEnded()) 22.dp else 14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    FeedIcon(feedName = feed.name, iconUrl = feed.icon)
                    Text(
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                        text = feed.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if ((feed.important ?: 0) != 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surfaceTint.copy(
                            alpha = badgeAlpha
                        ),
                        contentColor = MaterialTheme.colorScheme.outline,
                        content = {
                            Text(
                                text = feed.important.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                    )
                }
            }
        }
    }
}
