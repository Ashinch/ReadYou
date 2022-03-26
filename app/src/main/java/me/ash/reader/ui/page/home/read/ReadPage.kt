package me.ash.reader.ui.page.home.read

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import me.ash.reader.R
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.widget.WebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadPage(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ReadViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    readViewModel: ReadViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewState = viewModel.viewState.collectAsStateValue()
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url(
            "https://assets5.lottiefiles.com/packages/lf20_9tvcldy3.json"
        )
    )

    LaunchedEffect(viewModel.viewState) {
        viewModel.viewState.collect {
            if (it.articleWithFeed != null) {
//                if (it.articleWithFeed.article.isUnread) {
//                    viewModel.dispatch(ReadViewAction.MarkUnread(false))
//                }
                if (it.articleWithFeed.feed.isFullContent) {
                    viewModel.dispatch(ReadViewAction.RenderFullContent)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            SmallTopAppBar(
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
        },
        content = {
            if (viewState.articleWithFeed == null) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier
                        .padding(50.dp)
                        .alpha(0.6f),
                    isPlaying = true,
                    restartOnPlay = true,
                    iterations = Int.MAX_VALUE
                )
            } else {
                LazyColumn(
                    state = viewState.listState,
                ) {
                    val article = viewState.articleWithFeed.article
                    val feed = viewState.articleWithFeed.feed

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
                }
            }
        }
    )
}
