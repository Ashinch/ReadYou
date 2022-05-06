package me.ash.reader.ui.page.home.read

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.ui.ext.formatAsString
import me.ash.reader.ui.ext.roundClick

@Composable
fun Header(
    articleWithFeed: ArticleWithFeed,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .roundClick {
                articleWithFeed.article.link.let {
                    if (it.isNotEmpty()) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(articleWithFeed.article.link))
                        )
                    }
                }
            }
            .padding(12.dp)
    ) {
        Text(
            text = articleWithFeed.article.date.formatAsString(context, atHourMinute = true),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = articleWithFeed.article.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        articleWithFeed.article.author?.let {
            if (it.isNotEmpty()) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Text(
            text = articleWithFeed.feed.name,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}