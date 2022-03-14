package me.ash.reader.ui.page.home.feed

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import me.ash.reader.data.feed.Feed

@Composable
fun ColumnScope.FeedList(
    visible: Boolean,
    feeds: List<Feed>,
    onClick: (currentFeed: Feed?) -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            feeds.forEach { feed ->
                ButtonBar(
                    buttonBarType = ButtonBarType.FeedBar(
                        title = feed.name,
                        important = feed.important ?: 0,
                        icon = if (feed.icon == null) {
                            null
                        } else {
                            BitmapPainter(
                                BitmapFactory.decodeByteArray(
                                    feed.icon,
                                    0,
                                    feed.icon!!.size
                                ).asImageBitmap()
                            )
                        },
                    ),
                    onClick = {
                        onClick(feed)
                    },
                )
            }
        }
    }
}
