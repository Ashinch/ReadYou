package me.ash.reader.ui.page.home.reading

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import me.ash.reader.infrastructure.preference.LocalReadingAutoHideToolbar
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.isScrollDown
import me.ash.reader.ui.motion.materialSharedAxisY
import me.ash.reader.ui.page.home.HomeViewModel

@Composable
fun ReadingPage(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    readingViewModel: ReadingViewModel = hiltViewModel(),
) {
    val tonalElevation = LocalReadingPageTonalElevation.current
    val readingUiState = readingViewModel.readingUiState.collectAsStateValue()
    val readerState = readingViewModel.readerStateStateFlow.collectAsStateValue()
    val homeUiState = homeViewModel.homeUiState.collectAsStateValue()

    var isReaderScrollingDown by remember { mutableStateOf(false) }
    var showFullScreenImageViewer by remember { mutableStateOf(false) }

    var currentImageData by remember { mutableStateOf(ImageData()) }

    val isShowToolBar = if (LocalReadingAutoHideToolbar.current.value) {
        readingUiState.articleId != null && !isReaderScrollingDown
    } else {
        true
    }

    val pagingItems = homeUiState.pagingData.collectAsLazyPagingItems().itemSnapshotList

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect {
            it.arguments?.getString("articleId")?.let { articleId ->
                if (readingUiState.articleId != articleId) {
                    readingViewModel.initData(articleId)
                }
            }
        }
    }

    LaunchedEffect(readingUiState.articleId) {
        Log.i("RLog", "ReadPage: ${readingUiState.articleWithFeed}")
        readingUiState.articleId?.let {
            readingViewModel.updateNextArticleId(pagingItems)
            if (readingUiState.isUnread) {
                readingViewModel.markAsRead()
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
                    title = readerState.title,
                    link = readerState.link,
                    onClose = {
                        navController.popBackStack()
                    },
                )



                if (readingUiState.articleId != null) {
                    // Content
                    AnimatedContent(
                        targetState = readerState,
                        transitionSpec = {
                            if (initialState.title != targetState.title)
                                materialSharedAxisY(
                                    initialOffsetY = { (it * 0.1f).toInt() },
                                    targetOffsetY = { (it * -0.1f).toInt() })
                            else {
                                ContentTransform(
                                    targetContentEnter = EnterTransition.None,
                                    initialContentExit = ExitTransition.None, sizeTransform = null
                                )
                            }
                        }, label = ""
                    ) {
                        it.run {
                            val listState = rememberSaveable(
                                inputs = arrayOf(content),
                                saver = LazyListState.Saver
                            ) { LazyListState() }

                            isReaderScrollingDown = listState.isScrollDown()

                            Content(
                                content = content.text ?: "",
                                feedName = feedName,
                                title = title.toString(),
                                author = author,
                                link = link,
                                publishedDate = publishedDate,
                                isLoading = content is ReaderState.Loading,
                                listState = listState,
                                onImageClick = { imgUrl, altText ->
                                    currentImageData = ImageData(imgUrl, altText)
                                    showFullScreenImageViewer = true
                                }
                            )
                        }
                    }
                }
                // Bottom Bar
                if (readingUiState.articleId != null) {
                    BottomBar(
                        isShow = isShowToolBar,
                        isUnread = readingUiState.isUnread,
                        isStarred = readingUiState.isStarred,
                        isNextArticleAvailable = readingUiState.run { !nextArticleId.isNullOrEmpty() && nextArticleId != articleId },
                        isFullContent = readerState.content is ReaderState.FullContent,
                        onUnread = {
                            readingViewModel.updateReadStatus(it)
                        },
                        onStarred = {
                            readingViewModel.updateStarredStatus(it)
                        },
                        onNextArticle = {
                            readingUiState.nextArticleId?.let { readingViewModel.initData(it) }
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
    if (showFullScreenImageViewer) {
        ReaderImageViewer(imageData = currentImageData) { showFullScreenImageViewer = false }
    }
}
