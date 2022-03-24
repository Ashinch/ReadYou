package me.ash.reader.ui.page.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.data.constant.Filter
import me.ash.reader.ui.extension.getName

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FilterBar2(
    modifier: Modifier = Modifier,
    filter: Filter,
    onSelected: (Filter) -> Unit = {},
) {
    NavigationBar(
        tonalElevation = 0.dp,
    ) {
        Spacer(modifier = Modifier.width(60.dp))
        listOf(
            Filter.Starred,
            Filter.Unread,
            Filter.All,
        ).forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (filter == item) item.filledIcon else item.icon,
                        contentDescription = item.getName()
                    )
                },
//                label = { Text(text = item.getName()) },
                selected = filter == item,
                onClick = { onSelected(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            )
        }
        Spacer(modifier = Modifier.width(60.dp))
    }
}