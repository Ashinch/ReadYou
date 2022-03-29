package me.ash.reader.ui.page.home.read

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import me.ash.reader.R
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.widget.LottieAnimation
import me.ash.reader.ui.widget.WebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadPage(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    readViewModel: ReadViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

    LaunchedEffect(readViewModel.viewState) {
        readViewModel.viewState.collect {
            if (it.articleWithFeed != null) {
                if (it.articleWithFeed.article.isUnread) {
                    readViewModel.dispatch(ReadViewAction.MarkUnread(false))
                }
                if (it.articleWithFeed.feed.isFullContent) {
                    readViewModel.dispatch(ReadViewAction.RenderFullContent)
                }
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
                    TopBar(isScrollDown, homeViewModel, scope, readViewModel, viewState)
                }
                Content(viewState, viewState.articleWithFeed, context)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    BottomBar(isScrollDown, viewState.articleWithFeed, readViewModel)
                }
            }
        },
        bottomBar = {}
    )
}

@Composable
private fun TopBar(
    isScrollDown: Boolean,
    homeViewModel: HomeViewModel,
    scope: CoroutineScope,
    readViewModel: ReadViewModel,
    viewState: ReadViewState
) {
    AnimatedVisibility(
        visible = !isScrollDown,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        SmallTopAppBar(
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            title = {},
            navigationIcon = {
                IconButton(onClick = {
                    homeViewModel.dispatch(
                        HomeViewAction.ScrollToPage(
                            scope = scope,
                            targetPage = 1,
                            callback = {
                                readViewModel.dispatch(ReadViewAction.ClearArticle)
                            }
                        )
                    )
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                viewState.articleWithFeed?.let {
                    IconButton(onClick = {}) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Outlined.Headphones,
                            contentDescription = stringResource(R.string.mark_all_as_read),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun BottomBar(
    isScrollDown: Boolean,
    articleWithFeed: ArticleWithFeed?,
    readViewModel: ReadViewModel
) {
    articleWithFeed?.let {
        AnimatedVisibility(
            visible = !isScrollDown,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ReadBar(
                disabled = false,
                isUnread = articleWithFeed.article.isUnread,
                isStarred = articleWithFeed.article.isStarred,
                isFullContent = articleWithFeed.feed.isFullContent,
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
}

@Composable
private fun Content(
    viewState: ReadViewState,
    articleWithFeed: ArticleWithFeed?,
    context: Context
) {
    Column {
        if (articleWithFeed == null) {
            Spacer(modifier = Modifier.height(64.dp))
            LottieAnimation(
                modifier = Modifier.alpha(0.7f).padding(80.dp),
                url = "https://assets8.lottiefiles.com/packages/lf20_jm7mv1ib.json",
            )
        } else {
            LazyColumn(
                state = viewState.listState,
            ) {
                val article = articleWithFeed.article
                val feed = articleWithFeed.feed

                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                    ) {
                        Header(context, article, feed)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(22.dp))
                    Crossfade(targetState = viewState.content) { content ->
                        WebView(
                            content = content ?: "",
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

