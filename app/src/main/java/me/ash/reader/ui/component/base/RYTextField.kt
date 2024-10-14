package me.ash.reader.ui.component.base

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.delay
import me.ash.reader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RYTextField(
    readOnly: Boolean,
    value: String,
    label: String = "",
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)  // ???
        focusRequester.requestFocus()
    }

    TextField(
        modifier = Modifier.focusRequester(focusRequester),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        enabled = !readOnly,
        value = value,
        label = if (label.isEmpty()) null else {
            { Text(label) }
        },
        onValueChange = {
            if (!readOnly) onValueChange(it)
        },
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else visualTransformation,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        isError = errorMessage.isNotEmpty(),
        singleLine = singleLine,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = {
                    if (isPassword) {
                        showPassword = !showPassword
                    } else if (!readOnly) {
                        onValueChange("")
                    }
                }) {
                    Icon(
                        imageVector = if (isPassword) {
                            if (showPassword) Icons.Rounded.Visibility
                            else Icons.Rounded.VisibilityOff
                        } else Icons.Rounded.Close,
                        contentDescription = if (isPassword) stringResource(R.string.password) else stringResource(R.string.clear),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    )
                }
            } else {
                IconButton(onClick = {
                    onValueChange(clipboardManager.getText()?.text ?: "")
                }) {
                    Icon(
                        imageVector = Icons.Rounded.ContentPaste,
                        contentDescription = stringResource(R.string.paste),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}
