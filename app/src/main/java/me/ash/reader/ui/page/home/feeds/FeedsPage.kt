package me.ash.reader.ui.page.home.feeds

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.extension.getDesc
import me.ash.reader.ui.extension.getName
import me.ash.reader.ui.page.home.FilterBar
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewAction
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.widget.Banner
import me.ash.reader.ui.widget.Subtitle

@OptIn(
    ExperimentalMaterial3Api::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun FeedsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    feedsViewModel: FeedsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewState = feedsViewModel.viewState.collectAsStateValue()
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

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { result ->
        feedsViewModel.dispatch(FeedsViewAction.ExportAsString { string ->
            result?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                    outputStream.write(string.toByteArray())
                }
            }
        })
    }

    LaunchedEffect(Unit) {
        feedsViewModel.dispatch(FeedsViewAction.FetchAccount)
    }

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            feedsViewModel.dispatch(
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
                    IconButton(onClick = {
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
                        if (syncState.isSyncing) return@IconButton
                        homeViewModel.dispatch(HomeViewAction.Sync)
                    }) {
                        Icon(
                            modifier = Modifier.rotate(if (syncState.isSyncing) angle else 0f),
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = {
                        subscribeViewModel.dispatch(SubscribeViewAction.Show)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.subscribe),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                }
            )
        },
        content = {
            SubscribeDialog(
                openInputStreamCallback = {
                    feedsViewModel.dispatch(FeedsViewAction.ImportFromInputStream(it))
                },
            )
            LazyColumn {
                item {
                    Text(
                        modifier = Modifier
                            .padding(
                                start = 24.dp,
                                top = 48.dp,
                                end = 24.dp,
                                bottom = 24.dp
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        launcher.launch("ReadYou.opml")
                                    }
                                )
                            },
                        text = viewState.account?.name ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                item {
                    Banner(
                        title = filterState.filter.getName(),
                        desc = filterState.filter.getDesc(),
                        icon = filterState.filter.icon,
                        action = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.go_to),
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
                        modifier = Modifier.padding(start = 26.dp),
                        text = stringResource(R.string.feeds)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(viewState.groupWithFeedList) { index, groupWithFeed ->
                    Crossfade(targetState = groupWithFeed) { groupWithFeed ->
                        Column {
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
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                    Spacer(modifier = Modifier.height(64.dp))
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

