package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import me.ash.reader.data.entity.ArticleWithFeed

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.ArticleList(
    pagingItems: LazyPagingItems<FlowItemView>,
    onClick: (ArticleWithFeed) -> Unit = {},
) {
    for (index in 0 until pagingItems.itemCount) {
        when (val item = pagingItems.peek(index)) {
            is FlowItemView.Article -> {
                item(key = item.articleWithFeed.article.id) {
                    ArticleItem(
                        articleWithFeed = (pagingItems[index] as FlowItemView.Article).articleWithFeed,
                    ) {
                        onClick(it)
                    }
                }
            }
            is FlowItemView.Date -> {
                val separator = pagingItems[index] as FlowItemView.Date
                if (separator.showSpacer) item { Spacer(modifier = Modifier.height(40.dp)) }
                stickyHeader {
                    StickyHeader(separator.date)
                }
            }
            else -> {}
        }
    }
}