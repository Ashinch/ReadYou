package me.ash.reader.ui.page.home.feeds.subscribe

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import me.ash.reader.R
import me.ash.reader.data.entity.Group
import me.ash.reader.ui.component.SelectionChip
import me.ash.reader.ui.component.Subtitle
import me.ash.reader.ui.ext.roundClick
import me.ash.reader.ui.theme.palette.alwaysLight

@Composable
fun ResultView(
    modifier: Modifier = Modifier,
    link: String = "",
    groups: List<Group> = emptyList(),
    selectedAllowNotificationPreset: Boolean = false,
    selectedParseFullContentPreset: Boolean = false,
    isMoveToGroup: Boolean = false,
    showUnsubscribe: Boolean = false,
    selectedGroupId: String = "",
    allowNotificationPresetOnClick: () -> Unit = {},
    parseFullContentPresetOnClick: () -> Unit = {},
    unsubscribeOnClick: () -> Unit = {},
    onGroupClick: (groupId: String) -> Unit = {},
    onAddNewGroup: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        if (groups.isNotEmpty() && selectedGroupId.isEmpty()) onGroupClick(groups.first().id)
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Link(text = link)
        Spacer(modifier = Modifier.height(26.dp))

        Preset(
            selectedAllowNotificationPreset = selectedAllowNotificationPreset,
            selectedParseFullContentPreset = selectedParseFullContentPreset,
            showUnsubscribe = showUnsubscribe,
            allowNotificationPresetOnClick = allowNotificationPresetOnClick,
            parseFullContentPresetOnClick = parseFullContentPresetOnClick,
            unsubscribeOnClick = unsubscribeOnClick,
        )
        Spacer(modifier = Modifier.height(26.dp))

        AddToGroup(
            isMoveToGroup = isMoveToGroup,
            groups = groups,
            selectedGroupId = selectedGroupId,
            onGroupClick = onGroupClick,
            onAddNewGroup = onAddNewGroup,
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun Link(
    text: String,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SelectionContainer {
            Text(
                modifier = Modifier.roundClick {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(text))
                    )
                },
                text = text,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Preset(
    selectedAllowNotificationPreset: Boolean = false,
    selectedParseFullContentPreset: Boolean = false,
    showUnsubscribe: Boolean = false,
    allowNotificationPresetOnClick: () -> Unit = {},
    parseFullContentPresetOnClick: () -> Unit = {},
    unsubscribeOnClick: () -> Unit = {},
) {
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
            selected = selectedAllowNotificationPreset,
            selectedIcon = {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.allow_notification),
                    tint = MaterialTheme.colorScheme.onSurface alwaysLight true,
                )
            },
        ) {
            allowNotificationPresetOnClick()
        }
        SelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(R.string.parse_full_content),
            selected = selectedParseFullContentPreset,
            selectedIcon = {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                    imageVector = Icons.Outlined.Article,
                    contentDescription = stringResource(R.string.parse_full_content),
                    tint = MaterialTheme.colorScheme.onSurface alwaysLight true,
                )
            },
        ) {
            parseFullContentPresetOnClick()
        }
        if (showUnsubscribe) {
            SelectionChip(
                modifier = Modifier.animateContentSize(),
                content = stringResource(R.string.unsubscribe),
                selected = false,
            ) {
                unsubscribeOnClick()
            }
        }
    }
}

@Composable
private fun AddToGroup(
    isMoveToGroup: Boolean = false,
    groups: List<Group>,
    selectedGroupId: String,
    onGroupClick: (groupId: String) -> Unit = {},
    onAddNewGroup: () -> Unit = {},
) {
    Subtitle(text = stringResource(if (isMoveToGroup) R.string.move_to_group else R.string.add_to_group))
    Spacer(modifier = Modifier.height(10.dp))

    if (groups.size > 6) {
        LazyRow {
            items(groups) {
                SelectionChip(
                    modifier = Modifier.animateContentSize(),
                    content = it.name,
                    selected = it.id == selectedGroupId,
                ) {
                    onGroupClick(it.id)
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            item { NewGroupButton(onAddNewGroup) }
        }
    } else {
        FlowRow(
            mainAxisAlignment = MainAxisAlignment.Start,
            crossAxisSpacing = 10.dp,
            mainAxisSpacing = 10.dp,
        ) {
            groups.forEach {
                SelectionChip(
                    modifier = Modifier.animateContentSize(),
                    content = it.name,
                    selected = it.id == selectedGroupId,
                ) {
                    onGroupClick(it.id)
                }
            }
            NewGroupButton(onAddNewGroup)
        }
    }
}

@Composable
private fun NewGroupButton(onAddNewGroup: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onAddNewGroup() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.create_new_group),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}