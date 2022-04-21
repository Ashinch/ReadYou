package me.ash.reader.ui.page.home.feeds.option.feed

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.component.BottomDrawer
import me.ash.reader.ui.component.TextFieldDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.roundClick
import me.ash.reader.ui.page.home.feeds.subscribe.ResultView

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedOptionDrawer(
    modifier: Modifier = Modifier,
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewState = feedOptionViewModel.viewState.collectAsStateValue()
    val feed = viewState.feed
    val toastString = stringResource(R.string.rename_toast, viewState.newName)

    BackHandler(viewState.drawerState.isVisible) {
        scope.launch {
            viewState.drawerState.hide()
        }
    }

    BottomDrawer(
        drawerState = viewState.drawerState,
        sheetContent = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.roundClick { },
                        imageVector = Icons.Rounded.RssFeed,
                        contentDescription = feed?.name ?: stringResource(R.string.unknown),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.roundClick {
                            feedOptionViewModel.dispatch(FeedOptionViewAction.ShowRenameDialog)
                        },
                        text = feed?.name ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ResultView(
                    link = feed?.url ?: stringResource(R.string.unknown),
                    groups = viewState.groups,
                    selectedAllowNotificationPreset = viewState.feed?.isNotification ?: false,
                    selectedParseFullContentPreset = viewState.feed?.isFullContent ?: false,
                    isMoveToGroup = true,
                    showUnsubscribe = true,
                    selectedGroupId = viewState.feed?.groupId ?: "",
                    allowNotificationPresetOnClick = {
                        feedOptionViewModel.dispatch(FeedOptionViewAction.ChangeAllowNotificationPreset)
                    },
                    parseFullContentPresetOnClick = {
                        feedOptionViewModel.dispatch(FeedOptionViewAction.ChangeParseFullContentPreset)
                    },
                    unsubscribeOnClick = {
                        feedOptionViewModel.dispatch(FeedOptionViewAction.ShowDeleteDialog)
                    },
                    onGroupClick = {
                        feedOptionViewModel.dispatch(FeedOptionViewAction.SelectedGroup(it))
                    },
                    onAddNewGroup = {
                        feedOptionViewModel.dispatch(FeedOptionViewAction.ShowNewGroupDialog)
                    }
                )
            }
        }
    ) {
        content()
    }

    DeleteFeedDialog(feedName = feed?.name ?: "")

    TextFieldDialog(
        visible = viewState.newGroupDialogVisible,
        title = stringResource(R.string.create_new_group),
        icon = Icons.Outlined.CreateNewFolder,
        value = viewState.newGroupContent,
        placeholder = stringResource(R.string.name),
        onValueChange = {
            feedOptionViewModel.dispatch(FeedOptionViewAction.InputNewGroup(it))
        },
        onDismissRequest = {
            feedOptionViewModel.dispatch(FeedOptionViewAction.HideNewGroupDialog)
        },
        onConfirm = {
            feedOptionViewModel.dispatch(FeedOptionViewAction.AddNewGroup)
        }
    )

    TextFieldDialog(
        visible = viewState.renameDialogVisible,
        title = stringResource(R.string.rename),
        icon = Icons.Outlined.Edit,
        value = viewState.newName,
        placeholder = stringResource(R.string.name),
        onValueChange = {
            feedOptionViewModel.dispatch(FeedOptionViewAction.InputNewName(it))
        },
        onDismissRequest = {
            feedOptionViewModel.dispatch(FeedOptionViewAction.HideRenameDialog)
        },
        onConfirm = {
            feedOptionViewModel.dispatch(FeedOptionViewAction.Rename)
            feedOptionViewModel.dispatch(FeedOptionViewAction.Hide(scope))
            Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show()
        }
    )
}