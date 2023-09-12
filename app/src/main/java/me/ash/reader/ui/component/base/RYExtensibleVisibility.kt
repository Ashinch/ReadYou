package me.ash.reader.ui.component.base

import androidx.compose.animation.*
import androidx.compose.runtime.Composable

@Composable
fun RYExtensibleVisibility(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        content = content,
    )
}
