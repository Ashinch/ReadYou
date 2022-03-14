package me.ash.reader.ui.page.home.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collect
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.data.constant.Filter
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feed.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feed.subscribe.SubscribeViewAction
import me.ash.reader.ui.page.home.feed.subscribe.SubscribeViewModel
import me.ash.reader.ui.widget.TopTitleBox

@Composable
fun FeedPage(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
    filter: Filter,
    groupAndFeedOnClick: (currentGroup: Group?, currentFeed: Feed?) -> Unit = { _, _ -> },
) {
    val viewState = viewModel.viewState.collectAsStateValue()
    val syncState = homeViewModel.syncState.collectAsStateValue()

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            viewModel.dispatch(
                FeedViewAction.FetchData(
                    isStarred = state.filter.let { it != Filter.All && it == Filter.Starred },
                    isUnread = state.filter.let { it != Filter.All && it == Filter.Unread },
                )
            )
        }
    }

    DisposableEffect(Unit) {
        viewModel.dispatch(
            FeedViewAction.FetchAccount()
        )
        onDispose { }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        SubscribeDialog(
            openInputStreamCallback = {
                viewModel.dispatch(FeedViewAction.AddFromFile(it))
            },
        )
        TopTitleBox(
            title = viewState.account?.name ?: "未知账户",
            description = if (syncState.isSyncing) {
                "Syncing (${syncState.syncedCount}/${syncState.feedCount}) : ${syncState.currentFeedName}"
            } else {
                viewState.account?.updateAt?.toString(DateTimeExt.YYYY_MM_DD_HH_MM, true)
                    ?: "从未同步"
            },
            listState = viewState.listState,
            startOffset = Offset(20f, 80f),
            startHeight = 72f,
            startTitleFontSize = 38f,
            startDescriptionFontSize = 16f,
        ) {
            viewModel.dispatch(FeedViewAction.ScrollToItem(0))
        }
        Column {
            FeedPageTopBar(
                navController = navController,
                isSyncing = syncState.isSyncing,
                syncOnClick = {
                    homeViewModel.dispatch(HomeViewAction.Sync())
                },
                subscribeOnClick = {
                    subscribeViewModel.dispatch(SubscribeViewAction.Show)
                },
            )
            LazyColumn(
                state = viewState.listState,
                modifier = Modifier.weight(1f),
            ) {
                item {
                    Spacer(modifier = Modifier.height(114.dp))
                    ButtonBar(
                        buttonBarType = ButtonBarType.FilterBar(
                            title = filter.title,
                            important = viewState.filterImportant,
                        ),
                        onClick = {
                            groupAndFeedOnClick(null, null)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    ButtonBar(
                        buttonBarType = ButtonBarType.AllBar(
                            title = "Feeds",
                            icon = Icons.Rounded.ExpandMore
                        ),
                        onClick = {
                            viewModel.dispatch(FeedViewAction.ChangeGroupVisible)
                        }
                    )
                }
                itemsIndexed(viewState.groupWithFeedList) { index, groupWithFeed ->
                    GroupList(
                        modifier = modifier,
                        groupVisible = viewState.groupsVisible,
                        feedVisible = viewState.feedsVisible[index],
                        groupWithFeed = groupWithFeed,
                        groupAndFeedOnClick = groupAndFeedOnClick,
                        expandOnClick = {
                            viewModel.dispatch(FeedViewAction.ChangeFeedVisible(index))
                        }
                    )
                }
            }
        }
    }
}
