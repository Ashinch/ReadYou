package me.ash.reader.ui.page.home.feeds.drawer.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast

@Composable
fun AllAllowNotificationDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val allowToastString = stringResource(R.string.all_allow_notification_toast, groupName)
    val denyToastString = stringResource(R.string.all_deny_notification_toast, groupName)

    RYDialog(
        visible = groupOptionUiState.allAllowNotificationDialogVisible,
        onDismissRequest = {
            groupOptionViewModel.hideAllAllowNotificationDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = stringResource(R.string.allow_notification),
            )
        },
        title = {
            Text(text = stringResource(R.string.allow_notification))
        },
        text = {
            Text(text = stringResource(R.string.all_allow_notification_tips, groupName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.allAllowNotification(true) {
                        groupOptionViewModel.hideAllAllowNotificationDialog()
                        onConfirm()
                        context.showToast(allowToastString)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.allow),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.allAllowNotification(false) {
                        groupOptionViewModel.hideAllAllowNotificationDialog()
                        onConfirm()
                        context.showToast(denyToastString)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.deny),
                )
            }
        },
    )
}