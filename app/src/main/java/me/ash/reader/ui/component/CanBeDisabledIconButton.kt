package me.ash.reader.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CanBeDisabledIconButton(
    modifier: Modifier = Modifier,
    disabled: Boolean,
    imageVector: ImageVector,
    size: Dp = 24.dp,
    contentDescription: String?,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit = {},
) {
    IconButton(
        modifier = modifier.alpha(
            if (disabled) {
                0.5f
            } else {
                1f
            }
        ),
        enabled = !disabled,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(size),
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (disabled) MaterialTheme.colorScheme.outline else tint,
        )
    }
}