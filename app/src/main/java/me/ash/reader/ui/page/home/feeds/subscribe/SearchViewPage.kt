package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.delay
import me.ash.reader.R

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SearchViewPage(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    readOnly: Boolean = false,
    inputLink: String = "",
    errorMessage: String = "",
    onLinkValueChange: (String) -> Unit = {},
    onKeyboardAction: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)  // ???
        focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                textColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            ),
            enabled = !readOnly,
            value = inputLink,
            onValueChange = {
                if (!readOnly) onLinkValueChange(it)
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
                if (inputLink.isNotEmpty()) {
                    IconButton(onClick = {
                        if (!readOnly) onLinkValueChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    IconButton(onClick = {
                        onLinkValueChange(clipboardManager.getText()?.text ?: "")
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
                onSearch = {
                    focusManager.clearFocus()
                    onKeyboardAction()
                }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
        )
        if (errorMessage.isNotEmpty()) {
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}