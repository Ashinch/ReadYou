package me.ash.reader.ui.widget

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectionChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = CircleShape,
    selectedIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = "Check",
            modifier = Modifier.size(20.dp)
        )
    },
    content: @Composable RowScope.() -> Unit
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
        content = content,
    )
}