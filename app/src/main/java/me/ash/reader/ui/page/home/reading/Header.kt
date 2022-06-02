package me.ash.reader.ui.page.home.reading

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.ext.formatAsString
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.ext.roundClick
import java.util.*

@Composable
fun Header(
    feedName: String,
    title: String,
    author: String? = null,
    link: String? = null,
    publishedDate: Date,
) {
    val context = LocalContext.current
    val dateString = remember(publishedDate) {
        publishedDate.formatAsString(context, atHourMinute = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .roundClick {
                context.openURL(link)
            }
            .padding(12.dp)
    ) {
        Text(
            modifier = Modifier.alpha(0.7f),
            text = dateString,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(4.dp))
        author?.let {
            if (it.isNotEmpty()) {
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = it,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Start,
                )
            }
        }
        Text(
            modifier = Modifier.alpha(0.7f),
            text = feedName,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Start,
        )
    }
}