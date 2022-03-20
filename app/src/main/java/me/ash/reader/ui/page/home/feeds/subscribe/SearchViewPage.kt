package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.delay
import me.ash.reader.R

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SearchViewPage(
    pagerState: PagerState,
    inputContent: String = "",
    errorMessage: String = "",
    onValueChange: (String) -> Unit = {},
    onKeyboardAction: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)  // ???
        focusRequester.requestFocus()
    }

    Column {
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                textColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            ),
            value = inputContent,
            onValueChange = {
                if (pagerState.currentPage == 0) onValueChange(it)
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.feed_or_site_url),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            },
            isError = errorMessage.isNotEmpty(),
            singleLine = true,
            trailingIcon = {
                if (inputContent.isNotEmpty()) {
                    IconButton(onClick = {
                        onValueChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    IconButton(onClick = {
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = stringResource(R.string.paste),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    onKeyboardAction()
                }
            )
        )
        if (errorMessage.isNotEmpty()) {
            SelectionContainer {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}