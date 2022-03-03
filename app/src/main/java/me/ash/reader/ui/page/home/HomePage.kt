package me.ash.reader.ui.page.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.ash.reader.data.constant.Symbol
import me.ash.reader.ui.page.home.article.ArticlePage
import me.ash.reader.ui.page.home.feed.FeedPage
import me.ash.reader.ui.page.home.read.ReadPage
import me.ash.reader.ui.page.home.read.ReadViewAction
import me.ash.reader.ui.page.home.read.ReadViewModel
import me.ash.reader.ui.util.collectAsStateValue
import me.ash.reader.ui.util.findActivity
import me.ash.reader.ui.util.pagerAnimate
import me.ash.reader.ui.widget.AppNavigationBar

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun HomePage(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val filterState = viewModel.filterState.collectAsStateValue()
    val readState = readViewModel.viewState.collectAsStateValue()
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        context.findActivity()?.let { activity ->
            activity.intent?.let { intent ->
                intent.extras?.get(Symbol.EXTRA_ARTICLE_ID)?.let {
                    readViewModel.dispatch(ReadViewAction.ScrollToItem(2))
                    scope.launch {
                        val article =
                            readViewModel.articleRepository.findArticleById(it.toString().toInt())
                                ?: return@launch
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
                intent.extras?.clear()
            }
        }

        onDispose { }
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

//    val items = listOf(
//        Color.Red,
//        Color.Blue,
//        Color.Green,
//    )

    Column {
//        CustomPager(
//            items = items,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(256.dp),
//            itemFraction = .75f,
//            overshootFraction = .75f,
//            initialIndex = 3,
//            itemSpacing = 16.dp,
//        ) {
//            items.forEachIndexed { index, item ->
//                if (index % 2 == 0) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(item),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = item.toString(),
//                            modifier = Modifier.padding(all = 16.dp),
////                            style = MaterialTheme.typography.h6,
//                        )
//                    }
//                } else {
//                    Image(
//                        modifier = Modifier.fillMaxSize(),
//                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                        contentDescription = null,
//                    )
//                }
//            }
//        }

        HorizontalPager(
            count = 3,
            state = viewState.pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> FeedPage(
                    navController = navController,
                    modifier = Modifier.pagerAnimate(this, page),
                    filter = filterState.filter,
                    groupAndFeedOnClick = { currentGroup, currentFeed ->
                        viewModel.dispatch(
                            HomeViewAction.ChangeFilter(
                                filterState.copy(
                                    group = currentGroup,
                                    feed = currentFeed,
                                )
                            )
                        )
                        viewModel.dispatch(
                            HomeViewAction.ScrollToPage(
                                scope = scope,
                                targetPage = 1,
                            )
                        )
                    },
                )
                1 -> ArticlePage(
                    navController = navController,
                    modifier = Modifier.pagerAnimate(this, page),
                    BackOnClick = {
                        viewModel.dispatch(
                            HomeViewAction.ScrollToPage(
                                scope = scope,
                                targetPage = 0,
                            )
                        )
                    },
                    articleOnClick = {
                        readViewModel.dispatch(ReadViewAction.ScrollToItem(0))
                        readViewModel.dispatch(ReadViewAction.InitData(it))
                        if (it.feed.isFullContent) readViewModel.dispatch(ReadViewAction.RenderFullContent)
                        else readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                        readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                        viewModel.dispatch(
                            HomeViewAction.ScrollToPage(
                                scope = scope,
                                targetPage = 2,
                            )
                        )
                    },
                )
                2 -> ReadPage(
                    navController = navController,
                    modifier = Modifier.pagerAnimate(this, page),
                    btnBackOnClickListener = {
                        viewModel.dispatch(
                            HomeViewAction.ScrollToPage(
                                scope = scope,
                                targetPage = 1,
                                callback = {
                                    readViewModel.dispatch(ReadViewAction.ClearArticle)
                                }
                            )
                        )
                    },
                )
            }
        }
        AppNavigationBar(
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
}