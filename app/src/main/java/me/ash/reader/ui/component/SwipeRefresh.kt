package me.ash.reader.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun SwipeRefresh(
    isRefresh: Boolean = false,
    onRefresh: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    com.google.accompanist.swiperefresh.SwipeRefresh(
        state = rememberSwipeRefreshState(isRefresh),
        onRefresh = onRefresh,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                fade = true,
                scale = true,
                contentColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surface onDark MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    ) {
        content()
    }
}