package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.showFeedIcon
import me.ash.reader.ui.ext.widgetDataStore
import me.ash.reader.ui.ext.put

val LocalShowFeedIcon =
    compositionLocalOf<ShowFeedIconPreference> { ShowFeedIconPreference.default }

sealed class ShowFeedIconPreference(val value: Boolean) : WidgetPreference(showFeedIcon) {
    data object ON : ShowFeedIconPreference(true)
    data object OFF : ShowFeedIconPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.widgetDataStore.put(
                showFeedIcon,
                value
            )
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) = scope.launch {
        context.widgetDataStore.put(
            showFeedIcon,
            !value
        )
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[showFeedIcon]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}
