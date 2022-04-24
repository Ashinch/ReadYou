package me.ash.reader.ui.page.home.feeds.option.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.R
import me.ash.reader.ui.component.Dialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AllAllowNotificationDialog(
    modifier: Modifier = Modifier,
    groupName: String,
    viewModel: GroupOptionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val allowToastString = stringResource(R.string.all_allow_notification_toast, groupName)
    val denyToastString = stringResource(R.string.all_deny_notification_toast, groupName)

    Dialog(
        visible = viewState.allAllowNotificationDialogVisible,
        onDismissRequest = {
            viewModel.dispatch(GroupOptionViewAction.HideAllAllowNotificationDialog)
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
            Text(text = stringResource(R.string.all_allow_notification_tip, groupName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.dispatch(GroupOptionViewAction.AllAllowNotification(true) {
                        viewModel.dispatch(GroupOptionViewAction.HideAllAllowNotificationDialog)
                        viewModel.dispatch(GroupOptionViewAction.Hide(scope))
                        context.showToast(allowToastString)
                    })
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
                    viewModel.dispatch(GroupOptionViewAction.AllAllowNotification(false) {
                        viewModel.dispatch(GroupOptionViewAction.HideAllAllowNotificationDialog)
                        viewModel.dispatch(GroupOptionViewAction.Hide(scope))
                        context.showToast(denyToastString)
                    })
                }
            ) {
                Text(
                    text = stringResource(R.string.deny),
                )
            }
        },
    )
}