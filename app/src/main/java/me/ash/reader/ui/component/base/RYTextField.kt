package me.ash.reader.ui.component.base

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ash.reader.R
import timber.log.Timber

@Deprecated("Use overloads with BTF2 instead")
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
    onKeyboardAction: KeyboardActionHandler? = null,
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
                        contentDescription = if (isPassword) stringResource(R.string.password) else stringResource(
                            R.string.clear
                        ),
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
    )
}


@Composable
fun RYTextField2(
    value: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    label: String = "",
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    placeholder: String = "",
    errorMessage: String = "",
    autoFocus: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    val focusRequester = remember { FocusRequester() }

    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(100)  // ???
            focusRequester.requestFocus()
        }
    }

    val state = rememberTextFieldState().also { it.edit { value } }
    val textFlow = snapshotFlow { state.text }

    LaunchedEffect(textFlow) {
        textFlow.collect { onValueChange(it.toString()) }
    }

    if (isPassword) {
        var showPassword by remember { mutableStateOf(false) }

        SecureTextField(
            state = state, colors = textFieldColors(),
            label = if (label.isEmpty()) null else {
                { Text(label) }
            },
            modifier = modifier.focusRequester(focusRequester),
            trailingIcon = {
                PasswordVisibilityButton(
                    showPassword = showPassword,
                    onValueChange = { showPassword = it })
            },
            textObfuscationMode = if (showPassword) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
            placeholder = {
                Text(text = placeholder)
            },
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            isError = errorMessage.isNotEmpty(),
            supportingText = if (errorMessage.isNotEmpty()) {
                {
                    Text(errorMessage)
                }
            } else null
        )
    } else {
        TextField(
            state = state, modifier = modifier.focusRequester(focusRequester),
            colors = textFieldColors(),
            label = if (label.isEmpty()) null else {
                { Text(label) }
            },
            lineLimits = if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.MultiLine(),
            trailingIcon = {
                if (value.isNotEmpty()) {
                    ClearButton(state)
                } else {
                    PasteButton(state)
                }
            }, isError = errorMessage.isNotEmpty(),
            placeholder = {
                Text(text = placeholder)
            },
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            readOnly = readOnly,
            supportingText = if (errorMessage.isNotEmpty()) {
                {
                    Text(errorMessage)
                }
            } else null
        )
    }

}

@Composable
private fun textFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
private fun PasteButton(state: TextFieldState, modifier: Modifier = Modifier) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    IconButton(onClick = {
        scope.launch {
            val clipboardText =
                clipboardManager.getClipEntry()?.clipData?.getItemAt(0)?.text
            if (!clipboardText.isNullOrBlank()) {
                state.edit { clipboardText }
            }
        }
    }, modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.ContentPaste,
            contentDescription = stringResource(R.string.paste),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ClearButton(state: TextFieldState, modifier: Modifier = Modifier) {
    IconButton(onClick = { state.clearText() }, modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(
                R.string.clear
            ),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PasswordVisibilityButton(
    showPassword: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = {
        onValueChange(!showPassword)
    }, modifier = modifier) {
        Icon(
            imageVector =
                if (showPassword) Icons.Rounded.Visibility
                else Icons.Rounded.VisibilityOff,
            contentDescription = if (showPassword) "Show password" else "Hide password",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}