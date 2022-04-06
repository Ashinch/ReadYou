package me.ash.reader.ui.page.home.drawer.group

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.ash.reader.ui.component.BottomDrawer
import me.ash.reader.ui.component.SelectionChip
import me.ash.reader.ui.component.Subtitle
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.getDefaultGroupId
import me.ash.reader.ui.ext.roundClick

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GroupOptionDrawer(
    modifier: Modifier = Modifier,
    GroupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val viewState = GroupOptionViewModel.viewState.collectAsStateValue()
    val group = viewState.group

    BottomDrawer(
        drawerState = viewState.drawerState,
        sheetContent = {
            Column {
                Icon(
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = group?.name ?: stringResource(R.string.unknown),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = modifier.height(16.dp))
                Text(
                    modifier = Modifier
                        .roundClick {}
                        .fillMaxWidth(),
                    text = group?.name ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = modifier.height(16.dp))
                Column(
                    modifier = modifier.verticalScroll(rememberScrollState())
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
                            GroupOptionViewModel.dispatch(GroupOptionViewAction.ShowAllAllowNotificationDialog)
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
                            GroupOptionViewModel.dispatch(GroupOptionViewAction.ShowAllParseFullContentDialog)
                        }
                        if (group?.id != context.currentAccountId.getDefaultGroupId()) {
                            SelectionChip(
                                modifier = Modifier.animateContentSize(),
                                content = stringResource(R.string.delete_group),
                                selected = false,
                            ) {
                                GroupOptionViewModel.dispatch(GroupOptionViewAction.ShowDeleteDialog)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(26.dp))

                    Subtitle(text = stringResource(R.string.move_to_group))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (viewState.groups.size > 6) {
                        LazyRow {
                            items(viewState.groups) {
                                if (it.id != group?.id) {
                                    SelectionChip(
                                        modifier = Modifier.animateContentSize(),
                                        content = it.name,
                                        selected = false,
                                    ) {
                                        GroupOptionViewModel.dispatch(
                                            GroupOptionViewAction.ShowAllMoveToGroupDialog(it)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        }
                    } else {
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
                                        GroupOptionViewModel.dispatch(
                                            GroupOptionViewAction.ShowAllMoveToGroupDialog(it)
                                        )
                                    }
                                }
                            }
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
}
