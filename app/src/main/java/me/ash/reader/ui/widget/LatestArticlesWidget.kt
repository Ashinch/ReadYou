package me.ash.reader.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.runBlocking
import me.ash.reader.R
import me.ash.reader.infrastructure.android.MainActivity
import me.ash.reader.infrastructure.preference.widget.latestArticleWidgetSettings

/**
 * Implementation of App Widget functionality.
* App Widget Configuration implemented in [LatestArticlesWidgetConfigActivity]
 */
class LatestArticlesWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteWidgetSettings(context, appWidgetId)
        }
    }
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}



internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

    val serviceIntent = Intent(context, LatestArticlesWidgetService::class.java)
    val views = RemoteViews(context.packageName, R.layout.latest_articles_widget).apply {
        setRemoteAdapter(R.id.article_container, serviceIntent)

        val viewArticleIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        setPendingIntentTemplate(
            R.id.article_container,
            PendingIntent.getActivity(context, 0, viewArticleIntent, PendingIntent.FLAG_MUTABLE)
        )
    }
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.article_container)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}



private const val PREFS_NAME = "me.ash.reader.ui.widget.LatestArticlesWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: context.getString(R.string.appwidget_text)
}

internal fun deleteWidgetSettings(context: Context, appWidgetId: Int) {
    runBlocking {
        Log.d("deleteWidgetSettings", "Deleting widget settings")
        context.latestArticleWidgetSettings.clear(context, this)
    }
}