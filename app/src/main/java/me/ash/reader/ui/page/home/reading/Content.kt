package me.ash.reader.ui.page.home.reading

import RYExtensibleVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.component.reader.Reader
import me.ash.reader.ui.ext.drawVerticalScrollbar
import java.util.*

@Composable
fun Content(
    content: String,
    feedName: String,
    title: String,
    author: String? = null,
    link: String? = null,
    publishedDate: Date,
    listState: LazyListState,
    isLoading: Boolean,
    isShowToolBar: Boolean,
) {
    val context = LocalContext.current

    SelectionContainer {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .run {
                    if (isShowToolBar) {
                        navigationBarsPadding()
                    } else {
                        this
                    }
                }
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
                        Header(
                            feedName = feedName,
                            title = title,
                            author = author,
                            link = link,
                            publishedDate = publishedDate,
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(22.dp))
                RYExtensibleVisibility(visible = isLoading) {
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
                }
            }
            if (!isLoading) {
                Reader(
                    context = context,
                    link = link ?: "",
                    content = content
                )
            }
            item {
                Spacer(modifier = Modifier.height(128.dp))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}
