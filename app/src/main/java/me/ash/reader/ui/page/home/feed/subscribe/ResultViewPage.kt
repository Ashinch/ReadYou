package me.ash.reader.ui.page.home.feed.subscribe

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import me.ash.reader.data.group.Group
import me.ash.reader.ui.widget.SelectionChip

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
    Column {
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
    Text(
        text = "预设",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
    )
    Spacer(modifier = Modifier.height(10.dp))
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        SelectionChip(
            selected = selectedNotificationPreset,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Check",
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = notificationPresetOnClick,
        ) {
            Text(
                text = "接收通知",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        SelectionChip(
            selected = selectedFullContentParsePreset,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = "Check",
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = fullContentParsePresetOnClick,
        ) {
            Text(
                text = "全文解析",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
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
    Text(
        text = "添加到组",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
    )
    Spacer(modifier = Modifier.height(10.dp))
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        groups.forEach {
            SelectionChip(
                modifier = Modifier.animateContentSize(),
                selected = it.id == selectedGroupId,
                onClick = { groupOnClick(it.id) },
            ) {
                Text(
                    text = it.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }

        SelectionChip(
            selected = false,
            onClick = { /*TODO*/ },
        ) {
            BasicTextField(
                modifier = Modifier.width(56.dp),
                value = "新建分组",
                onValueChange = {},
                textStyle = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                ),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        onKeyboardAction()
                    }
                )
            )
        }
    }
}