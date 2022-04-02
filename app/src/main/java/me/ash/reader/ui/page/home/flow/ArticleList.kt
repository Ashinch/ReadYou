package me.ash.reader.ui.page.home.flow

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.ui.ext.formatAsString

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.generateArticleList(
    context: Context,
    pagingItems: LazyPagingItems<ArticleWithFeed>,
    onClick: (ArticleWithFeed) -> Unit = {},
) {
    var lastItemDay: String? = null
    for (itemIndex in 0 until pagingItems.itemCount) {
        val currentItem = pagingItems.peek(itemIndex) ?: continue
        val currentItemDay = currentItem.article.date.formatAsString(context)
        if (lastItemDay != currentItemDay) {
            if (itemIndex != 0) {
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
            stickyHeader {
                StickyHeader(currentItemDay)
            }
        }
        item {
            ArticleItem(
                articleWithFeed = pagingItems[itemIndex] ?: return@item,
            ) {
                onClick(it)
            }
        }
        lastItemDay = currentItemDay
    }
}