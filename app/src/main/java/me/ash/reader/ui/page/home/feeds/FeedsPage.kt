package me.ash.reader.ui.page.home.feeds

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.work.WorkInfo
import me.ash.reader.R
import me.ash.reader.data.model.preference.*
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.*
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.FilterState
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.accounts.AccountsTab
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionDrawer
import me.ash.reader.ui.page.home.feeds.drawer.group.GroupOptionDrawer
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.page.settings.accounts.AccountViewModel
import kotlin.math.ln

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class
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

    val newVersion = LocalNewVersionNumber.current
    val skipVersion = LocalSkipVersionNumber.current
    val currentVersion = remember { context.getCurrentVersion() }

    val owner = LocalLifecycleOwner.current
    var isSyncing by remember { mutableStateOf(false) }
    homeViewModel.syncWorkLiveData.observe(owner) {
        it?.let { isSyncing = it.any { it.state == WorkInfo.State.RUNNING } }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    val feedBadgeAlpha by remember { derivedStateOf { (ln(groupListTonalElevation.value + 1.4f) + 2f) / 100f } }
    val groupAlpha by remember { derivedStateOf { groupListTonalElevation.value.dp.alphaLN(weight = 1.2f) } }
    val groupIndicatorAlpha by remember {
        derivedStateOf {
            groupListTonalElevation.value.dp.alphaLN(
                weight = 1.4f
            )
        }
    }

    LaunchedEffect(Unit) {
        feedsViewModel.fetchAccount()
    }

    LaunchedEffect(filterUiState) {
        snapshotFlow { filterUiState }.collect {
            feedsViewModel.pullFeeds(it)
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
            if (subscribeViewModel.rssRepository.get().subscribe) {
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.subscribe),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {
                    subscribeViewModel.showDrawer()
                }
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(
                        modifier = Modifier
                            .clickable {
                                accountTabVisible = true
                            },
                        // .pointerInput(Unit) {
                        //     detectTapGestures(
                        //         onPress = {
                        //             accountTabRemember = true
                        //         },
                        //         onLongPress = {
                        //             accountTabRemember = true
                        //         }
                        //     )
                        // },
                        text = feedsUiState.account?.name ?: "",
                        desc = if (isSyncing) stringResource(R.string.syncing) else "",
                    )
                }
                item {
                    Banner(
                        title = filterUiState.filter.toName(),
                        desc = importantSum,
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
                itemsIndexed(groupWithFeedList) { index, groupWithFeed ->
                    when (groupWithFeed) {
                        is GroupFeedsView.Group -> {
                            if (index != 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            GroupItem(
                                isExpanded = {
                                    groupsVisible.getOrPut(
                                        groupWithFeed.group.id,
                                        groupListExpand::value
                                    )
                                },
                                group = groupWithFeed.group,
                                alpha = groupAlpha,
                                indicatorAlpha = groupIndicatorAlpha,
                                isEnded = { index == groupWithFeedList.lastIndex },
                                onExpanded = {
                                    groupsVisible[groupWithFeed.group.id] = groupsVisible.getOrPut(
                                        groupWithFeed.group.id,
                                        groupListExpand::value
                                    ).not()
                                }
                            ) {
                                filterChange(
                                    navController = navController,
                                    homeViewModel = homeViewModel,
                                    filterState = filterUiState.copy(
                                        group = groupWithFeed.group,
                                        feed = null,
                                    )
                                )
                            }
                        }

                        is GroupFeedsView.Feed -> {
                            FeedItem(
                                feed = groupWithFeed.feed,
                                alpha = groupAlpha,
                                badgeAlpha = feedBadgeAlpha,
                                isEnded = { index == groupWithFeedList.lastIndex || groupWithFeedList[index + 1] is GroupFeedsView.Group },
                                isExpanded = {
                                    groupsVisible.getOrPut(
                                        groupWithFeed.feed.groupId,
                                        groupListExpand::value
                                    )
                                },
                            ) {
                                filterChange(
                                    navController = navController,
                                    homeViewModel = homeViewModel,
                                    filterState = filterUiState.copy(
                                        group = null,
                                        feed = groupWithFeed.feed,
                                    )
                                )
                            }
                        }
                    }
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
