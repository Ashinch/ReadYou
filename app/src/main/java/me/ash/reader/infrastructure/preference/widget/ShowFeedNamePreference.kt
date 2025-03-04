package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.showFeedName
import me.ash.reader.ui.ext.widgetDataStore
import me.ash.reader.ui.ext.put

val LocalShowFeedName =
    compositionLocalOf<ShowFeedNamePreference> { ShowFeedNamePreference.default }

sealed class ShowFeedNamePreference(val value: Boolean) : WidgetPreference(showFeedName) {
    data object ON : ShowFeedNamePreference(true)
    data object OFF : ShowFeedNamePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.widgetDataStore.put(
                showFeedName,
                value
            )
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) = scope.launch {
        Log.d("ShowFeedNamePreference", "Toggling. value: $value")
        context.widgetDataStore.put(
            showFeedName,
            !value
        )
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[showFeedName]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}
