package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.swipeEndAction
import me.ash.reader.ui.ext.DataStoreKey.Companion.swipeStartAction
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

data object SwipeGestureActions {
    const val None = 0
    const val ToggleRead = 1
    const val ToggleStarred = 2
}

val LocalArticleListSwipeEndAction = compositionLocalOf { SwipeEndActionPreference.default }

sealed class SwipeEndActionPreference(val action: Int) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.swipeEndAction, action
            )
        }
    }

    data object None : SwipeEndActionPreference(SwipeGestureActions.None)
    data object ToggleRead : SwipeEndActionPreference(SwipeGestureActions.ToggleRead)
    data object ToggleStarred :
        SwipeEndActionPreference(SwipeGestureActions.ToggleStarred)

    val desc: String
        @Composable get() = when (this) {
            None -> stringResource(id = R.string.none)
            ToggleRead -> stringResource(id = R.string.toggle_read)
            ToggleStarred -> stringResource(id = R.string.toggle_starred)
        }

    companion object {
        val default: SwipeEndActionPreference = ToggleRead
        val values = listOf(
            None,
            ToggleRead,
            ToggleStarred
        )

        fun fromPreferences(preferences: Preferences): SwipeEndActionPreference {
            return when (preferences[DataStoreKey.keys[swipeEndAction]?.key as Preferences.Key<Int>]) {
                SwipeGestureActions.None -> None
                SwipeGestureActions.ToggleRead -> ToggleRead
                SwipeGestureActions.ToggleStarred -> ToggleStarred
                else -> default
            }
        }
    }
}

val LocalArticleListSwipeStartAction = compositionLocalOf { SwipeStartActionPreference.default }

sealed class SwipeStartActionPreference(val action: Int) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.swipeStartAction, action)
        }
    }

    data object None : SwipeStartActionPreference(SwipeGestureActions.None)
    data object ToggleRead : SwipeStartActionPreference(SwipeGestureActions.ToggleRead)
    data object ToggleStarred :
        SwipeStartActionPreference(SwipeGestureActions.ToggleStarred)

    val desc: String
        @Composable get() = when (this) {
            None -> stringResource(id = R.string.none)
            ToggleRead -> stringResource(id = R.string.toggle_read)
            ToggleStarred -> stringResource(id = R.string.toggle_starred)
        }

    companion object {
        val default: SwipeStartActionPreference = ToggleStarred
        val values = listOf(
            None,
            ToggleRead,
            ToggleStarred
        )

        fun fromPreferences(preferences: Preferences): SwipeStartActionPreference {
            return when (preferences[DataStoreKey.keys[swipeStartAction]?.key as Preferences.Key<Int>]) {
                SwipeGestureActions.None -> None
                SwipeGestureActions.ToggleRead -> ToggleRead
                SwipeGestureActions.ToggleStarred -> ToggleStarred
                else -> default
            }
        }
    }
}
