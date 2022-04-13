package me.ash.reader.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BlockButtonRadios(
    modifier: Modifier = Modifier,
    selected: Int = 0,
    onSelected: (Int) -> Unit,
    items: List<BlockButtonRadiosItem> = listOf(),
) {

    Column {
        Row(
            modifier = modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                BlockButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (item == items.last()) 0.dp else 8.dp),
                    text = item.text,
                    selected = selected == index,
                ) {
                    onSelected(index)
                    item.onClick()
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        items[selected].content()
    }
}

data class BlockButtonRadiosItem(
    val text: String,
    val onClick: () -> Unit = {},
    val content: @Composable () -> Unit,
)