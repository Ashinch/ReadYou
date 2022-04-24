package me.ash.reader.ui.page.home.feeds.option.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DriveFileMove
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
fun AllMoveToGroupDialog(
    modifier: Modifier = Modifier,
    groupName: String,
    viewModel: GroupOptionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString =
        stringResource(R.string.all_move_to_group_toast, viewState.targetGroup?.name ?: "")

    Dialog(
        visible = viewState.allMoveToGroupDialogVisible,
        onDismissRequest = {
            viewModel.dispatch(GroupOptionViewAction.HideAllMoveToGroupDialog)
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DriveFileMove,
                contentDescription = stringResource(R.string.move_to_group),
            )
        },
        title = {
            Text(text = stringResource(R.string.move_to_group))
        },
        text = {
            Text(
                text = stringResource(
                    R.string.all_move_to_group_tip,
                    groupName,
                    viewState.targetGroup?.name ?: "",
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.dispatch(GroupOptionViewAction.AllMoveToGroup {
                        viewModel.dispatch(GroupOptionViewAction.HideAllMoveToGroupDialog)
                        viewModel.dispatch(GroupOptionViewAction.Hide(scope))
                        context.showToast(toastString)
                    })
                }
            ) {
                Text(
                    text = stringResource(R.string.confirm),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.dispatch(GroupOptionViewAction.HideAllMoveToGroupDialog)
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )
}