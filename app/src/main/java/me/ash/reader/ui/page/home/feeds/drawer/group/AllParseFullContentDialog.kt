package me.ash.reader.ui.page.home.feeds.drawer.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Article
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
fun AllParseFullContentDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val allowToastString = stringResource(R.string.all_parse_full_content_toast, groupName)
    val denyToastString = stringResource(R.string.all_deny_parse_full_content_toast, groupName)

    RYDialog(
        visible = groupOptionUiState.allParseFullContentDialogVisible,
        onDismissRequest = {
            groupOptionViewModel.hideAllParseFullContentDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Article,
                contentDescription = stringResource(R.string.parse_full_content),
            )
        },
        title = {
            Text(text = stringResource(R.string.parse_full_content))
        },
        text = {
            Text(text = stringResource(R.string.all_parse_full_content_tips, groupName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.allParseFullContent(true) {
                        groupOptionViewModel.hideAllParseFullContentDialog()
                        groupOptionViewModel.hideDrawer(scope)
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
                    groupOptionViewModel.allParseFullContent(false) {
                        groupOptionViewModel.hideAllParseFullContentDialog()
                        groupOptionViewModel.hideDrawer(scope)
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