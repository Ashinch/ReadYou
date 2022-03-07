package me.ash.reader.ui.page.home.article

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.collect
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.data.constant.Filter
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.widget.AnimateLazyColumn
import me.ash.reader.ui.widget.TopTitleBox

@OptIn(ExperimentalFoundationApi::class)
@DelicateCoroutinesApi
@Composable
fun ArticlePage(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    viewModel: ArticleViewModel = hiltViewModel(),
    BackOnClick: () -> Unit,
    articleOnClick: (ArticleWithFeed) -> Unit,
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()
    val pagingItems = viewState.pagingData?.collectAsLazyPagingItems()
    val refreshState = rememberSwipeRefreshState(isRefreshing = viewState.isRefreshing)
    val syncState = homeViewModel.syncState.collectAsStateValue()

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            Log.i("RLog", "LaunchedEffect filterState: ")
            viewModel.dispatch(
                ArticleViewAction.FetchData(
                    groupId = state.group?.id,
                    feedId = state.feed?.id,
                    isStarred = state.filter.let { it != Filter.All && it == Filter.Starred },
                    isUnread = state.filter.let { it != Filter.All && it == Filter.Unread },
                )
            )
        }
    }

    SwipeRefresh(
        state = refreshState,
        refreshTriggerDistance = 100.dp,
        onRefresh = {
            if (syncState.isSyncing) return@SwipeRefresh
            homeViewModel.dispatch(HomeViewAction.Sync())
        }
    ) {
        Box {
            TopTitleBox(
                title = when {
                    filterState.group != null -> filterState.group.name
                    filterState.feed != null -> filterState.feed.name
                    else -> filterState.filter.title
                },
                description = if (syncState.isSyncing) {
                    "Syncing (${syncState.syncedCount}/${syncState.feedCount}) : ${syncState.currentFeedName}"
                } else {
                    "${viewState.filterImportant}${filterState.filter.description}"
                },
                listState = viewState.listState,
                startOffset = Offset(if (true) 52f else 20f, 72f),
                startHeight = 50f,
                startTitleFontSize = 24f,
                startDescriptionFontSize = 14f,
            ) {
                viewModel.dispatch(ArticleViewAction.ScrollToItem(0))
            }
            Column {
                ArticlePageTopBar(
                    backOnClick = BackOnClick,
                    readAllOnClick = {
                        viewModel.dispatch(ArticleViewAction.PeekSyncWork)
                        Toast.makeText(context, viewState.syncWorkInfo, Toast.LENGTH_LONG)
                            .show()
                    },
                    searchOnClick = {

                    },
                )

                Column(modifier = Modifier.weight(1f)) {
                    AnimateLazyColumn(
                        state = viewState.listState,
                        reference = filterState.filter,
                    ) {
                        if (pagingItems == null) return@AnimateLazyColumn
                        var lastItemDay: String? = null
                        item {
                            Spacer(modifier = Modifier.height(74.dp))
                        }
                        for (itemIndex in 0 until pagingItems.itemCount) {
                            val currentItem = pagingItems.peek(itemIndex)
                            val currentItemDay =
                                currentItem?.article?.date?.toString(DateTimeExt.YYYY_MM_DD, true)
                                    ?: "null"
                            if (lastItemDay != currentItemDay) {
                                if (itemIndex != 0) {
                                    item { Spacer(modifier = Modifier.height(40.dp)) }
                                }
                                stickyHeader {
                                    ArticleDateHeader(currentItemDay, true)
                                }
                            }
                            item {
                                ArticleItem(
                                    modifier = modifier,
                                    articleWithFeed = pagingItems[itemIndex],
                                    isStarredFilter = filterState.filter == Filter.Starred,
                                    index = itemIndex,
                                    articleOnClick = articleOnClick,
                                )
                            }
                            lastItemDay = currentItemDay
                        }
                    }
                }
            }
        }
    }
}