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
    isFilterUnread: Boolean,
    isShowFeedIcon: Boolean,
    isShowStickyHeader: Boolean,
    articleListTonalElevation: Int,
    onClick: (ArticleWithFeed) -> Unit = {},
    onSwipeOut: (ArticleWithFeed) -> Unit = {}
) {
    for (index in 0 until pagingItems.itemCount) {
        when (val item = pagingItems.peek(index)) {
            is ArticleFlowItem.Article -> {
                item(key = item.articleWithFeed.article.id) {
                    if (item.articleWithFeed.article.isUnread) {
                        SwipeableArticleItem(
                            articleWithFeed = item.articleWithFeed,
                            isFilterUnread = isFilterUnread,
                            articleListTonalElevation = articleListTonalElevation,
                            onClick = { onClick(it) },
                            onSwipeOut = { onSwipeOut(it) }
                        )
                    } else {
                        // Currently we don't have swipe left to mark as unread,
                        // so [SwipeableArticleItem] is not necessary for read articles.
                        ArticleItem(
                            articleWithFeed = (pagingItems[index] as ArticleFlowItem.Article).articleWithFeed,
                        ) {
                            onClick(it)
                        }
                    }
                }
            }

            is ArticleFlowItem.Date -> {
                if (item.showSpacer) item { Spacer(modifier = Modifier.height(40.dp)) }
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
