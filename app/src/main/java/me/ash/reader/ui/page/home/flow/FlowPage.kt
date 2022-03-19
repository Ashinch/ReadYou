package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.collect
import me.ash.reader.ui.extension.collectAsStateValue
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
    val scope = rememberCoroutineScope()
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()
    val pagingItems = viewState.pagingData?.collectAsLazyPagingItems()

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
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = "Read All",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
        content = {
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
                            else -> filterState.filter.name
                        },
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                generateArticleList(pagingItems, readViewModel, homeViewModel, scope)
            }
        }
    )
}