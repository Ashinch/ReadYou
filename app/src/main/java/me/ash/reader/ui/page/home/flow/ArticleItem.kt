package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.preference.*
import me.ash.reader.data.preference.ArticleListDatePreference.Companion.articleListDate
import me.ash.reader.data.preference.ArticleListDescPreference.Companion.articleListDesc
import me.ash.reader.data.preference.ArticleListFeedIconPreference.Companion.articleListFeedIcon
import me.ash.reader.data.preference.ArticleListFeedNamePreference.Companion.articleListFeedName
import me.ash.reader.data.preference.ArticleListImagePreference.Companion.articleListImage
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.formatAsString

@Composable
fun ArticleItem(
    modifier: Modifier = Modifier,
    articleWithFeed: ArticleWithFeed,
    onClick: (ArticleWithFeed) -> Unit = {},
) {
    val context = LocalContext.current
    val articleListFeedIcon =
        context.articleListFeedIcon.collectAsStateValue(initial = ArticleListFeedIconPreference.default)
    val articleListFeedName =
        context.articleListFeedName.collectAsStateValue(initial = ArticleListFeedNamePreference.default)
    val articleListImage =
        context.articleListImage.collectAsStateValue(initial = ArticleListImagePreference.default)
    val articleListDesc =
        context.articleListDesc.collectAsStateValue(initial = ArticleListDescPreference.default)
    val articleListDate =
        context.articleListDate.collectAsStateValue(initial = ArticleListDatePreference.default)

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(articleWithFeed) }
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .alpha(if (articleWithFeed.article.isStarred || articleWithFeed.article.isUnread) 1f else 0.5f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Feed name
            if (articleListFeedName.value) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (articleListFeedIcon.value) 30.dp else 0.dp),
                    text = articleWithFeed.feed.name,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (articleListDate.value) {
                Row(
                    modifier = Modifier.padding(start = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Starred
                    if (articleWithFeed.article.isStarred) {
                        Icon(
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 2.dp),
                            imageVector = Icons.Rounded.Star,
                            contentDescription = stringResource(R.string.starred),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        )
                    }
                    
                    // Date
                    Text(
                        text = articleWithFeed.article.date.formatAsString(
                            context,
                            onlyHourMinute = true
                        ),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Feed icon
            if (articleListFeedIcon.value) {
                Row(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {}
                Spacer(modifier = Modifier.width(10.dp))
            }
            // Article
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Title
                Text(
                    text = articleWithFeed.article.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = if (articleListDesc.value) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                )
                // Description
                if (articleListDesc.value) {
                    Text(
                        text = articleWithFeed.article.shortDescription,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}