package me.ash.reader.ui.widget

import androidx.compose.animation.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable

@Composable
fun Dialog(
    visible: Boolean,
    onDismissRequest: () -> Unit = {},
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = icon,
            title = title,
            text = text,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
        )
    }
}