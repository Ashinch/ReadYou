package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Get the widget-specific key (as a string) for the given key name.
 */
fun widgetDataKey(keyName: String, appWidgetId: Int): String = "${keyName}_${appWidgetId}"

class WidgetPreferencesManager(context: Context) {
    internal val groupToDisplay = StringWidgetPreference(context, "groupToDisplay", "")
    internal val maxLatestArticleCount = IntWidgetPreference(context, "maxLatestArticleCount", 20)
    internal val showFeedIcon = BooleanWidgetPreference(context, "showFeedIcon", true)
    internal val showFeedName = BooleanWidgetPreference(context, "showFeedName", false)

    fun deleteAll(widgetId: Int, scope: CoroutineScope) {
        scope.launch {
            groupToDisplay.delete(widgetId, this)
            maxLatestArticleCount.delete(widgetId, this)
            showFeedIcon.delete(widgetId, this)
            showFeedName.delete(widgetId, this)
        }
    }

    companion object {
        @Volatile
        private var instance: WidgetPreferencesManager? = null

        fun getInstance(context: Context): WidgetPreferencesManager {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = WidgetPreferencesManager(context)
                    }
                }
            }
            return instance!!
        }
    }
}
