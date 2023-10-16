package me.ash.reader.ui.page.home.reading

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import me.ash.reader.infrastructure.preference.LocalReadingAutoHideToolbar
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.ArticleSwipeDirection
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.isScrollDown
import me.ash.reader.ui.ext.swipeLeftAndRight
import me.ash.reader.ui.ext.swipeableUpDown
import me.ash.reader.ui.page.home.HomeViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReadingPage(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    readingViewModel: ReadingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val tonalElevation = LocalReadingPageTonalElevation.current
    val readingUiState = readingViewModel.readingUiState.collectAsStateValue()
    val homeUiState = homeViewModel.homeUiState.collectAsStateValue()
    val readingProgressState = homeViewModel.readingProgressState.collectAsStateValue()
    val slideDirection = remember { mutableStateOf(ArticleSwipeDirection.Default.raw) }

    val isShowToolBar = if (LocalReadingAutoHideToolbar.current.value) {
        readingUiState.articleWithFeed != null && !readingUiState.listState.isScrollDown()
    } else {
        true
    }

    readingViewModel.recordCurrentReadingState(homeViewModel)

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            it.arguments?.getString("articleId")?.let { articleId ->
                if (readingUiState.articleWithFeed?.article?.id != articleId) {
                    readingViewModel.initData(articleId)
                }
            }
        }
    }

    LaunchedEffect(readingUiState.articleWithFeed?.article?.id) {
        Log.i("RLog", "ReadPage: ${readingUiState.articleWithFeed}")
        readingUiState.articleWithFeed?.let {
            if (it.article.isUnread) {
                readingViewModel.markUnread(false)
            }
        }
    }

    RYScaffold(
        topBarTonalElevation = tonalElevation.value.dp,
        containerTonalElevation = tonalElevation.value.dp,
        content = {
            Log.i("RLog", "TopBar: recomposition")

            Box(modifier = Modifier
                .fillMaxSize()
                .swipeLeftAndRight(
                    onLeft = {
                        slideDirection.value = ArticleSwipeDirection.Left.raw
                        readingViewModel.trySwitchArticle(readingProgressState.readingPrev)
                    },
                    onRight = {
                        slideDirection.value = ArticleSwipeDirection.Right.raw
                        readingViewModel.trySwitchArticle(readingProgressState.readingNext)
                    }
                )
                .swipeableUpDown(
                    onUp = {
                        if (!readingUiState.isFullContent) {
                            readingViewModel.renderFullContent()
                        }
                    },
                    onDown = {
                        if (!readingUiState.isFullContent) {
                            readingViewModel.renderFullContent()
                        }
                    })) {
                // Top Bar
                TopBar(
                    navController = navController,
                    isShow = isShowToolBar,
                    title = readingUiState.articleWithFeed?.article?.title,
                    link = readingUiState.articleWithFeed?.article?.link,
                    onClose = {
                        navController.popBackStack()
                    },
                )

                // Content
                if (readingUiState.articleWithFeed != null) {
                    AnimatedContent(
                        targetState = readingUiState.content ?: "",
                        transitionSpec = {
                            when (slideDirection.value) {
                                ArticleSwipeDirection.Left.raw, ArticleSwipeDirection.Right.raw -> {
                                    val symbol = if (slideDirection.value == ArticleSwipeDirection.Right.raw) 1 else -1
                                    slideInHorizontally { width -> symbol * width } + fadeIn() with
                                            slideOutHorizontally { width -> symbol * (-width) } + fadeOut()
                                }
                                ArticleSwipeDirection.Down.raw -> {
                                    slideInVertically(
                                        spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessLow,
                                        )
                                    ) { height -> height / 2 } with slideOutVertically { height -> -(height / 2) } + fadeOut(
                                        spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessLow,
                                        )
                                    )
                                }
                                else -> {
                                    fadeIn() with fadeOut()
                                }
                            }

                        }
                    ) { target ->
                        Content(
                            content = target,
                            feedName = readingUiState.articleWithFeed.feed.name,
                            title = readingUiState.articleWithFeed.article.title,
                            author = readingUiState.articleWithFeed.article.author,
                            link = readingUiState.articleWithFeed.article.link,
                            publishedDate = readingUiState.articleWithFeed.article.date,
                            isLoading = readingUiState.isLoading,
                            listState = readingUiState.listState,
                            isShowToolBar = isShowToolBar,
                        )
                    }
                }
                // Bottom Bar
                if (readingUiState.articleWithFeed != null) {
                    BottomBar(
                        isShow = isShowToolBar,
                        isUnread = readingUiState.articleWithFeed.article.isUnread,
                        isStarred = readingUiState.articleWithFeed.article.isStarred,
                        isFullContent = readingUiState.isFullContent,
                        onUnread = {
                            readingViewModel.markUnread(it)
                        },
                        onStarred = {
                            readingViewModel.markStarred(it)
                        },
                        onFullContent = {
                            if (it) readingViewModel.renderFullContent()
                            else readingViewModel.renderDescriptionContent()
                        },
                        progress = "${readingProgressState.readingCurrentNumber+1} / ${readingProgressState.readingList.size}",
                    )
                }
            }
        }
    )
}
