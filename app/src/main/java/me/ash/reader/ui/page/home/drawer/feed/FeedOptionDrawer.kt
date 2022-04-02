package me.ash.reader.ui.page.home.drawer.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.extension.roundClick
import me.ash.reader.ui.page.home.feeds.subscribe.ResultViewPage
import me.ash.reader.ui.widget.BottomDrawer
import me.ash.reader.ui.widget.Subtitle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedOptionDrawer(
    modifier: Modifier = Modifier,
    viewModel: FeedOptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {},
) {
    val viewState = viewModel.viewState.collectAsStateValue()
    val feed = viewState.feed

    BottomDrawer(
        drawerState = viewState.drawerState,
        sheetContent = {
            Column {
                Icon(
                    modifier = modifier
                        .roundClick { }
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    imageVector = Icons.Rounded.RssFeed,
                    contentDescription = feed?.name
                        ?: stringResource(R.string.unknown),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = modifier.height(16.dp))
                Text(
                    modifier = Modifier
                        .roundClick {}
                        .fillMaxWidth(),
                    text = feed?.name ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = modifier.height(16.dp))
                ResultViewPage(
                    link = feed?.url ?: stringResource(R.string.unknown),
                    groups = viewState.groups,
                    selectedAllowNotificationPreset = viewState.feed?.isNotification ?: false,
                    selectedParseFullContentPreset = viewState.feed?.isFullContent ?: false,
                    selectedGroupId = viewState.feed?.groupId ?: "",
                    newGroupContent = viewState.newGroupContent,
                    onNewGroupValueChange = {
                        viewModel.dispatch(FeedOptionViewAction.InputNewGroup(it))
                    },
                    newGroupSelected = viewState.newGroupSelected,
                    changeNewGroupSelected = {
                        viewModel.dispatch(FeedOptionViewAction.SelectedNewGroup(it))
                    },
                    allowNotificationPresetOnClick = {
                        viewModel.dispatch(FeedOptionViewAction.ChangeAllowNotificationPreset)
                    },
                    parseFullContentPresetOnClick = {
                        viewModel.dispatch(FeedOptionViewAction.ChangeParseFullContentPreset)
                    },
                    onGroupClick = {
                        viewModel.dispatch(FeedOptionViewAction.SelectedGroup(it))
                    },
                    onKeyboardAction = { },
                )
                Spacer(modifier = Modifier.height(20.dp))
                Subtitle(text = stringResource(R.string.options))
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error,
                    ),
                    onClick = {
                        viewModel.dispatch(FeedOptionViewAction.ShowDeleteDialog)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.delete),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(R.string.unsubscribe),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    ) {
        content()
    }

    DeleteFeedDialog(feedName = feed?.name ?: "")
}