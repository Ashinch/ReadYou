package me.ash.reader.ui.page.home.article

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.R
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.ui.extension.paddingFixedHorizontal
import me.ash.reader.ui.extension.roundClick

@Composable
fun ArticleItem(
    modifier: Modifier = Modifier,
    articleWithFeed: ArticleWithFeed? = null,
    isStarredFilter: Boolean,
    index: Int,
    articleOnClick: (ArticleWithFeed) -> Unit,
) {
    if (articleWithFeed == null) return
    Column(
        modifier = modifier
            .paddingFixedHorizontal(
                top = if (index == 0) 8.dp else 0.dp,
                bottom = 8.dp
            )
            .roundClick {
                articleOnClick(articleWithFeed)
            }
            .alpha(
                if (isStarredFilter || articleWithFeed.article.isUnread) {
                    1f
                } else {
                    0.7f
                }
            )
    ) {
        Column(modifier = modifier.padding(10.dp)) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(start = 32.dp),
                    text = articleWithFeed.feed.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isStarredFilter || articleWithFeed.article.isUnread) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                )
                Text(
                    text = articleWithFeed.article.date.toString(
                        DateTimeExt.HH_MM
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = modifier.height(1.dp))
            Row {
                if (true) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(24.dp)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.inverseOnSurface,
                                RoundedCornerShape(4.dp)
                            ),
                    ) {
                        if (articleWithFeed.feed.icon == null) {
                            Icon(
                                painter = painterResource(id = R.drawable.default_folder),
                                contentDescription = "icon",
                                modifier = modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        } else {
                            Image(
                                painter = BitmapPainter(
                                    BitmapFactory.decodeByteArray(
                                        articleWithFeed.feed.icon,
                                        0,
                                        articleWithFeed.feed.icon!!.size
                                    ).asImageBitmap()
                                ),
                                contentDescription = "icon",
                                modifier = modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column {
                    Text(
                        text = articleWithFeed.article.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isStarredFilter || articleWithFeed.article.isUnread) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = modifier.height(1.dp))
                    Text(
                        text = articleWithFeed.article.shortDescription,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}