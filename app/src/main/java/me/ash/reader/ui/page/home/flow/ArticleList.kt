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
    for (itemIndex in 0 until pagingItems.itemCount) {
        when (val item = pagingItems[itemIndex]) {
            is FlowItemView.Article -> {
                item {
                    ArticleItem(
                        articleWithFeed = item.articleWithFeed,
                    ) {
                        onClick(it)
                    }
                }
            }
            is FlowItemView.Date -> {
                if (itemIndex != 0) item { Spacer(modifier = Modifier.height(40.dp)) }
                stickyHeader {
                    StickyHeader(item.date)
                }
            }
            else -> {}
        }
    }
}