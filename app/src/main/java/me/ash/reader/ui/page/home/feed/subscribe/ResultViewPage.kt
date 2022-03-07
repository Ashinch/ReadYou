package me.ash.reader.ui.page.home.feed.subscribe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
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
import me.ash.reader.ui.widget.SelectionChip

@Composable
fun ResultViewPage() {
    Column {
        Link()
        Spacer(modifier = Modifier.height(26.dp))

        Preset()
        Spacer(modifier = Modifier.height(26.dp))

        AddToGroup()
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun Link() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SelectionContainer {
            Text(
                text = "https://material.io/feed.xml",
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun Preset() {
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
            selected = true,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Check",
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = { /*TODO*/ },
        ) {
            Text(
                text = "接收通知",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        SelectionChip(
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = "Check",
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = { /*TODO*/ }
        ) {
            Text(
                text = "全文输出",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun AddToGroup() {
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
        SelectionChip(
            selected = false,
            onClick = { /*TODO*/ },
        ) {
            Text(
                text = "未分组",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        SelectionChip(
            selected = true,
            onClick = { /*TODO*/ }
        ) {
            Text(
                text = "技术",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        SelectionChip(
            selected = true,
            onClick = { /*TODO*/ }
        ) {
            Text(
                text = "新鲜事",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        SelectionChip(
            selected = false,
            onClick = { /*TODO*/ }
        ) {
            Text(
                text = "游戏",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        SelectionChip(
            selected = true,
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
            )
        }
    }
}