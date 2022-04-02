package me.ash.reader.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset

@Composable
fun Menu(
    offset: DpOffset,
    expanded: Boolean,
    dismissFunction: () -> Unit = {},
) {
    Box {
        DropdownMenu(
//        modifier = Modifier.offset(offset.x.dp, offset.y.dp),
            offset = offset,
            expanded = expanded,
            onDismissRequest = dismissFunction,
        ) {
            DropdownMenuItem(
                text = {
                    Text(text = "打开")
                },
                onClick = {
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = "取消订阅")
                },
                onClick = {
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = "编辑")
                },
                onClick = {
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = "默认全文解析")
                },
                onClick = {
                }
            )
        }
    }
}