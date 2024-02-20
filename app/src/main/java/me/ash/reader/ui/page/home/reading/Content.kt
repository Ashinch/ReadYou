package me.ash.reader.ui.page.home.reading

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.LocalReadingSubheadUpperCase
import me.ash.reader.ui.component.reader.Reader
import me.ash.reader.ui.ext.drawVerticalScrollbar
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.ext.pagerAnimate
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
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val subheadUpperCase = LocalReadingSubheadUpperCase.current
    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current

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
    }
}
