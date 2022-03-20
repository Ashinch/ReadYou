package me.ash.reader.ui.page.home.flow

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.CoroutineScope
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.formatToString
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.read.ReadViewAction
import me.ash.reader.ui.page.home.read.ReadViewModel

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.generateArticleList(
    context: Context,
    pagingItems: LazyPagingItems<ArticleWithFeed>?,
    readViewModel: ReadViewModel,
    homeViewModel: HomeViewModel,
    scope: CoroutineScope
) {
    pagingItems ?: return
    var lastItemDay: String? = null
    for (itemIndex in 0 until pagingItems.itemCount) {
        val currentItem = pagingItems.peek(itemIndex) ?: continue
        val currentItemDay = currentItem.article.date.formatToString(context)
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
                readViewModel.dispatch(ReadViewAction.ScrollToItem(0))
                readViewModel.dispatch(ReadViewAction.InitData(it))
                if (it.feed.isFullContent) readViewModel.dispatch(ReadViewAction.RenderFullContent)
                else readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                homeViewModel.dispatch(
                    HomeViewAction.ScrollToPage(
                        scope = scope,
                        targetPage = 2,
                    )
                )
            }
        }

        lastItemDay = currentItemDay
    }
}