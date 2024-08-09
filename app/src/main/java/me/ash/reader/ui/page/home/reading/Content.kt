package me.ash.reader.ui.page.home.reading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.LocalReadingRenderer
import me.ash.reader.infrastructure.preference.LocalReadingSubheadUpperCase
import me.ash.reader.infrastructure.preference.ReadingRendererPreference
import me.ash.reader.ui.component.reader.Reader
import me.ash.reader.ui.component.webview.RYWebView
import me.ash.reader.ui.ext.drawVerticalScrollbar
import me.ash.reader.ui.ext.extractDomain
import me.ash.reader.ui.ext.openURL
import java.util.Date

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
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val subheadUpperCase = LocalReadingSubheadUpperCase.current
    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current
    val renderer = LocalReadingRenderer.current

    if (isLoading) {
        Column {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    } else {

        SelectionContainer {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .drawVerticalScrollbar(listState),
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
                    Spacer(modifier = Modifier.height(22.dp))
                }

                when (renderer) {
                    ReadingRendererPreference.WebView -> {
                        item {
                            RYWebView(
                                content = content,
                                refererDomain = link.extractDomain(),
                                onImageClick = onImageClick,
                            )
                        }
                    }

                    ReadingRendererPreference.NativeComponent -> {
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
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(128.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}
