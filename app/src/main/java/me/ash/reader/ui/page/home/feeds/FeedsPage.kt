package me.ash.reader.ui.page.home.feeds

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import me.ash.reader.R
import me.ash.reader.data.model.getName
import me.ash.reader.data.preference.*
import me.ash.reader.data.repository.SyncWorker.Companion.getIsSyncing
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.findActivity
import me.ash.reader.ui.ext.getCurrentVersion
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.FilterState
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionDrawer
import me.ash.reader.ui.page.home.feeds.drawer.group.GroupOptionDrawer
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel

@OptIn(
    com.google.accompanist.pager.ExperimentalPagerApi::class,
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
    val topBarTonalElevation = LocalFeedsTopBarTonalElevation.current
    val groupListTonalElevation = LocalFeedsGroupListTonalElevation.current
    val groupListExpand = LocalFeedsGroupListExpand.current
    val filterBarStyle = LocalFeedsFilterBarStyle.current
    val filterBarFilled = LocalFeedsFilterBarFilled.current
    val filterBarPadding = LocalFeedsFilterBarPadding.current
    val filterBarTonalElevation = LocalFeedsFilterBarTonalElevation.current

    val feedsUiState = feedsViewModel.feedsUiState.collectAsStateValue()
    val filterUiState = homeViewModel.filterUiState.collectAsStateValue()

    val newVersion = LocalNewVersionNumber.current
    val skipVersion = LocalSkipVersionNumber.current
    val currentVersion = remember { context.getCurrentVersion() }

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
        feedsViewModel.exportAsOpml { string ->
            result?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(string.toByteArray())
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        feedsViewModel.fetchAccount()
    }

    LaunchedEffect(filterUiState) {
        snapshotFlow { filterUiState }.collect {
            feedsViewModel.fetchData(it)
        }
    }

    BackHandler(true) {
        context.findActivity()?.moveTaskToBack(false)
    }

    RYScaffold(
        topBarTonalElevation = topBarTonalElevation.value.dp,
        containerTonalElevation = groupListTonalElevation.value.dp,
        navigationIcon = {
            FeedbackIconButton(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onSurface,
                showBadge = newVersion.whetherNeedUpdate(currentVersion, skipVersion),
            ) {
                navController.navigate(RouteName.SETTINGS) {
                    launchSingleTop = true
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
                if (!isSyncing) homeViewModel.sync()
            }
            FeedbackIconButton(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.subscribe),
                tint = MaterialTheme.colorScheme.onSurface,
            ) {
                subscribeViewModel.showDrawer()
            }
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
                        text = feedsUiState.account?.name ?: stringResource(R.string.read_you),
                        desc = if (isSyncing) stringResource(R.string.syncing) else "",
                    )
                }
                item {
                    Banner(
                        title = filterUiState.filter.getName(),
                        desc = feedsUiState.importantCount.ifEmpty { stringResource(R.string.loading) },
                        icon = filterUiState.filter.iconOutline,
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
                            filterState = filterUiState.copy(
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
                itemsIndexed(feedsUiState.groupWithFeedList) { index, groupWithFeed ->
//                    Crossfade(targetState = groupWithFeed) { groupWithFeed ->
                    Column {
                        GroupItem(
                            isExpanded = groupListExpand.value,
                            tonalElevation = groupListTonalElevation.value.dp,
                            group = groupWithFeed.group,
                            feeds = groupWithFeed.feeds,
                            groupOnClick = {
                                filterChange(
                                    navController = navController,
                                    homeViewModel = homeViewModel,
                                    filterState = filterUiState.copy(
                                        group = groupWithFeed.group,
                                        feed = null,
                                    )
                                )
                            },
                            feedOnClick = { feed ->
                                filterChange(
                                    navController = navController,
                                    homeViewModel = homeViewModel,
                                    filterState = filterUiState.copy(
                                        group = null,
                                        feed = feed,
                                    )
                                )
                            }
                        )
                        if (index != feedsUiState.groupWithFeedList.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
//                    }
                }
                item {
                    Spacer(modifier = Modifier.height(128.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
        bottomBar = {
            FilterBar(
                filter = filterUiState.filter,
                filterBarStyle = filterBarStyle.value,
                filterBarFilled = filterBarFilled.value,
                filterBarPadding = filterBarPadding.dp,
                filterBarTonalElevation = filterBarTonalElevation.value.dp,
            ) {
                filterChange(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    filterState = filterUiState.copy(filter = it),
                    isNavigate = false,
                )
            }
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
    homeViewModel.changeFilter(filterState)
    if (isNavigate) {
        navController.navigate(RouteName.FLOW) {
            launchSingleTop = true
        }
    }
}