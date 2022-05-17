package me.ash.reader.ui.page.home.feeds.drawer.feed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.base.Dialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast

@Composable
fun ClearFeedDialog(
    modifier: Modifier = Modifier,
    feedName: String,
    viewModel: FeedOptionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState = viewModel.viewState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString = stringResource(R.string.clear_articles_in_feed_toast, feedName)

    Dialog(
        visible = viewState.clearDialogVisible,
        onDismissRequest = {
            viewModel.dispatch(FeedOptionViewAction.HideClearDialog)
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = stringResource(R.string.clear_articles),
            )
        },
        title = {
            Text(text = stringResource(R.string.clear_articles))
        },
        text = {
            Text(text = stringResource(R.string.clear_articles_feed_tips, feedName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.dispatch(FeedOptionViewAction.Clear {
                        viewModel.dispatch(FeedOptionViewAction.HideClearDialog)
                        viewModel.dispatch(FeedOptionViewAction.Hide(scope))
                        context.showToast(toastString)
                    })
                }
            ) {
                Text(
                    text = stringResource(R.string.clear),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.dispatch(FeedOptionViewAction.HideClearDialog)
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )
}