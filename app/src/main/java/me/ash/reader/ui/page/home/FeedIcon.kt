package me.ash.reader.ui.page.home

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.component.AsyncImage
import java.net.URLEncoder

@Composable
fun FeedIcon(
    feedName: String = "",
    size: Dp = 20.dp
) {
    val map = remember { mutableStateMapOf<String, String>() }

    AsyncImage(
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        contentDescription = feedName,
        data = if (map.containsKey(feedName)) {
            getURL(map[feedName] ?: "")
        } else {
            getURL(URLEncoder.encode(
                feedName,
                Charsets.UTF_8.toString()
            ).also {
                map[feedName] = it
            })
        },
        placeholder = null,
    )
}

fun getURL(encodedFeedName: String): String =
    "https://ui-avatars.com/api/?length=1&background=random&name=$encodedFeedName"