package me.ash.reader.ui.page.home.feeds

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.data.entity.Feed
import me.ash.reader.ui.page.home.FeedIcon
import me.ash.reader.ui.page.home.feeds.option.feed.FeedOptionViewAction
import me.ash.reader.ui.page.home.feeds.option.feed.FeedOptionViewModel
import kotlin.math.ln

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
)
@Composable
fun FeedItem(
    modifier: Modifier = Modifier,
    feed: Feed,
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    tonalElevation: Dp,
    onClick: () -> Unit = {},
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(32.dp))
            .combinedClickable(
                onClick = {
                    onClick()
                },
                onLongClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    feedOptionViewModel.dispatch(FeedOptionViewAction.Show(scope, feed.id))
                }
            )
            .padding(vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f)) {
                FeedIcon(feed.name)
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
                        alpha = (ln(tonalElevation.value + 1.4f) + 2f) / 100f
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