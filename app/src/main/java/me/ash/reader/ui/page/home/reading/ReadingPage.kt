package me.ash.reader.ui.page.home.reading

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import me.ash.reader.data.model.preference.LocalReadingAutoHideToolbar
import me.ash.reader.data.model.preference.LocalReadingPageTonalElevation
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.isScrollDown
import me.ash.reader.ui.page.home.HomeViewModel

@Composable
fun ReadingPage(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    readingViewModel: ReadingViewModel = hiltViewModel(),
) {
    val tonalElevation = LocalReadingPageTonalElevation.current
    val readingUiState = readingViewModel.readingUiState.collectAsStateValue()
    val homeUiState = homeViewModel.homeUiState.collectAsStateValue()
    val isShowToolBar = if (LocalReadingAutoHideToolbar.current.value) {
        readingUiState.articleWithFeed != null && !readingUiState.listState.isScrollDown()
    } else {
        true
    }

    val pagingItems = homeUiState.pagingData.collectAsLazyPagingItems().itemSnapshotList
    readingViewModel.recorderNextArticle(readingUiState, homeUiState, pagingItems)

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            it.arguments?.getString("articleId")?.let {
                readingViewModel.initData(it)
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

            Box(modifier = Modifier.fillMaxSize()) {
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
                    Content(
                        content = readingUiState.content ?: "",
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
                        onNextArticle = {
                            readingViewModel.nextArticle(navController, homeUiState.nextArticleId)
                        },
                        onFullContent = {
                            if (it) readingViewModel.renderFullContent()
                            else readingViewModel.renderDescriptionContent()
                        },
                    )
                }
            }
        }
    )
}
