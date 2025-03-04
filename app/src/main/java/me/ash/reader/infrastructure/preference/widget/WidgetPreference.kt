package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.showFeedName
import me.ash.reader.ui.ext.widgetDataStore

sealed class WidgetPreference(val key: String) {

    abstract fun put(context: Context, scope: CoroutineScope)

    fun delete(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.widgetDataStore.edit { preferences ->
                preferences.remove(DataStoreKey.keys[showFeedName]?.key as Preferences.Key<*>)
            }
        }
    }
}

fun Preferences.toLatestArticleWidgetSettings(): LatestArticleWidgetSettings {
    return LatestArticleWidgetSettings(
        showFeedIcon = ShowFeedIconPreference.fromPreferences(this),
        showFeedName = ShowFeedNamePreference.fromPreferences(this)
    )
}