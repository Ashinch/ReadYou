package me.ash.reader.ui.page.home.feed

import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.flow.collect
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.data.constant.Filter
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupWithFeed
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.util.collectAsStateValue
import me.ash.reader.ui.widget.*


@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun FeedPage(
    navController: NavHostController,
    modifier: Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    filter: Filter,
    groupAndFeedOnClick: (currentGroup: Group?, currentFeed: Feed?) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val syncState = RssRepository.syncState.collectAsStateValue()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        Log.i("RLog", "launcher: ${it}")
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                viewModel.dispatch(FeedViewAction.AddFromFile(inputStream))
            }
        }
    }

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            viewModel.dispatch(
                FeedViewAction.FetchData(
                    isStarred = state.filter.let { it != Filter.All && it == Filter.Starred },
                    isUnread = state.filter.let { it != Filter.All && it == Filter.Unread },
                )
            )
        }
    }

    DisposableEffect(Unit) {
        viewModel.dispatch(
            FeedViewAction.FetchAccount()
        )
        onDispose { }
    }

    Box(
        modifier.fillMaxSize()
    ) {
        TopTitleBox(
            title = viewState.account?.name ?: "未知账户",
            description = if (syncState.isSyncing) {
                "Syncing (${syncState.syncedCount}/${syncState.feedCount}) : ${syncState.currentFeedName}"
            } else {
                viewState.account?.updateAt?.toString(DateTimeExt.YYYY_MM_DD_HH_MM, true)
                    ?: "从未同步"
            },
            listState = viewState.listState,
            startOffset = Offset(20f, 80f),
            startHeight = 72f,
            startTitleFontSize = 38f,
            startDescriptionFontSize = 16f,
        ) {
            viewModel.dispatch(FeedViewAction.ScrollToItem(0))
        }
        Column {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(route = RouteName.SETTINGS)
                    }) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (syncState.isSyncing) return@IconButton
                        homeViewModel.dispatch(HomeViewAction.Sync())
                    }) {
                        Icon(
                            modifier = Modifier.size(26.dp),
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = {
                        launcher.launch("*/*")
                    }) {
                        Icon(
                            modifier = Modifier.size(26.dp),
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Subscribe",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
            LazyColumn(
                state = viewState.listState,
                modifier = Modifier.weight(1f),
            ) {
                item {
                    Spacer(modifier = Modifier.height(114.dp))
                    BarButton(
                        barButtonType = ButtonType(
                            content = filter.title,
                            important = viewState.filterImportant
                        )
                    ) {
                        groupAndFeedOnClick(null, null)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    BarButton(
                        barButtonType = FirstExpandType(
                            content = "Feeds",
                            icon = Icons.Rounded.ExpandMore
                        )
                    ) {
                        viewModel.dispatch(FeedViewAction.ChangeGroupVisible)
                    }
                }
                itemsIndexed(viewState.groupWithFeedList) { index, groupWithFeed ->
                    GroupList(
                        modifier = modifier,
                        groupVisible = viewState.groupsVisible,
                        feedVisible = viewState.feedsVisible[index],
                        groupWithFeed = groupWithFeed,
                        groupAndFeedOnClick = groupAndFeedOnClick,
                        expandOnClick = {
                            viewModel.dispatch(FeedViewAction.ChangeFeedVisible(index))
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun ColumnScope.GroupList(
    modifier: Modifier = Modifier,
    groupVisible: Boolean,
    feedVisible: Boolean,
    groupWithFeed: GroupWithFeed,
    groupAndFeedOnClick: (currentGroup: Group?, currentFeed: Feed?) -> Unit = { _, _ -> },
    expandOnClick: () -> Unit
) {
    AnimatedVisibility(
        visible = groupVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = modifier) {
            BarButton(
                barButtonType = SecondExpandType(
                    content = groupWithFeed.group.name,
                    icon = Icons.Rounded.ExpandMore,
                    important = groupWithFeed.group.important ?: 0,
                ),
                iconOnClickListener = expandOnClick
            ) {
                groupAndFeedOnClick(groupWithFeed.group, null)
            }
            FeedList(
                visible = feedVisible,
                feeds = groupWithFeed.feeds,
                onClick = { currentFeed ->
                    groupAndFeedOnClick(null, currentFeed)
                }
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun ColumnScope.FeedList(
    visible: Boolean,
    feeds: List<Feed>,
    onClick: (currentFeed: Feed?) -> Unit = {},
) {
//    LaunchedEffect(feeds) {
//
//    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            feeds.forEach { feed ->
                Log.i("RLog", "FeedList: ${feed.icon}")
                BarButton(
                    barButtonType = ItemType(
//                        icon = feed.icon ?: "",
                        icon = if (feed.icon == null) {
                            null
                        } else {
                            BitmapPainter(
                                BitmapFactory.decodeByteArray(
                                    feed.icon,
                                    0,
                                    feed.icon!!.size
                                ).asImageBitmap()
                            )
                        },
                        content = feed.name,
                        important = feed.important ?: 0
                    )
                ) {
                    onClick(feed)
                }
            }
        }
    }
}