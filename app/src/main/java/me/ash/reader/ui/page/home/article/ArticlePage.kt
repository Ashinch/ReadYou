package me.ash.reader.ui.page.home.article

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.collect
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.R
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.data.constant.Filter
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.util.collectAsStateValue
import me.ash.reader.ui.util.paddingFixedHorizontal
import me.ash.reader.ui.util.roundClick
import me.ash.reader.ui.widget.AnimateLazyColumn
import me.ash.reader.ui.widget.TopTitleBox

@DelicateCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun ArticlePage(
    navController: NavHostController,
    modifier: Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    viewModel: ArticleViewModel = hiltViewModel(),
    BackOnClick: () -> Unit,
    articleOnClick: (ArticleWithFeed) -> Unit,
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()
    val pagingItems = viewState.pagingData?.collectAsLazyPagingItems()
    val refreshState = rememberSwipeRefreshState(isRefreshing = viewState.isRefreshing)
    val syncState = RssRepository.syncState.collectAsStateValue()

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            Log.i("RLog", "LaunchedEffect filterState: ")
            viewModel.dispatch(
                ArticleViewAction.FetchData(
                    groupId = state.group?.id,
                    feedId = state.feed?.id,
                    isStarred = state.filter.let { it != Filter.All && it == Filter.Starred },
                    isUnread = state.filter.let { it != Filter.All && it == Filter.Unread },
                )
            )
        }
    }

    SwipeRefresh(
        state = refreshState,
        refreshTriggerDistance = 100.dp,
        onRefresh = {
            if (syncState.isSyncing) return@SwipeRefresh
            homeViewModel.dispatch(HomeViewAction.Sync())
        }
    ) {
        Box {
            TopTitleBox(
                title = when {
                    filterState.group != null -> filterState.group.name
                    filterState.feed != null -> filterState.feed.name
                    else -> filterState.filter.title
                },
                description = if (syncState.isSyncing) {
                    "Syncing (${syncState.syncedCount}/${syncState.feedCount}) : ${syncState.currentFeedName}"
                } else {
                    "${viewState.filterImportant}${filterState.filter.description}"
                },
                listState = viewState.listState,
                startOffset = Offset(if (true) 52f else 20f, 72f),
                startHeight = 50f,
                startTitleFontSize = 24f,
                startDescriptionFontSize = 14f,
            ) {
                viewModel.dispatch(ArticleViewAction.ScrollToItem(0))
            }
            Column {
                SmallTopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(BackOnClick) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.dispatch(ArticleViewAction.PeekSyncWork)
                            Toast.makeText(context, viewState.syncWorkInfo, Toast.LENGTH_LONG)
                                .show()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = "Done All",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = {
                            if (syncState.isSyncing) return@IconButton
                            homeViewModel.dispatch(HomeViewAction.Sync())
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                )

                Column(modifier = Modifier.weight(1f)) {
                    AnimateLazyColumn(
                        state = viewState.listState,
                        reference = filterState.filter,
                    ) {
                        if (pagingItems == null) return@AnimateLazyColumn
                        var lastItemDay: String? = null
                        item {
                            Spacer(modifier = Modifier.height(74.dp))
                        }
                        for (itemIndex in 0 until pagingItems.itemCount) {
                            val currentItem = pagingItems.peek(itemIndex)
                            val currentItemDay =
                                currentItem?.article?.date?.toString(DateTimeExt.YYYY_MM_DD, true)
                                    ?: "null"
                            if (lastItemDay != currentItemDay) {
                                if (itemIndex != 0) {
                                    item { Spacer(modifier = Modifier.height(40.dp)) }
                                }
                                stickyHeader {
                                    ArticleDateHeader(currentItemDay, true)
                                }
                            }
                            item {
                                ArticleItem(
                                    modifier = modifier,
                                    articleWithFeed = pagingItems[itemIndex],
                                    isStarredFilter = filterState.filter == Filter.Starred,
                                    index = itemIndex,
                                    articleOnClick = articleOnClick,
                                )
                            }
                            lastItemDay = currentItemDay
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleItem(
    modifier: Modifier = Modifier,
    articleWithFeed: ArticleWithFeed?,
    isStarredFilter: Boolean,
    index: Int,
    articleOnClick: (ArticleWithFeed) -> Unit,
) {
    if (articleWithFeed == null) return
    Column(
        modifier = modifier
            .paddingFixedHorizontal(
                top = if (index == 0) 8.dp else 0.dp,
                bottom = 8.dp
            )
            .roundClick {
                articleOnClick(articleWithFeed)
            }
            .alpha(
                if (isStarredFilter || articleWithFeed.article.isUnread) {
                    1f
                } else {
                    0.75f
                }
            )
    ) {
        Column(modifier = modifier.padding(10.dp)) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(start = 32.dp),
                    text = articleWithFeed.feed.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isStarredFilter || articleWithFeed.article.isUnread) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                )
                Text(
                    text = articleWithFeed.article.date.toString(
                        DateTimeExt.HH_MM
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = modifier.height(1.dp))
            Row {
                if (true) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(24.dp)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.inverseOnSurface,
                                RoundedCornerShape(4.dp)
                            ),
                    ) {
                        if (articleWithFeed.feed.icon == null) {
                            Icon(
                                painter = painterResource(id = R.drawable.default_folder),
                                contentDescription = "icon",
                                modifier = modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        } else {
                            Image(
                                painter = BitmapPainter(
                                    BitmapFactory.decodeByteArray(
                                        articleWithFeed.feed.icon,
                                        0,
                                        articleWithFeed.feed.icon!!.size
                                    ).asImageBitmap()
                                ),
                                contentDescription = "icon",
                                modifier = modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column {
                    Text(
                        text = articleWithFeed.article.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isStarredFilter || articleWithFeed.article.isUnread) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = modifier.height(1.dp))
                    Text(
                        text = articleWithFeed.article.shortDescription,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleDateHeader(date: String, isDisplayIcon: Boolean) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = (if (isDisplayIcon) 52 else 20).dp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}