package me.ash.reader.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FeedIcon(
    feedName: String,
    size: Dp = 20.dp
) {
    Row(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {}

//    val url by remember {
//        mutableStateOf(
//            "https://ui-avatars.com/api/?length=1&background=random&name=${
//                URLEncoder.encode(
//                    feedName,
//                    Charsets.UTF_8.toString()
//                )
//            }"
//        )
//    }
//
//    AsyncImage(
//        modifier = Modifier
//            .size(size)
//            .clip(CircleShape),
//        contentDescription = feedName,
//        data = url,
//        placeholder = null,
//    )
}
