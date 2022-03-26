package me.ash.reader.ui.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.ash.reader.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectionChip(
    content: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = CircleShape,
    border: BorderStroke? = null,
    selectedIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    FilterChip(
        modifier = modifier,
        colors = ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = border,
        interactionSource = interactionSource,
        enabled = enabled,
        selected = selected,
        selectedIcon = selectedIcon ?: {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.selected),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp),
            )
        },
        shape = shape,
        onClick = {
            focusManager.clearFocus()
            onClick()
        },
        content = {
            Text(
                modifier = modifier.padding(
                    start = if (selected) 0.dp else 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                ),
                text = content,
                style = MaterialTheme.typography.titleSmall,
            )
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectionEditorChip(
    modifier: Modifier = Modifier,
    content: String,
    onValueChange: (String) -> Unit = {},
    selected: Boolean,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = CircleShape,
    selectedIcon: @Composable (() -> Unit)? = null,
    onKeyboardAction: () -> Unit = {},
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val placeholder = stringResource(R.string.add_to_group)

    FilterChip(
        modifier = modifier.defaultMinSize(minHeight = 36.dp),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        interactionSource = interactionSource,
        enabled = enabled,
        selected = selected,
        selectedIcon = selectedIcon ?: {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.selected),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp),
            )
        },
        shape = shape,
        onClick = onClick,
        content = {
            BasicTextField(
                modifier = Modifier
                    .padding(
                        start = if (selected) 0.dp else 8.dp,
                        top = 8.dp,
                        end = if (content.isEmpty()) 0.dp else 8.dp,
                        bottom = 8.dp
                    )
                    .onFocusChanged {
                        if (it.isFocused) {
                            onClick()
                        } else {
                            focusManager.clearFocus()
                        }
                    },
                value = content,
                onValueChange = { onValueChange(it) },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    color =  MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                decorationBox = { innerTextField ->
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (content.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    innerTextField()
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onKeyboardAction()
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
            )
        },
    )
}