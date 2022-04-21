package me.ash.reader.ui.page.home.feeds

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
import androidx.lifecycle.LiveData
import androidx.navigation.NavHostController
import androidx.work.WorkInfo
import me.ash.reader.R
import me.ash.reader.data.repository.SyncWorker.Companion.getIsSyncing
import me.ash.reader.ui.component.Banner
import me.ash.reader.ui.component.DisplayText
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.component.Subtitle
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.getDesc
import me.ash.reader.ui.ext.getName
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.FilterBar
import me.ash.reader.ui.page.home.FilterState
import me.ash.reader.ui.page.home.feeds.option.feed.FeedOptionDrawer
import me.ash.reader.ui.page.home.feeds.option.group.GroupOptionDrawer
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeDialog
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewAction
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel

@OptIn(
    ExperimentalMaterial3Api::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun FeedsPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    feedsViewModel: FeedsViewModel = hiltViewModel(),
    syncWorkLiveData: LiveData<WorkInfo>,
    filterState: FilterState,
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
    onSyncClick: () -> Unit = {},
    onFilterChange: (filterState: FilterState) -> Unit = {},
    onScrollToPage: (targetPage: Int) -> Unit = {},
) {
    val context = LocalContext.current
    val viewState = feedsViewModel.viewState.collectAsStateValue()

    val owner = LocalLifecycleOwner.current
    var isSyncing by remember { mutableStateOf(false) }
    syncWorkLiveData.observe(owner) {
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
        feedsViewModel.dispatch(FeedsViewAction.FetchData(filterState))
    }

    LaunchedEffect(isSyncing) {
        if (!isSyncing) {
            feedsViewModel.dispatch(FeedsViewAction.FetchData(filterState))
        }
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
                    ) {
                        navController.navigate(RouteName.SETTINGS)
                    }
                },
                actions = {
                    FeedbackIconButton(
                        modifier = Modifier.rotate(if (isSyncing) angle else 0f),
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        if (!isSyncing) {
                            onSyncClick()
                        }
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
            SubscribeDialog()
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
                        text = viewState.account?.name ?: stringResource(R.string.unknown),
                        desc = if (isSyncing) stringResource(R.string.syncing) else "",
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
                        onFilterChange(
                            filterState.copy(
                                group = null,
                                feed = null
                            )
                        )
                        onScrollToPage(1)
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
//                    Crossfade(targetState = groupWithFeed) { groupWithFeed ->
                    Column {
                        GroupItem(
                            group = groupWithFeed.group,
                            feeds = groupWithFeed.feeds,
                            groupOnClick = {
                                onFilterChange(
                                    filterState.copy(
                                        group = groupWithFeed.group,
                                        feed = null
                                    )
                                )
                                onScrollToPage(1)
                            },
                            feedOnClick = { feed ->
                                onFilterChange(
                                    filterState.copy(
                                        group = null,
                                        feed = feed
                                    )
                                )
                                onScrollToPage(1)
                            }
                        )
                        if (index != viewState.groupWithFeedList.lastIndex) {
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
                    onFilterChange(
                        filterState.copy(
                            filter = it
                        )
                    )
                },
            )
        }
    )

    FeedOptionDrawer()
    GroupOptionDrawer()
}

