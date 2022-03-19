package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun FeedItem(
    modifier: Modifier = Modifier,
    name: String,
    important: Int,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.padding(start = 14.dp)) {
                Row(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline),
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (important != 0) {
                Badge(
                    modifier = Modifier.padding(end = 6.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f),
                    contentColor = MaterialTheme.colorScheme.outline,
                    content = {
                        Text(
                            text = important.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                )
            }
        }
    }
}