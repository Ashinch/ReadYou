package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
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
    onToggleStarred: (ArticleWithFeed) -> Unit = { },
    onToggleRead: (ArticleWithFeed) -> Unit = { },
    onMarkAboveAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onShare: ((ArticleWithFeed) -> Unit)? = null,
) {
    // https://issuetracker.google.com/issues/193785330
    // FIXME: Using sticky header with paging-compose need to iterate through the entire list
    //  to figure out where to add sticky headers, which significantly impacts the performance
    if (!isShowStickyHeader) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(::key),
            contentType = pagingItems.itemContentType(::contentType)
        ) { index ->
            when (val item = pagingItems[index]) {
                is ArticleFlowItem.Article -> {
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

                is ArticleFlowItem.Date -> {
                    if (item.showSpacer) {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                    StickyHeader(item.date, isShowFeedIcon, articleListTonalElevation)
                }

                else -> {}
            }
        }
    } else {
        for (index in 0 until pagingItems.itemCount) {
            when (val item = pagingItems.peek(index)) {
                is ArticleFlowItem.Article -> {
                    item(key = key(item), contentType = contentType(item)) {
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
                    if (item.showSpacer) {
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                    stickyHeader(key = key(item), contentType = contentType(item)) {
                        StickyHeader(item.date, isShowFeedIcon, articleListTonalElevation)
                    }
                }

                else -> {}
            }
        }
    }
}

private fun key(item: ArticleFlowItem): String {
    return when (item) {
        is ArticleFlowItem.Article -> item.articleWithFeed.article.id
        is ArticleFlowItem.Date -> item.date
    }
}

private fun contentType(item: ArticleFlowItem): Int {
    return when (item) {
        is ArticleFlowItem.Article -> ARTICLE
        is ArticleFlowItem.Date -> DATE
    }
}

private const val ARTICLE = 1
private const val DATE = 2