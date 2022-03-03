package me.ash.reader.ui.page.home.read

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.flow.collect
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.data.article.Article
import me.ash.reader.data.feed.Feed
import me.ash.reader.ui.util.collectAsStateValue
import me.ash.reader.ui.util.paddingFixedHorizontal
import me.ash.reader.ui.util.roundClick
import me.ash.reader.ui.widget.WebView


@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun ReadPage(
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
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {

            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { btnBackOnClickListener() }) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )

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
                SelectionContainer {
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
}

@Composable
private fun Header(
    context: Context,
    article: Article,
    feed: Feed
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .roundClick {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                )
            }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = article.date.toString(DateTimeExt.YYYY_MM_DD_HH_MM, true),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.title,
                fontSize = 27.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            article.author?.let {
                Text(
                    text = article.author,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = feed.name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}