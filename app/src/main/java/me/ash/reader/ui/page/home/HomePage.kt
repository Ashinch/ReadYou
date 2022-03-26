package me.ash.reader.ui.page.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.extension.findActivity
import me.ash.reader.ui.page.common.ExtraName
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
    viewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = viewModel.filterState.collectAsStateValue()
    val readState = readViewModel.viewState.collectAsStateValue()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        context.findActivity()?.let { activity ->
            activity.intent?.let { intent ->
                intent.extras?.get(ExtraName.ARTICLE_ID)?.let {
                    readViewModel.dispatch(ReadViewAction.ScrollToItem(2))
                    scope.launch {
                        val article = readViewModel
                            .rssRepository.get()
                            .findArticleById(it.toString()) ?: return@launch
                        readViewModel.dispatch(ReadViewAction.InitData(article))
                        if (article.feed.isFullContent) readViewModel.dispatch(ReadViewAction.RenderFullContent)
                        else readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                        readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                        viewModel.dispatch(
                            HomeViewAction.ScrollToPage(
                                scope = scope,
                                targetPage = 2,
                            )
                        )
                    }
                }
                intent.extras?.remove(ExtraName.ARTICLE_ID)
            }
        }
    }

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
                    FeedsPage(navController = navController)
                },
                {
                    FlowPage(navController = navController)
                },
                {
                    ReadPage(navController = navController)
                },
            ),
        )
        HomeBottomNavBar(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(),
            pagerState = viewState.pagerState,
            disabled = readState.articleWithFeed == null,
            isUnread = readState.articleWithFeed?.article?.isUnread ?: false,
            isStarred = readState.articleWithFeed?.article?.isStarred ?: false,
            isFullContent = readState.articleWithFeed?.feed?.isFullContent ?: false,
            unreadOnClick = {
                readViewModel.dispatch(ReadViewAction.MarkUnread(it))
            },
            starredOnClick = {
                readViewModel.dispatch(ReadViewAction.MarkStarred(it))
            },
            fullContentOnClick = { afterIsFullContent ->
                readState.articleWithFeed?.let {
                    if (afterIsFullContent) readViewModel.dispatch(ReadViewAction.RenderFullContent)
                    else readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                }
            },
            filter = filterState.filter,
            filterOnClick = {
                viewModel.dispatch(
                    HomeViewAction.ChangeFilter(
                        filterState.copy(
                            filter = it
                        )
                    )
                )
            },
        )
    }

    FeedOptionDrawer()
}