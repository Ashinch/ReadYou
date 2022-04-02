package me.ash.reader.ui.page.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.page.home.drawer.feed.FeedOptionDrawer
import me.ash.reader.ui.page.home.drawer.feed.FeedOptionViewAction
import me.ash.reader.ui.page.home.drawer.feed.FeedOptionViewModel
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.read.ReadPage
import me.ash.reader.ui.page.home.read.ReadViewAction
import me.ash.reader.ui.page.home.read.ReadViewModel
import me.ash.reader.ui.widget.ViewPager

@OptIn(ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomePage(
    navController: NavHostController,
    extrasArticleId: Any? = null,
    homeViewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val viewState = homeViewModel.viewState.collectAsStateValue()
    val filterState = homeViewModel.filterState.collectAsStateValue()
    val syncState = homeViewModel.syncState.collectAsStateValue()

    OpenArticleByExtras(extrasArticleId)

    BackHandler(true) {
        val currentPage = viewState.pagerState.currentPage
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

    LaunchedEffect(homeViewModel.viewState) {
        homeViewModel.viewState.collect {
            Log.i(
                "RLog",
                "HomePage: ${it.pagerState.currentPage}, ${it.pagerState.targetPage}, ${it.pagerState.currentPageOffset}"
            )
        }
    }

    Column {
        ViewPager(
            modifier = Modifier.weight(1f),
            state = viewState.pagerState,
            composableList = listOf(
                {
                    FeedsPage(
                        navController = navController,
                        filterState = filterState,
                        syncState = syncState,
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
                            readViewModel.dispatch(ReadViewAction.InitData(it))
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
}