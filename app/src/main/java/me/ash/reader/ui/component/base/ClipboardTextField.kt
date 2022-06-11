package me.ash.reader.ui.component.base

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun ClipboardTextField(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    placeholder: String = "",
    errorText: String = "",
    imeAction: ImeAction = ImeAction.Done,
    focusManager: FocusManager? = null,
    onConfirm: (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(10.dp))
        RYTextField(
            readOnly = readOnly,
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            errorMessage = errorText,
            keyboardActions = KeyboardActions(
                onDone = if (imeAction == ImeAction.Done)
                    action(focusManager, onConfirm, value) else null,
                onGo = if (imeAction == ImeAction.Go)
                    action(focusManager, onConfirm, value) else null,
                onNext = if (imeAction == ImeAction.Next)
                    action(focusManager, onConfirm, value) else null,
                onPrevious = if (imeAction == ImeAction.Previous)
                    action(focusManager, onConfirm, value) else null,
                onSearch = if (imeAction == ImeAction.Search)
                    action(focusManager, onConfirm, value) else null,
                onSend = if (imeAction == ImeAction.Send)
                    action(focusManager, onConfirm, value) else null,
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction
            ),
        )
        if (errorText.isNotEmpty()) {
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

private fun action(
    focusManager: FocusManager?,
    onConfirm: (String) -> Unit,
    value: String,
): KeyboardActionScope.() -> Unit = {
    focusManager?.clearFocus()
    onConfirm(value)
}
