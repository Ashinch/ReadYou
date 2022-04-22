package me.ash.reader.ui.page.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.ui.component.ViewPager
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.findActivity
import me.ash.reader.ui.page.common.ExtraName
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.feeds.option.feed.FeedOptionDrawer
import me.ash.reader.ui.page.home.feeds.option.feed.FeedOptionViewAction
import me.ash.reader.ui.page.home.feeds.option.feed.FeedOptionViewModel
import me.ash.reader.ui.page.home.feeds.option.group.GroupOptionDrawer
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.read.ReadPage
import me.ash.reader.ui.page.home.read.ReadViewAction
import me.ash.reader.ui.page.home.read.ReadViewModel

@OptIn(ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomePage(
    navController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val intent = remember { context.findActivity()?.intent }
    val scope = rememberCoroutineScope()
    val viewState = homeViewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()

    var openArticleId by rememberSaveable {
        mutableStateOf(intent?.extras?.get(ExtraName.ARTICLE_ID)?.toString() ?: "")
    }.also {
        intent?.replaceExtras(null)
    }

    LaunchedEffect(openArticleId) {
        if (openArticleId.isNotEmpty()) {
            readViewModel.dispatch(ReadViewAction.InitData(openArticleId))
            readViewModel.dispatch(ReadViewAction.ScrollToItem(0))
            homeViewModel.dispatch(HomeViewAction.ScrollToPage(scope, 2))
            openArticleId = ""
        }
    }

    BackHandler(true) {
        val currentPage = viewState.pagerState.currentPage
        if (currentPage == 0) {
            context.findActivity()?.moveTaskToBack(false)
            return@BackHandler
        }
        homeViewModel.dispatch(
            HomeViewAction.ScrollToPage(
                scope = scope,
                targetPage = when (currentPage) {
                    2 -> 1
                    else -> 0
                },
                callback = {
                    if (currentPage == 2) {
                        readViewModel.dispatch(ReadViewAction.ClearArticle)
                    }
                    if (currentPage == 0) {
                        feedOptionViewModel.dispatch(FeedOptionViewAction.Hide(scope))
                    }
                }
            )
        )
    }

    Column{
        ViewPager(
            modifier = Modifier.weight(1f),
            state = viewState.pagerState,
            composableList = listOf(
                {
                    FeedsPage(
                        navController = navController,
                        syncWorkLiveData = homeViewModel.syncWorkLiveData,
                        filterState = filterState,
                        onSyncClick = {
                            homeViewModel.dispatch(HomeViewAction.Sync)
                        },
                        onFilterChange = {
                            homeViewModel.dispatch(HomeViewAction.ChangeFilter(it))
                        },
                        onScrollToPage = {
                            homeViewModel.dispatch(
                                HomeViewAction.ScrollToPage(
                                    scope = scope,
                                    targetPage = it,
                                )
                            )
                        }
                    )
                },
                {
                    FlowPage(
                        navController = navController,
                        syncWorkLiveData = homeViewModel.syncWorkLiveData,
                        filterState = filterState,
                        onScrollToPage = {
                            homeViewModel.dispatch(
                                HomeViewAction.ScrollToPage(
                                    scope = scope,
                                    targetPage = it,
                                )
                            )
                        },
                        onFilterChange = {
                            homeViewModel.dispatch(HomeViewAction.ChangeFilter(it))
                        },
                        onItemClick = {
                            readViewModel.dispatch(ReadViewAction.ScrollToItem(0))
                            readViewModel.dispatch(ReadViewAction.InitData(it.article.id))
                            if (it.feed.isFullContent) readViewModel.dispatch(ReadViewAction.RenderFullContent)
                            else readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                            readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                            homeViewModel.dispatch(
                                HomeViewAction.ScrollToPage(
                                    scope = scope,
                                    targetPage = 2,
                                )
                            )
                        }
                    )
                },
                {
                    ReadPage(
                        navController = navController,
                        onScrollToPage = { targetPage, callback ->
                            homeViewModel.dispatch(
                                HomeViewAction.ScrollToPage(
                                    scope = scope,
                                    targetPage = targetPage,
                                    callback = callback
                                ),
                            )
                        })
                },
            ),
        )
    }

    FeedOptionDrawer()
    GroupOptionDrawer()
}