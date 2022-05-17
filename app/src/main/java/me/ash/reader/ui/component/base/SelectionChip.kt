package me.ash.reader.ui.component.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.ui.theme.palette.alwaysLight

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
        modifier = modifier.defaultMinSize(minHeight = 36.dp),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
        ),
        border = border,
        interactionSource = interactionSource,
        enabled = enabled,
        selected = selected,
        selectedIcon = selectedIcon ?: {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp),
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.selected),
                tint = MaterialTheme.colorScheme.onSurface alwaysLight true
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
                color = MaterialTheme.colorScheme.onSurface alwaysLight selected,
            )
        },
    )
}