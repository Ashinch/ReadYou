package me.ash.reader.ui.page.home.read

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.flow.collect
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.extension.paddingFixedHorizontal
import me.ash.reader.ui.widget.WebView

@Composable
fun ReadPage(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ReadViewModel = hiltViewModel(),
    btnBackOnClickListener: () -> Unit,
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()

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

    Box {
        Column(
            modifier.fillMaxSize()
        ) {
            ReadPageTopBar(btnBackOnClickListener)

            val composition by rememberLottieComposition(
                LottieCompositionSpec.Url(
                    "https://assets5.lottiefiles.com/packages/lf20_9tvcldy3.json"
                )
            )

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
            }
            AnimatedVisibility(
                modifier = modifier.fillMaxSize(),
                visible = viewState.articleWithFeed != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                if (viewState.articleWithFeed == null) return@AnimatedVisibility
                LazyColumn(
                    state = viewState.listState,
                    modifier = Modifier
                        .weight(1f),
                ) {
                    val article = viewState.articleWithFeed.article
                    val feed = viewState.articleWithFeed.feed

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .paddingFixedHorizontal()
                        ) {
                            Header(context, article, feed)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        WebView(
                            content = viewState.content ?: "",
                        )
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }
    }
}
