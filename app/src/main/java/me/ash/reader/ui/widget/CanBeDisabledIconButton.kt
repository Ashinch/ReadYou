package me.ash.reader.ui.widget

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CanBeDisabledIconButton(
    modifier: Modifier = Modifier,
    disabled: Boolean,
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit = {},
) {
    IconButton(
        modifier = Modifier.alpha(
            if (disabled) {
                0.7f
            } else {
                1f
            }
        ),
        enabled = !disabled,
        onClick = onClick,
    ) {
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (disabled) MaterialTheme.colorScheme.outline else tint,
        )
    }
}