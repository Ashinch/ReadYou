package me.ash.reader.ui.page.home.feeds

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.map
import me.ash.reader.R
import me.ash.reader.data.entity.toVersion
import me.ash.reader.data.repository.SyncWorker.Companion.getIsSyncing
import me.ash.reader.ui.component.Banner
import me.ash.reader.ui.component.DisplayText
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.component.Subtitle
import me.ash.reader.ui.ext.*
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.FilterBar
import me.ash.reader.ui.page.home.FilterState
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.option.feed.FeedOptionDrawer
import me.ash.reader.ui.page.home.feeds.option.group.GroupOptionDrawer
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewAction
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(
    ExperimentalMaterial3Api::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun FeedsPage(
    navController: NavHostController,
    feedsViewModel: FeedsViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel,
) {
    val context = LocalContext.current
    val feedsViewState = feedsViewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()

    val skipVersion = context.dataStore.data
        .map { it[DataStoreKeys.SkipVersionNumber.key] ?: "" }
        .collectAsState(initial = "")
        .value
        .toVersion()
    val latestVersion = context.dataStore.data
        .map { it[DataStoreKeys.NewVersionNumber.key] ?: "" }
        .collectAsState(initial = "")
        .value
        .toVersion()
    val currentVersion by remember { mutableStateOf(context.getCurrentVersion()) }

    val owner = LocalLifecycleOwner.current
    var isSyncing by remember { mutableStateOf(false) }
    homeViewModel.syncWorkLiveData.observe(owner) {
        it?.let { isSyncing = it.progress.getIsSyncing() }
    }

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

    LaunchedEffect(filterState) {
        snapshotFlow { filterState }.collect {
            feedsViewModel.dispatch(FeedsViewAction.FetchData(it))
        }
    }

    BackHandler(true) {
        context.findActivity()?.moveTaskToBack(false)
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
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                        showBadge = latestVersion.whetherNeedUpdate(currentVersion, skipVersion),
                    ) {
                        navController.navigate(RouteName.SETTINGS) {
                            popUpTo(RouteName.FEEDS)
                        }
                    }
                },
                actions = {
                    FeedbackIconButton(
                        modifier = Modifier.rotate(if (isSyncing) angle else 0f),
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        if (!isSyncing) homeViewModel.dispatch(HomeViewAction.Sync)
                    }
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.subscribe),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        subscribeViewModel.dispatch(SubscribeViewAction.Show)
                    }
                }
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    launcher.launch("ReadYou.opml")
                                }
                            )
                        },
                        text = feedsViewState.account?.name ?: "",
                        desc = if (isSyncing) stringResource(R.string.syncing) else "",
                    )
                }
                item {
                    Banner(
                        title = filterState.filter.getName(),
                        desc = feedsViewState.importantCount,
                        icon = filterState.filter.icon,
                        action = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.go_to),
                            )
                        },
                    ) {
                        filterChange(
                            navController = navController,
                            homeViewModel = homeViewModel,
                            filterState = filterState.copy(
                                group = null,
                                feed = null,
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
                itemsIndexed(feedsViewState.groupWithFeedList) { index, groupWithFeed ->
//                    Crossfade(targetState = groupWithFeed) { groupWithFeed ->
                    Column {
                        GroupItem(
                            group = groupWithFeed.group,
                            feeds = groupWithFeed.feeds,
                            groupOnClick = {
                                filterChange(
                                    navController = navController,
                                    homeViewModel = homeViewModel,
                                    filterState = filterState.copy(
                                        group = groupWithFeed.group,
                                        feed = null,
                                    )
                                )
                            },
                            feedOnClick = { feed ->
                                filterChange(
                                    navController = navController,
                                    homeViewModel = homeViewModel,
                                    filterState = filterState.copy(
                                        group = null,
                                        feed = feed,
                                    )
                                )
                            }
                        )
                        if (index != feedsViewState.groupWithFeedList.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
//                    }
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
                    filterChange(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        filterState = filterState.copy(filter = it),
                        isNavigate = false,
                    )
                },
            )
        }
    )

    SubscribeDialog()
    GroupOptionDrawer()
    FeedOptionDrawer()
}

private fun filterChange(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    filterState: FilterState,
    isNavigate: Boolean = true,
) {
    homeViewModel.dispatch(HomeViewAction.ChangeFilter(filterState))
    if (isNavigate) {
        navController.navigate(RouteName.FLOW) {
            popUpTo(RouteName.FEEDS)
        }
    }
}