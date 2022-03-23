package me.ash.reader.ui.widget

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
    selectedIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.selected),
            modifier = Modifier
                .padding(start = 8.dp)
                .size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    },
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    FilterChip(
        modifier = modifier,
        colors = ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.outline,
            leadingIconColor = MaterialTheme.colorScheme.onSurface,
            disabledBackgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onSurface,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSurface
        ),
        interactionSource = interactionSource,
        enabled = enabled,
        selected = selected,
        selectedIcon = selectedIcon,
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
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outline
                },
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
    selectedIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.selected),
            modifier = Modifier
                .padding(start = 8.dp)
                .size(20.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
    },
    onKeyboardAction: () -> Unit = {},
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val placeholder = stringResource(R.string.add_to_group)

    FilterChip(
        colors = ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledBackgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        interactionSource = interactionSource,
        enabled = enabled,
        selected = selected,
        selectedIcon = selectedIcon,
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
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondaryContainer),
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ),
                decorationBox = { innerTextField ->
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (content.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
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