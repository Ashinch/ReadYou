package me.ash.reader.ui.page.home.flow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.repository.SyncWorker.Companion.getIsSyncing
import me.ash.reader.ui.component.DisplayText
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.component.SwipeRefresh
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.getName
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.FilterBar
import me.ash.reader.ui.page.home.FilterState
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    com.google.accompanist.pager.ExperimentalPagerApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
)
@Composable
fun FlowPage(
    navController: NavHostController,
    flowViewModel: FlowViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel,
    pagingItems: LazyPagingItems<FlowItemView>,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var markAsRead by remember { mutableStateOf(false) }
    var onSearch by remember { mutableStateOf(false) }

    val viewState = flowViewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()
    val homeViewState = homeViewModel.viewState.collectAsStateValue()
    val listState = if (pagingItems.itemCount > 0) viewState.listState else rememberLazyListState()

    val owner = LocalLifecycleOwner.current
    var isSyncing by remember { mutableStateOf(false) }
    homeViewModel.syncWorkLiveData.observe(owner) {
        it?.let { isSyncing = it.progress.getIsSyncing() }
    }

    LaunchedEffect(onSearch) {
        snapshotFlow { onSearch }.collect {
            if (it) {
                delay(100)  // ???
                focusRequester.requestFocus()
            } else {
                keyboardController?.hide()
                if (homeViewState.searchContent.isNotBlank()) {
                    homeViewModel.dispatch(HomeViewAction.InputSearchContent(""))
                }
            }
        }
    }

    LaunchedEffect(viewState.listState) {
        snapshotFlow { viewState.listState.firstVisibleItemIndex }.collect {
            if (it > 0) {
                keyboardController?.hide()
            }
        }
    }

    BackHandler(onSearch) {
        onSearch = false
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        onSearch = false
                        navController.popBackStack()
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = !filterState.filter.isStarred(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        FeedbackIconButton(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = stringResource(R.string.mark_all_as_read),
                            tint = if (markAsRead) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ) {
                            scope.launch {
                                viewState.listState.scrollToItem(0)
                                markAsRead = !markAsRead
                                onSearch = false
                            }
                        }
                    }
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.search),
                        tint = if (onSearch) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    ) {
                        scope.launch {
                            viewState.listState.scrollToItem(0)
                            onSearch = !onSearch
                        }
                    }
                }
            )
        },
        content = {
//                if (pagingItems.loadState.source.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
//                    LottieAnimation(
//                        modifier = Modifier
//                            .alpha(0.7f)
//                            .padding(80.dp),
//                        url = "https://assets7.lottiefiles.com/packages/lf20_l4ny0jjm.json",
//                    )
//                }
            SwipeRefresh(
                onRefresh = {
                    if (!isSyncing) {
                        flowViewModel.dispatch(FlowViewAction.Sync)
                    }
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                ) {
                    item {
                        DisplayTextHeader(filterState, isSyncing)
                        AnimatedVisibility(
                            visible = markAsRead,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Spacer(modifier = Modifier.height((56 + 24 + 10).dp))
                        }
                        MarkAsReadBar(
                            visible = markAsRead,
                            absoluteY = if (isSyncing) (4 + 16 + 180).dp else 180.dp,
                            onDismissRequest = {
                                markAsRead = false
                            },
                        ) {
                            markAsRead = false
                            flowViewModel.dispatch(
                                FlowViewAction.MarkAsRead(
                                    groupId = filterState.group?.id,
                                    feedId = filterState.feed?.id,
                                    articleId = null,
                                    markAsReadBefore = it,
                                )
                            )
                        }
                        AnimatedVisibility(
                            visible = onSearch,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            SearchBar(
                                value = homeViewState.searchContent,
                                placeholder = when {
                                    filterState.group != null -> stringResource(
                                        R.string.search_for_in,
                                        filterState.filter.getName(),
                                        filterState.group.name
                                    )
                                    filterState.feed != null -> stringResource(
                                        R.string.search_for_in,
                                        filterState.filter.getName(),
                                        filterState.feed.name
                                    )
                                    else -> stringResource(
                                        R.string.search_for,
                                        filterState.filter.getName()
                                    )
                                },
                                focusRequester = focusRequester,
                                onValueChange = {
                                    homeViewModel.dispatch(HomeViewAction.InputSearchContent(it))
                                },
                                onClose = {
                                    onSearch = false
                                    homeViewModel.dispatch(HomeViewAction.InputSearchContent(""))
                                }
                            )
                            Spacer(modifier = Modifier.height((56 + 24 + 10).dp))
                        }
                    }
                    ArticleList(
                        pagingItems = pagingItems,
                    ) {
                        onSearch = false
                        navController.navigate("${RouteName.READING}/${it.article.id}") {
                            popUpTo(RouteName.FLOW)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                        if (pagingItems.loadState.source.refresh is LoadState.NotLoading && pagingItems.itemCount != 0) {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }
            }
        },
        bottomBar = {
            FilterBar(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                filter = filterState.filter,
                filterOnClick = {
                    flowViewModel.dispatch(FlowViewAction.ScrollToItem(0))
                    homeViewModel.dispatch(HomeViewAction.ChangeFilter(filterState.copy(filter = it)))
                    homeViewModel.dispatch(HomeViewAction.FetchArticles)
                },
            )
        }
    )
}

@Composable
private fun DisplayTextHeader(
    filterState: FilterState,
    isSyncing: Boolean
) {
    DisplayText(
        modifier = Modifier.padding(start = 30.dp),
        text = when {
            filterState.group != null -> filterState.group.name
            filterState.feed != null -> filterState.feed.name
            else -> filterState.filter.getName()
        },
        desc = if (isSyncing) stringResource(R.string.syncing) else "",
    )
}
