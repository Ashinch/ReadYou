package me.ash.reader.ui.page.home.feeds

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.eventFlow
import androidx.navigation.NavHostController
import androidx.work.WorkInfo
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalFeedsFilterBarFilled
import me.ash.reader.infrastructure.preference.LocalFeedsFilterBarPadding
import me.ash.reader.infrastructure.preference.LocalFeedsFilterBarStyle
import me.ash.reader.infrastructure.preference.LocalFeedsFilterBarTonalElevation
import me.ash.reader.infrastructure.preference.LocalFeedsGroupListExpand
import me.ash.reader.infrastructure.preference.LocalFeedsGroupListTonalElevation
import me.ash.reader.infrastructure.preference.LocalFeedsTopBarTonalElevation
import me.ash.reader.infrastructure.preference.LocalHideEmptyGroups
import me.ash.reader.infrastructure.preference.LocalNewVersionNumber
import me.ash.reader.infrastructure.preference.LocalSkipVersionNumber
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.component.base.Banner
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.findActivity
import me.ash.reader.ui.ext.getCurrentVersion
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.FilterState
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.accounts.AccountsTab
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionDrawer
import me.ash.reader.ui.page.home.feeds.drawer.group.GroupOptionDrawer
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.page.settings.accounts.AccountViewModel
import me.ash.reader.ui.theme.Shape32
import kotlin.collections.set

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun FeedsPage(
    navController: NavHostController,
    accountViewModel: AccountViewModel = hiltViewModel(),
    feedsViewModel: FeedsViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel,
) {
    var accountTabVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val topBarTonalElevation = LocalFeedsTopBarTonalElevation.current
    val groupListTonalElevation = LocalFeedsGroupListTonalElevation.current
    val groupListExpand = LocalFeedsGroupListExpand.current
    val filterBarStyle = LocalFeedsFilterBarStyle.current
    val filterBarFilled = LocalFeedsFilterBarFilled.current
    val filterBarPadding = LocalFeedsFilterBarPadding.current
    val filterBarTonalElevation = LocalFeedsFilterBarTonalElevation.current

    val accounts = accountViewModel.accounts.collectAsStateValue(initial = emptyList())

    val feedsUiState = feedsViewModel.feedsUiState.collectAsStateValue()
    val filterUiState = homeViewModel.filterUiState.collectAsStateValue()
    val importantSum =
        feedsUiState.importantSum.collectAsStateValue(initial = stringResource(R.string.loading))
    val groupWithFeedList =
        feedsUiState.groupWithFeedList.collectAsStateValue(initial = emptyList())
    val groupsVisible: SnapshotStateMap<String, Boolean> = feedsUiState.groupsVisible
    val hasGroupVisible by remember(groupWithFeedList) { derivedStateOf { groupWithFeedList.fastAny { groupsVisible[it.group.id] == true } } }

    val newVersion = LocalNewVersionNumber.current
    val skipVersion = LocalSkipVersionNumber.current
    val currentVersion = remember { context.getCurrentVersion() }
    val listState =
        if (groupWithFeedList.isNotEmpty()) feedsUiState.listState else rememberLazyListState()

    val owner = LocalLifecycleOwner.current

    var isSyncing by remember { mutableStateOf(false) }
    val syncingState = rememberPullToRefreshState()
    val syncingScope = rememberCoroutineScope()
    val doSync: () -> Unit = {
        isSyncing = true
        syncingScope.launch {

            homeViewModel.sync()
        }
    }

    DisposableEffect(owner) {
        scope.launch {
            owner.lifecycle.eventFlow.collect {
                when (it) {
                    Lifecycle.Event.ON_RESUME,
                    Lifecycle.Event.ON_PAUSE -> {
                        homeViewModel.commitDiff()
                    }

                    else -> {/* no-op */
                    }
                }
            }
        }
        homeViewModel.syncWorkLiveData.observe(owner) { workInfoList ->
            workInfoList.let {
                isSyncing = it.any { workInfo -> workInfo.state == WorkInfo.State.RUNNING }
            }
        }
        onDispose { homeViewModel.syncWorkLiveData.removeObservers(owner) }
    }

    fun expandAllGroups() {
        groupWithFeedList.forEach { groupWithFeed ->
            groupsVisible[groupWithFeed.group.id] = true
        }
    }

    fun collapseAllGroups() {
        groupWithFeedList.forEach { groupWithFeed ->
            groupsVisible[groupWithFeed.group.id] = false
        }
    }

    val groupDrawerState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val feedDrawerState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    LaunchedEffect(Unit) {
        feedsViewModel.fetchAccount()
    }

    val hideEmptyGroups = LocalHideEmptyGroups.current.value

    LaunchedEffect(filterUiState, isSyncing) {
        snapshotFlow { filterUiState }.collect {
            feedsViewModel.pullFeeds(it, hideEmptyGroups)
        }
    }

    BackHandler(true) {
        context.findActivity()?.moveTaskToBack(false)
    }

    RYScaffold(
        topBarTonalElevation = topBarTonalElevation.value.dp,
//        containerTonalElevation = groupListTonalElevation.value.dp,
        topBar = {
            TopAppBar(
                modifier = Modifier.clickable(
                    onClick = {
                        scope.launch {
                            if (listState.firstVisibleItemIndex != 0) {
                                listState.animateScrollToItem(0)
                            }
                        }
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
                title = {},
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
                    if (subscribeViewModel.rssService.get().addSubscription) {
                        FeedbackIconButton(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.subscribe),
                            tint = MaterialTheme.colorScheme.onSurface,
                        ) {
                            subscribeViewModel.showDrawer()
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        topBarTonalElevation.value.dp
                    ),
                )
            )
        },
        content = {
            PullToRefreshBox(
                state = syncingState,
                isRefreshing = isSyncing,
                onRefresh = doSync
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    item {
                        DisplayText(
                            text = feedsUiState.account?.name ?: "",
                            desc = "",
                        ) { accountTabVisible = true }
                    }
                    item {
                        FeedsBanner(
                            filter = filterUiState.filter,
                            desc = importantSum,
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 26.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.feeds),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                            IconButton(
                                onClick = {
                                    if (hasGroupVisible) collapseAllGroups() else expandAllGroups()
                                }, modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasGroupVisible) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                                    contentDescription = stringResource(R.string.unfold_less),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(groupWithFeedList) { _, (group, feeds) ->

                        GroupWithFeedsContainer {
                            GroupItem(
                                isExpanded = {
                                    groupsVisible.getOrPut(
                                        group.id,
                                        groupListExpand::value
                                    )
                                },
                                group = group,
                                onExpanded = {
                                    groupsVisible[group.id] =
                                        groupsVisible.getOrPut(
                                            group.id,
                                            groupListExpand::value
                                        ).not()
                                },
                                onLongClick = {
                                    scope.launch {
                                        groupDrawerState.show()
                                    }
                                }
                            ) {
                                filterChange(
                                    navController = navController,
                                    homeViewModel = homeViewModel,
                                    filterState = filterUiState.copy(
                                        group = group,
                                        feed = null,
                                    )
                                )
                            }

                            feeds.forEachIndexed { index, feed ->
                                FeedItem(
                                    feed = feed,
                                    isLastItem = { index == feeds.lastIndex },
                                    isExpanded = {
                                        groupsVisible.getOrPut(
                                            feed.groupId,
                                            groupListExpand::value
                                        )
                                    }, onClick = {
                                        filterChange(
                                            navController = navController,
                                            homeViewModel = homeViewModel,
                                            filterState = filterUiState.copy(
                                                group = null,
                                                feed = feed,
                                            )
                                        )
                                    }, onLongClick = {
                                        scope.launch {
                                            feedDrawerState.show()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(128.dp))
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
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

    SubscribeDialog(subscribeViewModel = subscribeViewModel)

    GroupOptionDrawer(drawerState = groupDrawerState)
    FeedOptionDrawer(drawerState = feedDrawerState)

    AccountsTab(
        visible = accountTabVisible,
        accounts = accounts,
        onAccountSwitch = {
            accountViewModel.switchAccount(it) {
                accountTabVisible = false
                navController.navigate(RouteName.SETTINGS)
                navController.navigate(RouteName.FEEDS) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        onClickSettings = {
            accountTabVisible = false
            navController.navigate("${RouteName.ACCOUNT_DETAILS}/${context.currentAccountId}")
        },
        onClickManage = {
            accountTabVisible = false
            navController.navigate(RouteName.ACCOUNTS)
        },
        onDismissRequest = {
            accountTabVisible = false
        },
    )
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
