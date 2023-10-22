package me.ash.reader.ui.component.base

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material.icons.outlined.VerticalAlignCenter
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import me.ash.reader.R

@Composable
fun RYSortPriority(
    priority: Int = 0,
    setPriority: (Int) -> Unit = {}
) {
    FlowRow(
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        crossAxisSpacing = 10.dp,
        mainAxisSpacing = 10.dp,
    ) {
        RYSelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(id = R.string.priority_low),
            selected = priority == -2,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.VerticalAlignBottom,
                    contentDescription = stringResource(id = R.string.priority_low),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            setPriority(-2)
        }

        RYSelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(id = R.string.priority_mid),
            selected = priority == 0,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.VerticalAlignCenter,
                    contentDescription = stringResource(id = R.string.priority_mid),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            setPriority(0)
        }

        RYSelectionChip(
            modifier = Modifier.animateContentSize(),
            content = stringResource(id = R.string.priority_high),
            selected = priority == 2,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.VerticalAlignTop,
                    contentDescription = stringResource(id = R.string.priority_high),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            setPriority(2)
        }
    }
}