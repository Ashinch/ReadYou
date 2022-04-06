package me.ash.reader.ui.page.home.drawer.group

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import me.ash.reader.R
import me.ash.reader.data.entity.Group
import me.ash.reader.ui.component.BottomDrawer
import me.ash.reader.ui.component.SelectionChip
import me.ash.reader.ui.component.Subtitle
import me.ash.reader.ui.component.TextFieldDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.getDefaultGroupId
import me.ash.reader.ui.ext.roundClick

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GroupOptionDrawer(
    modifier: Modifier = Modifier,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewState = groupOptionViewModel.viewState.collectAsStateValue()
    val group = viewState.group
    val toastString = stringResource(R.string.rename_toast, viewState.newName)

    BottomDrawer(
        drawerState = viewState.drawerState,
        sheetContent = {
            Column {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = group?.name ?: stringResource(R.string.unknown),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.roundClick {
                            groupOptionViewModel.dispatch(GroupOptionViewAction.ShowRenameDialog)
                        },
                        text = group?.name ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.group_option_tip),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))
                    Subtitle(text = stringResource(R.string.preset))

                    Spacer(modifier = Modifier.height(10.dp))
                    Preset(groupOptionViewModel, group, context)

                    if (viewState.groups.size != 1) {
                        Spacer(modifier = Modifier.height(26.dp))
                        Subtitle(text = stringResource(R.string.move_to_group))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (viewState.groups.size > 6) {
                            LazyRowGroups(viewState, group, groupOptionViewModel)
                        } else {
                            FlowRowGroups(viewState, group, groupOptionViewModel)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    ) {
        content()
    }

    DeleteGroupDialog(groupName = group?.name ?: "")
    AllAllowNotificationDialog(groupName = group?.name ?: "")
    AllParseFullContentDialog(groupName = group?.name ?: "")
    AllMoveToGroupDialog(groupName = group?.name ?: "")
    TextFieldDialog(
        visible = viewState.renameDialogVisible,
        title = stringResource(R.string.rename),
        icon = Icons.Outlined.Edit,
        value = viewState.newName,
        placeholder = stringResource(R.string.name),
        onValueChange = {
            groupOptionViewModel.dispatch(GroupOptionViewAction.InputNewName(it))
        },
        onDismissRequest = {
            groupOptionViewModel.dispatch(GroupOptionViewAction.HideRenameDialog)
        },
        onConfirm = {
            groupOptionViewModel.dispatch(GroupOptionViewAction.Rename)
            groupOptionViewModel.dispatch(GroupOptionViewAction.Hide(scope))
            Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
private fun Preset(
    groupOptionViewModel: GroupOptionViewModel,
    group: Group?,
    context: Context
) {
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        SelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(R.string.allow_notification),
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.allow_notification),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            groupOptionViewModel.dispatch(GroupOptionViewAction.ShowAllAllowNotificationDialog)
        }
        SelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(R.string.parse_full_content),
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = stringResource(R.string.parse_full_content),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            groupOptionViewModel.dispatch(GroupOptionViewAction.ShowAllParseFullContentDialog)
        }
        if (group?.id != context.currentAccountId.getDefaultGroupId()) {
            SelectionChip(
                modifier = Modifier.animateContentSize(),
                content = stringResource(R.string.delete_group),
                selected = false,
            ) {
                groupOptionViewModel.dispatch(GroupOptionViewAction.ShowDeleteDialog)
            }
        }
    }
}

@Composable
private fun FlowRowGroups(
    viewState: GroupOptionViewState,
    group: Group?,
    groupOptionViewModel: GroupOptionViewModel
) {
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        viewState.groups.forEach {
            if (it.id != group?.id) {
                SelectionChip(
                    modifier = Modifier.animateContentSize(),
                    content = it.name,
                    selected = false,
                ) {
                    groupOptionViewModel.dispatch(
                        GroupOptionViewAction.ShowAllMoveToGroupDialog(it)
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyRowGroups(
    viewState: GroupOptionViewState,
    group: Group?,
    groupOptionViewModel: GroupOptionViewModel
) {
    LazyRow {
        items(viewState.groups) {
            if (it.id != group?.id) {
                SelectionChip(
                    modifier = Modifier.animateContentSize(),
                    content = it.name,
                    selected = false,
                ) {
                    groupOptionViewModel.dispatch(
                        GroupOptionViewAction.ShowAllMoveToGroupDialog(it)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}
