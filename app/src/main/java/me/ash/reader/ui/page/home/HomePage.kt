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
    viewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = viewModel.filterState.collectAsStateValue()
    val syncState = viewModel.syncState.collectAsStateValue()

    OpenArticleByExtras(extrasArticleId)

    BackHandler(true) {
        val currentPage = viewState.pagerState.currentPage
        viewModel.dispatch(
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

    LaunchedEffect(viewModel.viewState) {
        viewModel.viewState.collect {
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
                            viewModel.dispatch(HomeViewAction.Sync)
                        },
                        onFilterChange = {
                            viewModel.dispatch(HomeViewAction.ChangeFilter(it))
                        },
                        onScrollToPage = {
                            viewModel.dispatch(
                                HomeViewAction.ScrollToPage(
                                    scope = scope,
                                    targetPage = it,
                                )
                            )
                        }
                    )
                },
                {
                    FlowPage(navController = navController)
                },
                {
                    ReadPage(navController = navController)
                },
            ),
        )
    }

    FeedOptionDrawer()
}