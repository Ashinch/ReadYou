package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.ArticleWithFeed

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
fun LazyListScope.ArticleList(
    pagingItems: LazyPagingItems<ArticleFlowItem>,
    currentReadingId: String,
    isFilterUnread: Boolean,
    isShowFeedIcon: Boolean,
    isShowStickyHeader: Boolean,
    articleListTonalElevation: Int,
    onClick: (ArticleWithFeed) -> Unit = {},
    onSwipeOut: (ArticleWithFeed) -> Unit = {},
    onFinished: (List<String>, Int) -> Unit = { strings: List<String>, i: Int -> }
) {
    var count =0
    var matched = -1
    val idList = mutableListOf<String>()
    for (index in 0 until pagingItems.itemCount) {
        when (val item = pagingItems.peek(index)) {
            is ArticleFlowItem.Article -> {
                val id = item.articleWithFeed.article.id
                idList.add(id)
                if (matched < 0 && id == currentReadingId) {
                    matched = count
                }
                count++
                item(key = id) {
                    swipeToDismiss(
                        articleWithFeed = item.articleWithFeed,
                        isFilterUnread = isFilterUnread,
                        onClick = { onClick(it) },
                        onSwipeOut = { onSwipeOut(it) }
                    )
                }
            }

            is ArticleFlowItem.Date -> {
                if (item.showSpacer) {
                    count++
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
                count++
                if (isShowStickyHeader) {
                    stickyHeader(key = item.date) {
                        StickyHeader(item.date, isShowFeedIcon, articleListTonalElevation)
                    }
                } else {
                    item(key = item.date) {
                        StickyHeader(item.date, isShowFeedIcon, articleListTonalElevation)
                    }
                }
            }

            else -> {}
        }
    }
}
onFinished(idList, matched)
