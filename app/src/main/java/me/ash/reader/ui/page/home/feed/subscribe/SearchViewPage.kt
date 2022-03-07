package me.ash.reader.ui.page.home.feed.subscribe

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SearchViewPage(
    inputContent: String = "",
    onValueChange: (String) -> Unit = {},
    onKeyboardAction: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)  // ???
        focusRequester.requestFocus()
    }

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
            onValueChange(it)
        },
        placeholder = {
            Text(
                text = "订阅源或站点链接",
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
        },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = {
//                focusRequester.requestFocus()
            }) {
                Icon(
                    imageVector = Icons.Rounded.ContentPaste,
                    contentDescription = "Paste",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        keyboardActions = KeyboardActions(
            onDone = {
                onKeyboardAction()
            }
        )
    )
    Spacer(modifier = Modifier.height(10.dp))
}