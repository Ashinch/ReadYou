package me.ash.reader.ui.page.home.feed

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupWithFeed

@Composable
fun ColumnScope.GroupList(
    modifier: Modifier = Modifier,
    groupVisible: Boolean,
    feedVisible: Boolean,
    groupWithFeed: GroupWithFeed,
    groupAndFeedOnClick: (currentGroup: Group?, currentFeed: Feed?) -> Unit = { _, _ -> },
    expandOnClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = groupVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = modifier) {
            ButtonBar(
                buttonBarType = ButtonBarType.GroupBar(
                    title = groupWithFeed.group.name,
                    icon = Icons.Rounded.ExpandMore,
                    important = groupWithFeed.group.important ?: 0,
                    iconOnClick = expandOnClick,
                ),
                onClick = {
                    groupAndFeedOnClick(groupWithFeed.group, null)
                }
            )
            FeedList(
                visible = feedVisible,
                feeds = groupWithFeed.feeds,
                onClick = { currentFeed ->
                    groupAndFeedOnClick(null, currentFeed)
                }
            )
        }
    }
}