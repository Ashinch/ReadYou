package me.ash.reader.ui.page.home.read

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.ash.reader.data.article.Article
import me.ash.reader.data.feed.Feed
import me.ash.reader.formatToString
import me.ash.reader.ui.extension.roundClick

@Composable
fun Header(
    context: Context,
    article: Article,
    feed: Feed
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .roundClick {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                )
            }
            .padding(12.dp)
    ) {
        Text(
            text = article.date.formatToString(context, atHourMinute = true),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = article.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        article.author?.let {
            Text(
                text = article.author,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Text(
            text = feed.name,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}