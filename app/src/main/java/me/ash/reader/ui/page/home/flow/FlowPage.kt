package me.ash.reader.ui.page.home.flow

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.WorkInfo
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.repository.SyncWorker.Companion.getIsSyncing
import me.ash.reader.ui.component.DisplayText
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.getName
import me.ash.reader.ui.page.home.FilterBar
import me.ash.reader.ui.page.home.FilterState

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
)
@Composable
fun FlowPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    flowViewModel: FlowViewModel = hiltViewModel(),
    syncWorkLiveData: LiveData<WorkInfo>,
    filterState: FilterState,
    onFilterChange: (filterState: FilterState) -> Unit = {},
    onScrollToPage: (targetPage: Int) -> Unit = {},
    onItemClick: (item: ArticleWithFeed) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewState = flowViewModel.viewState.collectAsStateValue()
    val pagingItems = viewState.pagingData.collectAsLazyPagingItems()
    var markAsRead by remember { mutableStateOf(false) }

    val owner = LocalLifecycleOwner.current
    var isSyncing by remember { mutableStateOf(false) }
    syncWorkLiveData.observe(owner) {
        it?.let { isSyncing = it.progress.getIsSyncing() }
    }

    LaunchedEffect(filterState) {
        flowViewModel.dispatch(
            FlowViewAction.FetchData(filterState)
        )
    }

//    LaunchedEffect(viewState.listState.isScrollInProgress) {
//        Log.i("RLog", "isScrollInProgress: ${viewState.listState.isScrollInProgress}")
//        if (viewState.listState.isScrollInProgress) {
//            Log.i("RLog", "isScrollInProgress: ${true}")
//            markAsRead = false
//        }
//    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(markAsRead) {
                detectTapGestures {
                    markAsRead = false
                }
            },
        topBar = {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    FeedbackIconButton(
                        isHaptic = false,
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        onScrollToPage(0)
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = !filterState.filter.isStarred(),// && pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount != 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        FeedbackIconButton(
                            isHaptic = false,
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
                            }
                        }
                    }
                    FeedbackIconButton(
                        isHaptic = false,
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.search),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                    }
                }
            )
        },
        content = {
            Crossfade(targetState = pagingItems) { pagingItems ->
//                if (pagingItems.loadState.source.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
//                    LottieAnimation(
//                        modifier = Modifier
//                            .alpha(0.7f)
//                            .padding(80.dp),
//                        url = "https://assets7.lottiefiles.com/packages/lf20_l4ny0jjm.json",
//                    )
//                }
                LazyColumn(
                    state = viewState.listState,
                ) {
                    item {
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
                    item {
                        AnimatedVisibility(
                            visible = markAsRead,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column {
                                MarkAsReadBar()
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                    generateArticleList(
                        context = context,
                        pagingItems = pagingItems,
                    ) {
                        markAsRead = false
                        onItemClick(it)
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
                    markAsRead = false
                    onFilterChange(
                        filterState.copy(
                            filter = it
                        )
                    )
                },
            )
        }
    )
}