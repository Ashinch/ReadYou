package me.ash.reader.ui.component.base

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Deprecated("Use overloads with state instead")
@Composable
fun ClipboardTextField(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    value: String = "",
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit = {},
    placeholder: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    imeAction: ImeAction = ImeAction.Done,
    focusManager: FocusManager? = null,
    onConfirm: (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(10.dp))
        RYTextField2(
            readOnly = readOnly,
            value = value,
            singleLine = singleLine,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isPassword = isPassword,
            errorMessage = errorText,
            onKeyboardAction = if (imeAction != ImeAction.Default || imeAction != ImeAction.None) {
                KeyboardActionHandler { action(focusManager, onConfirm, value) }
            } else null,
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction
            ),
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}


@Composable
fun ClipboardTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    placeholder: String = "",
    isPassword: Boolean = false,
    errorText: String = "",
    imeAction: ImeAction = ImeAction.Done,
    focusManager: FocusManager? = null,
    onConfirm: (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(10.dp))
        RYTextField2(
            readOnly = readOnly,
            state = state,
            singleLine = singleLine,
            placeholder = placeholder,
            isPassword = isPassword,
            errorMessage = errorText,
            onKeyboardAction = if (imeAction != ImeAction.Default || imeAction != ImeAction.None) {
                KeyboardActionHandler { action(focusManager, onConfirm, state.text.toString()) }
            } else null,
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction
            ),
        )
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
