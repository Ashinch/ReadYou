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
    isSwipeEnabled: () -> Boolean = { false },
    isMenuEnabled: Boolean = true,
    onClick: (ArticleWithFeed) -> Unit = {},
    onToggleStarred: (ArticleWithFeed, Long) -> Unit = { _, _ -> },
    onToggleRead: (ArticleWithFeed, Long) -> Unit = { _, _ -> },
    onMarkAboveAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onShare: ((ArticleWithFeed) -> Unit)? = null,
) {
    for (index in 0 until pagingItems.itemCount) {
        when (val item = pagingItems.peek(index)) {
            is ArticleFlowItem.Article -> {
                item(key = item.articleWithFeed.article.id) {
                    SwipeableArticleItem(
                        articleWithFeed = item.articleWithFeed,
                        isFilterUnread = isFilterUnread,
                        articleListTonalElevation = articleListTonalElevation,
                        onClick = onClick,
                        isSwipeEnabled = isSwipeEnabled,
                        isMenuEnabled = isMenuEnabled,
                        onToggleStarred = onToggleStarred,
                        onToggleRead = onToggleRead,
                        onMarkAboveAsRead = if (index == 1) null else onMarkAboveAsRead, // index == 0 -> ArticleFlowItem.Date
                        onMarkBelowAsRead = if (index == pagingItems.itemCount - 1) null else onMarkBelowAsRead,
                        onShare = onShare
                    )
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
