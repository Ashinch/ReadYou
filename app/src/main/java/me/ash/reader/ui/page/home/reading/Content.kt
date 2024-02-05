package me.ash.reader.ui.page.home.reading

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.LocalReadingSubheadUpperCase
import me.ash.reader.ui.component.reader.Reader
import me.ash.reader.ui.ext.drawVerticalScrollbar
import me.ash.reader.ui.ext.openURL
import java.util.*
import kotlin.math.abs

@Composable
fun Content(
    modifier: Modifier = Modifier,
    content: String,
    feedName: String,
    title: String,
    author: String? = null,
    link: String? = null,
    publishedDate: Date,
    listState: LazyListState,
    isLoading: Boolean,
    pullToLoadState: PullToLoadState,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val subheadUpperCase = LocalReadingSubheadUpperCase.current
    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column {
                Spacer(modifier = Modifier.height(22.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            SelectionContainer {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .drawVerticalScrollbar(listState)
                        .offset(x = 0.dp, y = (pullToLoadState.offsetFraction * 100).dp),
                    state = listState,
                ) {
                    item {
                        // Top bar height
                        Spacer(modifier = Modifier.height(64.dp))
                        // padding
                        Spacer(modifier = Modifier.height(22.dp))
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                        ) {
                            DisableSelection {
                                Metadata(
                                    feedName = feedName,
                                    title = title,
                                    author = author,
                                    link = link,
                                    publishedDate = publishedDate,
                                )
                            }
                        }
                    }
                    Reader(
                        context = context,
                        subheadUpperCase = subheadUpperCase.value,
                        link = link ?: "",
                        content = content,
                        onImageClick = onImageClick,
                        onLinkClick = {
                            context.openURL(it, openLink, openLinkSpecificBrowser)
                        }
                    )

                    item {
                        Spacer(modifier = Modifier.height(128.dp))
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
            pullToLoadState.status.run {
                val fraction = pullToLoadState.offsetFraction

                val imageVector = when (this) {
                    PullToLoadState.Status.PulledDown -> Icons.Outlined.KeyboardArrowUp
                    PullToLoadState.Status.PulledUp -> Icons.Outlined.KeyboardArrowDown
                    else -> null
                }

                val alignment = if (fraction < 0f) {
                    Alignment.BottomCenter
                } else {
                    Alignment.TopCenter
                }
                if (this != PullToLoadState.Status.Idle)
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(alignment)
                            .padding(vertical = 72.dp)
                            .offset(y = (fraction * 48).dp)
                    ) {
                        AnimatedContent(
                            targetState = imageVector,
                            label = ""
                        ) {
                            it?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 4.dp,
                                            vertical = (abs(fraction) * 2).dp
                                        )
                                        .size(32.dp)
                                )
                            } ?: Spacer(
                                modifier = Modifier.size(
                                    width = 36.dp,
                                    height = (abs(fraction) * 8).dp
                                )
                            )
                        }

                    }

            }

        }
    }
}
