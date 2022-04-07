package me.ash.reader.ui.page.home.read

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.component.WebView
import me.ash.reader.ui.ext.collectAsStateValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadPage(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    readViewModel: ReadViewModel = hiltViewModel(),
    onScrollToPage: (targetPage: Int, callback: () -> Unit) -> Unit = { _, _ -> },
) {
    val viewState = readViewModel.viewState.collectAsStateValue()
    var isScrollDown by remember { mutableStateOf(false) }

    if (viewState.listState.isScrollInProgress) {
        LaunchedEffect(Unit) {
            Log.i("RLog", "scroll: start")
        }

        val preItemIndex by remember { mutableStateOf(viewState.listState.firstVisibleItemIndex) }
        val preScrollStartOffset by remember { mutableStateOf(viewState.listState.firstVisibleItemScrollOffset) }
        val currentItemIndex = viewState.listState.firstVisibleItemIndex
        val currentScrollStartOffset = viewState.listState.firstVisibleItemScrollOffset

        isScrollDown = when {
            currentItemIndex > preItemIndex -> true
            currentItemIndex < preItemIndex -> false
            else -> currentScrollStartOffset > preScrollStartOffset
        }

        DisposableEffect(Unit) {
            onDispose {
                Log.i("RLog", "scroll: end")
            }
        }
    }

    LaunchedEffect(viewState.articleWithFeed?.article?.id) {
        isScrollDown = false
        viewState.articleWithFeed?.let {
            if (it.article.isUnread) {
                readViewModel.dispatch(ReadViewAction.MarkUnread(false))
            }
            if (it.feed.isFullContent) {
                readViewModel.dispatch(ReadViewAction.RenderFullContent)
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {},
        content = {
            Box(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    TopBar(
                        isShow = viewState.articleWithFeed == null || !isScrollDown,
                        isShowActions = viewState.articleWithFeed != null,
                        onScrollToPage = onScrollToPage,
                        onClearArticle = {
                            readViewModel.dispatch(ReadViewAction.ClearArticle)
                        }
                    )
                }
                Content(
                    content = viewState.content ?: "",
                    articleWithFeed = viewState.articleWithFeed,
                    LazyListState = viewState.listState,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    BottomBar(
                        isShow = viewState.articleWithFeed != null && !isScrollDown,
                        articleWithFeed = viewState.articleWithFeed,
                        unreadOnClick = {
                            readViewModel.dispatch(ReadViewAction.MarkUnread(it))
                        },
                        starredOnClick = {
                            readViewModel.dispatch(ReadViewAction.MarkStarred(it))
                        },
                        fullContentOnClick = { afterIsFullContent ->
                            if (afterIsFullContent) readViewModel.dispatch(ReadViewAction.RenderFullContent)
                            else readViewModel.dispatch(ReadViewAction.RenderDescriptionContent)
                        },
                    )
                }
            }
        },
        bottomBar = {}
    )
}

@Composable
private fun TopBar(
    isShow: Boolean,
    isShowActions: Boolean = false,
    onScrollToPage: (targetPage: Int, callback: () -> Unit) -> Unit = { _, _ -> },
    onClearArticle: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = isShow,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        SmallTopAppBar(
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            title = {},
            navigationIcon = {
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurface
                ) {
                    onScrollToPage(1) {
                        onClearArticle()
                    }
                }
            },
            actions = {
                if (isShowActions) {
                    FeedbackIconButton(
                        modifier = Modifier.size(22.dp),
                        imageVector = Icons.Outlined.Headphones,
                        contentDescription = stringResource(R.string.mark_all_as_read),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                    }
                    FeedbackIconButton(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.search),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                    }
                }
            }
        )
    }
}

@Composable
private fun Content(
    content: String,
    articleWithFeed: ArticleWithFeed?,
    LazyListState: LazyListState = rememberLazyListState(),
) {
    Column {
        if (articleWithFeed == null) {
            Spacer(modifier = Modifier.height(64.dp))
//            LottieAnimation(
//                modifier = Modifier
//                    .alpha(0.7f)
//                    .padding(80.dp),
//                url = "https://assets8.lottiefiles.com/packages/lf20_jm7mv1ib.json",
//            )
        } else {
            LazyColumn(
                state = LazyListState,
            ) {
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                    ) {
                        Header(articleWithFeed)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(22.dp))
                    Crossfade(targetState = content) { content ->
                        WebView(
                            content = content
                        )
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    isShow: Boolean,
    articleWithFeed: ArticleWithFeed?,
    unreadOnClick: (afterIsUnread: Boolean) -> Unit = {},
    starredOnClick: (afterIsStarred: Boolean) -> Unit = {},
    fullContentOnClick: (afterIsFullContent: Boolean) -> Unit = {},
) {
    articleWithFeed?.let {
        AnimatedVisibility(
            visible = isShow,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ReadBar(
                disabled = false,
                isUnread = articleWithFeed.article.isUnread,
                isStarred = articleWithFeed.article.isStarred,
                isFullContent = articleWithFeed.feed.isFullContent,
                unreadOnClick = unreadOnClick,
                starredOnClick = starredOnClick,
                fullContentOnClick = fullContentOnClick,
            )
        }
    }
}