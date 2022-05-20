package me.ash.reader.ui.page.home.feeds.drawer.feed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Edit
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
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.BottomDrawer
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.roundClick
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.page.home.feeds.subscribe.ResultView

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedOptionDrawer(
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val feedOptionUiState = feedOptionViewModel.feedOptionUiState.collectAsStateValue()
    val feed = feedOptionUiState.feed
    val toastString = stringResource(R.string.rename_toast, feedOptionUiState.newName)

    BackHandler(feedOptionUiState.drawerState.isVisible) {
        scope.launch {
            feedOptionUiState.drawerState.hide()
        }
    }

    BottomDrawer(
        drawerState = feedOptionUiState.drawerState,
        sheetContent = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    FeedIcon(feedName = feed?.name ?: "", size = 24.dp)
//                    Icon(
//                        modifier = Modifier.roundClick { },
//                        imageVector = Icons.Rounded.RssFeed,
//                        contentDescription = feed?.name ?: stringResource(R.string.unknown),
//                        tint = MaterialTheme.colorScheme.secondary,
//                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.roundClick {
                            feedOptionViewModel.showRenameDialog()
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
                    groups = feedOptionUiState.groups,
                    selectedAllowNotificationPreset = feedOptionUiState.feed?.isNotification ?: false,
                    selectedParseFullContentPreset = feedOptionUiState.feed?.isFullContent ?: false,
                    isMoveToGroup = true,
                    showUnsubscribe = true,
                    selectedGroupId = feedOptionUiState.feed?.groupId ?: "",
                    allowNotificationPresetOnClick = {
                        feedOptionViewModel.changeAllowNotificationPreset()
                    },
                    parseFullContentPresetOnClick = {
                        feedOptionViewModel.changeParseFullContentPreset()
                    },
                    clearArticlesOnClick = {
                        feedOptionViewModel.showClearDialog()
                    },
                    unsubscribeOnClick = {
                        feedOptionViewModel.showDeleteDialog()
                    },
                    onGroupClick = {
                        feedOptionViewModel.selectedGroup(it)
                    },
                    onAddNewGroup = {
                        feedOptionViewModel.showNewGroupDialog()
                    },
                    onFeedUrlClick = {
                        feedOptionViewModel.showFeedUrlDialog()
                    }
                )
            }
        }
    ) {
        content()
    }

    DeleteFeedDialog(feedName = feed?.name ?: "")

    ClearFeedDialog(feedName = feed?.name ?: "")

    TextFieldDialog(
        visible = feedOptionUiState.newGroupDialogVisible,
        title = stringResource(R.string.create_new_group),
        icon = Icons.Outlined.CreateNewFolder,
        value = feedOptionUiState.newGroupContent,
        placeholder = stringResource(R.string.name),
        onValueChange = {
            feedOptionViewModel.inputNewGroup(it)
        },
        onDismissRequest = {
            feedOptionViewModel.hideNewGroupDialog()
        },
        onConfirm = {
            feedOptionViewModel.addNewGroup()
        }
    )

    TextFieldDialog(
        visible = feedOptionUiState.renameDialogVisible,
        title = stringResource(R.string.rename),
        icon = Icons.Outlined.Edit,
        value = feedOptionUiState.newName,
        placeholder = stringResource(R.string.name),
        onValueChange = {
            feedOptionViewModel.inputNewName(it)
        },
        onDismissRequest = {
            feedOptionViewModel.hideRenameDialog()
        },
        onConfirm = {
            feedOptionViewModel.renameFeed()
            feedOptionViewModel.hideDrawer(scope)
            context.showToast(toastString)
        }
    )

    TextFieldDialog(
        visible = feedOptionUiState.changeUrlDialogVisible,
        title = stringResource(R.string.change_url),
        icon = Icons.Outlined.Edit,
        value = feedOptionUiState.newUrl,
        placeholder = stringResource(R.string.feed_url_placeholder),
        onValueChange = {
            feedOptionViewModel.inputNewUrl(it)
        },
        onDismissRequest = {
            feedOptionViewModel.hideFeedUrlDialog()
        },
        onConfirm = {
            feedOptionViewModel.changeFeedUrl()
            feedOptionViewModel.hideDrawer(scope)
        }
    )
}