package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.ArticleWithFeed

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.ArticleList(
    pagingItems: LazyPagingItems<ArticleFlowItem>,
    isFilterUnread: Boolean,
    isShowFeedIcon: Boolean,
    isShowStickyHeader: Boolean,
    articleListTonalElevation: Int,
    isScrollInProgress: () -> Boolean = { false },
    onClick: (ArticleWithFeed) -> Unit = {},
    onSwipeStartToEnd: ((ArticleWithFeed) -> Unit)? = null,
    onSwipeEndToStart: ((ArticleWithFeed) -> Unit)? = null,
) {
    for (index in 0 until pagingItems.itemCount) {
        when (val item = pagingItems.peek(index)) {
            is ArticleFlowItem.Article -> {
                item(key = item.articleWithFeed.article.id) {
//                    if (item.articleWithFeed.article.isUnread) {
                    SwipeableArticleItem(
                        articleWithFeed = item.articleWithFeed,
                        isFilterUnread = isFilterUnread,
                        articleListTonalElevation = articleListTonalElevation,
                        onClick = { onClick(it) },
                        isScrollInProgress = isScrollInProgress,
                        onSwipeStartToEnd = onSwipeStartToEnd,
                        onSwipeEndToStart = onSwipeEndToStart
                    )
                    /*                    } else {
                                            // Currently we don't have swipe left to mark as unread,
                                            // so [SwipeableArticleItem] is not necessary for read articles.
                                            ArticleItem(
                                                articleWithFeed = (pagingItems[index] as ArticleFlowItem.Article).articleWithFeed,
                                            ) {
                                                onClick(it)
                                            }
                                        }*/
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
