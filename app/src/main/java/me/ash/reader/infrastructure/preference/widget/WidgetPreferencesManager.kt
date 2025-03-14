package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.widgetDataStore

/**
 * Get the widget-specific key (as a string) for the given key name.
 */
fun widgetDataKey(keyName: String, appWidgetId: Int): String = "${keyName}_${appWidgetId}"

class WidgetPreferencesManager(context: Context) {
    internal val headingText = StringWidgetPreference(context, "headingText", context.resources.getString(R.string.latest))
    internal val groupToDisplay = StringWidgetPreference(context, "groupToDisplay", "")
    internal val maxLatestArticleCount = IntWidgetPreference(context, "maxLatestArticleCount", 20)
    internal val showFeedIcon = BooleanWidgetPreference(context, "showFeedIcon", true)
    internal val showFeedName = BooleanWidgetPreference(context, "showFeedName", false)
    internal val readArticleDisplay = ReadArticleDisplayPreference(context)
    internal val primaryColor = IntWidgetPreference(context, "primaryColor", 0x6200EE)
    internal val onPrimaryColor = IntWidgetPreference(context, "onPrimaryColor", 0xFFFFFF)

    val allPreferences = arrayOf(
        headingText,
        groupToDisplay,
        maxLatestArticleCount,
        showFeedIcon,
        showFeedName,
        readArticleDisplay,
        primaryColor,
        onPrimaryColor
    )

    fun deleteAllForId(widgetId: Int, scope: CoroutineScope) {
        scope.launch {
            for (p in allPreferences) {
                p.delete(widgetId, this)
            }
        }
    }

    fun deleteAll(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.widgetDataStore.edit {
                it.clear()
            }
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
