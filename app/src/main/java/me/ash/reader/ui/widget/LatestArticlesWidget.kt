package me.ash.reader.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import me.ash.reader.R

/**
 * Implementation of App Widget functionality.
* App Widget Configuration implemented in [LatestArticlesWidgetConfigureActivity]
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
            deleteTitlePref(context, appWidgetId)
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

    val intent = Intent(context, LatestArticlesWidgetService::class.java)
    val views = RemoteViews(context.packageName, R.layout.latest_articles_widget).apply {
        setRemoteAdapter(R.id.article_container, intent)
    }
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.article_container)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}