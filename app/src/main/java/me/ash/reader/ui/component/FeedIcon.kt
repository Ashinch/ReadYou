package me.ash.reader.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.ui.component.base.Base64Image
import me.ash.reader.ui.component.base.RYAsyncImage

@Composable
fun FeedIcon(
    modifier: Modifier = Modifier,
    feedName: String? = "",
    iconUrl: String?,
    size: Dp = 20.dp,
    placeholderIcon: ImageVector? = null,
) {
    if (iconUrl.isNullOrEmpty()) {
        if (placeholderIcon == null) {
            FontIcon(modifier, size, feedName ?: "")
        } else {
            ImageIcon(modifier, placeholderIcon, feedName ?: "")
        }
    }
    // e.g. image/gif;base64,R0lGODlh...
    else if ("^image/.*;base64,.*".toRegex().matches(iconUrl)) {
        Base64Image(
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            base64Uri = iconUrl,
            onEmpty = { FontIcon(modifier, size, feedName ?: "") },
        )
    } else {
        RYAsyncImage(
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentDescription = feedName ?: "",
            data = iconUrl,
            placeholder = null,
        )
    }
}

@Composable
private fun ImageIcon(modifier: Modifier, placeholderIcon: ImageVector, feedName: String) {
    Icon(
        modifier = modifier,
        imageVector = placeholderIcon,
        contentDescription = feedName,
    )
}

@Composable
private fun FontIcon(modifier: Modifier, size: Dp, feedName: String) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = feedName.ifEmpty { " " }.first().toString(),
            style = MaterialTheme.typography.bodyMedium.merge(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        )
    }
}

@Preview
@Composable
fun FeedIconPrev() {
    FeedIcon(feedName = stringResource(R.string.preview_feed_name), iconUrl = null)
}
