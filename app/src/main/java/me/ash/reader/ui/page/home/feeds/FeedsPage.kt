package me.ash.reader.ui.page.home.feeds

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collect
import me.ash.reader.data.constant.Symbol
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewAction
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.widget.Banner
import me.ash.reader.ui.widget.Subtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: FeedsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()
    val syncState = homeViewModel.syncState.collectAsStateValue()

    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    LaunchedEffect(Unit) {
        viewModel.dispatch(FeedsViewAction.FetchAccount())
    }

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            viewModel.dispatch(
                FeedsViewAction.FetchData(state)
            )
        }
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (syncState.isSyncing) return@IconButton
                        homeViewModel.dispatch(HomeViewAction.Sync())
                    }) {
                        Icon(
                            modifier = Modifier.rotate(if (syncState.isSyncing) angle else 0f),
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = {
                        subscribeViewModel.dispatch(SubscribeViewAction.Show)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Subscribe",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
        content = {
            SubscribeDialog(
                openInputStreamCallback = {
                    viewModel.dispatch(FeedsViewAction.AddFromFile(it))
                },
            )
            LazyColumn {
                item {
                    Text(
                        modifier = Modifier.padding(
                            start = 24.dp,
                            top = 48.dp,
                            end = 24.dp,
                            bottom = 24.dp
                        ),
                        text = viewState.account?.name ?: Symbol.Unknown,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                item {
                    Banner(
                        title = viewState.filter.name,
                        desc = "${viewState.filter.important}${viewState.filter.description}",
                        icon = viewState.filter.icon,
                        action = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowRight,
                                contentDescription = "Goto",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                    ) {
                        homeViewModel.dispatch(
                            HomeViewAction.ChangeFilter(
                                filterState.copy(
                                    group = null,
                                    feed = null
                                )
                            )
                        )
                        homeViewModel.dispatch(
                            HomeViewAction.ScrollToPage(
                                scope = scope,
                                targetPage = 1,
                            )
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Subtitle(
                        modifier = Modifier.padding(start = 28.dp),
                        text = "Feeds"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(viewState.groupWithFeedList) { index, groupWithFeed ->
                    GroupItem(
                        text = groupWithFeed.group.name,
                        feeds = groupWithFeed.feeds,
                        groupOnClick = {
                            homeViewModel.dispatch(
                                HomeViewAction.ChangeFilter(
                                    filterState.copy(
                                        group = groupWithFeed.group,
                                        feed = null
                                    )
                                )
                            )
                            homeViewModel.dispatch(
                                HomeViewAction.ScrollToPage(
                                    scope = scope,
                                    targetPage = 1,
                                )
                            )
                        },
                        feedOnClick = { feed ->
                            homeViewModel.dispatch(
                                HomeViewAction.ChangeFilter(
                                    filterState.copy(
                                        group = null,
                                        feed = feed
                                    )
                                )
                            )
                            homeViewModel.dispatch(
                                HomeViewAction.ScrollToPage(
                                    scope = scope,
                                    targetPage = 1,
                                )
                            )
                        }
                    )
                    if (index != viewState.groupWithFeedList.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}

