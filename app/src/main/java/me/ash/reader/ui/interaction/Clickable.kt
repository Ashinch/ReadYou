package me.ash.reader.ui.interaction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role

@Composable
fun Modifier.alphaIndicationClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedAlpha by animateFloatAsState(if (isPressed) .5f else 1f)

    return clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            indication = null,
            interactionSource = interactionSource,
            onClick = onClick,
        )
        .alpha(animatedAlpha)
}
