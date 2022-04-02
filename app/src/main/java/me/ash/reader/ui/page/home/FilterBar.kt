package me.ash.reader.ui.page.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.data.entity.Filter
import me.ash.reader.ui.ext.getName

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FilterBar(
    modifier: Modifier = Modifier,
    filter: Filter,
    filterOnClick: (Filter) -> Unit = {},
) {
    Box(
        modifier = Modifier.height(60.dp)
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .zIndex(1f),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
        )
        NavigationBar(
            modifier = Modifier.fillMaxSize(),
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
                            imageVector = item.icon,
                            contentDescription = item.getName()
                        )
                    },
                    selected = filter == item,
                    onClick = { filterOnClick(item) },
//                    colors = NavigationBarItemDefaults.colors(
//                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
//                        unselectedIconColor = MaterialTheme.colorScheme.outline,
//                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
//                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
//                    )
                )
            }
            Spacer(modifier = Modifier.width(60.dp))
        }
    }
}