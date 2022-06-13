package me.ash.reader.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.ash.reader.R
import me.ash.reader.ui.component.base.TextFieldDialog

@Composable
fun RenameDialog(
    visible: Boolean = false,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    TextFieldDialog(
        visible = visible,
        title = stringResource(R.string.rename),
        icon = Icons.Outlined.Edit,
        value = value,
        placeholder = stringResource(R.string.name),
        onValueChange = onValueChange,
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm
    )
}
