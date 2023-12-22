package me.ash.reader.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.ui.component.base.Base64Image
import me.ash.reader.ui.component.base.RYAsyncImage

@Composable
fun FeedIcon(
    feedName: String,
    iconUrl: String?,
    size: Dp = 20.dp,
) {
    if (iconUrl == null) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = feedName.ifEmpty { " " }.first().toString(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp,
            )
        }
    }
    // e.g. image/gif;base64,R0lGODlh...
    else if ("^image/.*;base64,.*".toRegex().matches(iconUrl)) {
        Base64Image(
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            base64Uri = iconUrl)
    } else {
        RYAsyncImage(
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentDescription = feedName,
            data = iconUrl,
            placeholder = null,
        )
    }
}

@Preview
@Composable
fun FeedIconPrev(){
    FeedIcon("AFF", null)
}
