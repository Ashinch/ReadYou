package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import me.ash.reader.data.group.Group
import me.ash.reader.ui.widget.SelectionChip
import me.ash.reader.ui.widget.SelectionEditorChip
import me.ash.reader.ui.widget.Subtitle

@Composable
fun ResultViewPage(
    link: String = "",
    groups: List<Group> = emptyList(),
    selectedNotificationPreset: Boolean = false,
    selectedFullContentParsePreset: Boolean = false,
    selectedGroupId: String = "",
    notificationPresetOnClick: () -> Unit = {},
    fullContentParsePresetOnClick: () -> Unit = {},
    groupOnClick: (groupId: String) -> Unit = {},
    onKeyboardAction: () -> Unit = {},
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Link(
            text = link
        )
        Spacer(modifier = Modifier.height(26.dp))

        Preset(
            selectedNotificationPreset = selectedNotificationPreset,
            selectedFullContentParsePreset = selectedFullContentParsePreset,
            notificationPresetOnClick = notificationPresetOnClick,
            fullContentParsePresetOnClick = fullContentParsePresetOnClick,
        )
        Spacer(modifier = Modifier.height(26.dp))

        AddToGroup(
            groups = groups,
            selectedGroupId = selectedGroupId,
            groupOnClick = groupOnClick,
            onKeyboardAction = onKeyboardAction,
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun Link(
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SelectionContainer {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun Preset(
    selectedNotificationPreset: Boolean = false,
    selectedFullContentParsePreset: Boolean = false,
    notificationPresetOnClick: () -> Unit = {},
    fullContentParsePresetOnClick: () -> Unit = {},
) {
    Subtitle(text = "预设")
    Spacer(modifier = Modifier.height(10.dp))
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        SelectionChip(
            modifier = Modifier.animateContentSize(),
            content = "接收通知",
            selected = selectedNotificationPreset,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Check",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp),
                )
            },
        ) {
            notificationPresetOnClick()
        }
        SelectionChip(
            modifier = Modifier.animateContentSize(),
            content = "全文解析",
            selected = selectedFullContentParsePreset,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = "Check",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp),
                )
            },
        ) {
            fullContentParsePresetOnClick()
        }
    }
}

@Composable
private fun AddToGroup(
    groups: List<Group>,
    selectedGroupId: String,
    groupOnClick: (groupId: String) -> Unit = {},
    onKeyboardAction: () -> Unit = {},
) {
    Subtitle(text = "添加到组")
    Spacer(modifier = Modifier.height(10.dp))
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
                groupOnClick(it.id)
            }
        }

        SelectionEditorChip(
            modifier = Modifier.animateContentSize(),
            content = "新建分组",
            selected = false,
            onKeyboardAction = onKeyboardAction,
        ) {

        }
    }
}