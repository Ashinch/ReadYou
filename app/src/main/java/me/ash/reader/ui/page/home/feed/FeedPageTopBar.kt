package me.ash.reader.ui.page.home.feed

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.ui.page.common.RouteName

@Composable
fun FeedPageTopBar(
    navController: NavHostController,
    isSyncing: Boolean = false,
    syncOnClick: () -> Unit = {},
    subscribeOnClick: () -> Unit = {},
) {
    SmallTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(route = RouteName.SETTINGS)
            }) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = {
                if (isSyncing) return@IconButton
                syncOnClick()
            }) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = subscribeOnClick) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Subscribe",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}