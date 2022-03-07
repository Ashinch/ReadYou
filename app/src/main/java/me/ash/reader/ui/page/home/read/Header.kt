package me.ash.reader.ui.page.home.read

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.data.article.Article
import me.ash.reader.data.feed.Feed
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
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = article.date.toString(DateTimeExt.YYYY_MM_DD_HH_MM, true),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.title,
                fontSize = 27.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            article.author?.let {
                Text(
                    text = article.author,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = feed.name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}