package me.ash.reader.ui.page.home.flow

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import me.ash.reader.R
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.extension.getName
import me.ash.reader.ui.page.home.FilterBar
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.read.ReadViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun FlowPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: FlowViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()
    val pagingItems = viewState.pagingData.collectAsLazyPagingItems()
    var markAsRead by remember { mutableStateOf(false) }

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            viewModel.dispatch(
                FlowViewAction.FetchData(state)
            )
        }
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        homeViewModel.dispatch(
                            HomeViewAction.ScrollToPage(
                                scope = scope,
                                targetPage = 0,
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.dispatch(FlowViewAction.PeekSyncWork)
                        Toast.makeText(
                            context,
                            viewState.syncWorkInfo.length.toString(),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = stringResource(R.string.mark_all_as_read),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
        content = {
            Crossfade(targetState = pagingItems) { pagingItems ->
                LazyColumn(
                    state = viewState.listState,
                ) {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = if (true) 54.dp else 24.dp,
                                    top = 48.dp,
                                    end = 24.dp,
                                    bottom = 24.dp
                                ),
                            text = when {
                                filterState.group != null -> filterState.group.name
                                filterState.feed != null -> filterState.feed.name
                                else -> filterState.filter.getName()
                            },
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    generateArticleList(context, pagingItems, readViewModel, homeViewModel, scope)
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
                    homeViewModel.dispatch(
                        HomeViewAction.ChangeFilter(
                            filterState.copy(
                                filter = it
                            )
                        )
                    )
                },
            )
        }
    )
}