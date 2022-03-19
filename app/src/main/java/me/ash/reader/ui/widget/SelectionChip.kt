package me.ash.reader.ui.widget

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

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
            imageVector = Icons.Rounded.Check,
            contentDescription = "Check",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(18.dp)
        )
    },
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier,
        colors = ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
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
        onClick = onClick,
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
    content: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = CircleShape,
    selectedIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "Check",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(16.dp)
        )
    },
    onKeyboardAction: () -> Unit = {},
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier,
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
                        end = 8.dp,
                        bottom = 8.dp
                    )
                    .width(56.dp),
                value = content,
                onValueChange = {},
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        onKeyboardAction()
                    }
                )
            )
        },
    )
}