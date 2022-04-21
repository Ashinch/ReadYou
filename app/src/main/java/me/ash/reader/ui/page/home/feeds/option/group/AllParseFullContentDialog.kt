package me.ash.reader.ui.page.home.feeds.option.group

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AllParseFullContentDialog(
    modifier: Modifier = Modifier,
    groupName: String,
    viewModel: GroupOptionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val allowToastString = stringResource(R.string.all_parse_full_content_toast, groupName)
    val denyToastString = stringResource(R.string.all_deny_parse_full_content_toast, groupName)

    Dialog(
        visible = viewState.allParseFullContentDialogVisible,
        onDismissRequest = {
            viewModel.dispatch(GroupOptionViewAction.HideAllParseFullContentDialog)
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Article,
                contentDescription = stringResource(R.string.parse_full_content),
            )
        },
        title = {
            Text(text = stringResource(R.string.parse_full_content))
        },
        text = {
            Text(text = stringResource(R.string.all_parse_full_content_tip, groupName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.dispatch(GroupOptionViewAction.AllParseFullContent(true) {
                        viewModel.dispatch(GroupOptionViewAction.HideAllParseFullContentDialog)
                        viewModel.dispatch(GroupOptionViewAction.Hide(scope))
                        Toast.makeText(context, allowToastString, Toast.LENGTH_SHORT).show()
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
                    viewModel.dispatch(GroupOptionViewAction.AllParseFullContent(false) {
                        viewModel.dispatch(GroupOptionViewAction.HideAllParseFullContentDialog)
                        viewModel.dispatch(GroupOptionViewAction.Hide(scope))
                        Toast.makeText(context, denyToastString, Toast.LENGTH_SHORT).show()
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