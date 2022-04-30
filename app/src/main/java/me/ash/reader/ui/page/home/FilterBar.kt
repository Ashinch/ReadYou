package me.ash.reader.ui.page.home

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
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
    val view = LocalView.current

    Box(
//        modifier = Modifier.height(60.dp)
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .zIndex(1f),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
        )
        NavigationBar(
//            modifier = Modifier.fillMaxSize(),
//            tonalElevation = 0.dp,
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
                    label = { Text(text = item.getName(), style = MaterialTheme.typography.labelMedium) },
                    selected = filter == item,
                    onClick = {
//                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        filterOnClick(item)
                    },
                    colors = NavigationBarItemDefaults.colors(
//                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer alwaysLight true,
//                        unselectedIconColor = MaterialTheme.colorScheme.outline,
//                        selectedTextColor = MaterialTheme.colorScheme.onSurface alwaysLight true,
//                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                        indicatorColor = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
                    )
                )
            }
            Spacer(modifier = Modifier.width(60.dp))
        }
    }
}