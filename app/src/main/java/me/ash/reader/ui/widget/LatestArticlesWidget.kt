package me.ash.reader.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.infrastructure.android.MainActivity
import me.ash.reader.infrastructure.preference.widget.WidgetPreferencesManager

/**
 * Implementation of App Widget functionality.
* App Widget Configuration implemented in [LatestArticlesWidgetConfigActivity]
 */
class LatestArticlesWidget : AppWidgetProvider() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            scope.launch {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
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
        val widgetPreferencesManager = WidgetPreferencesManager.getInstance(context)
        widgetPreferencesManager.deleteAll(context, scope)
    }

    private fun deleteWidgetSettings(context: Context, appWidgetId: Int) {
        scope.launch {
            WidgetPreferencesManager.getInstance(context).deleteAllForId(appWidgetId, this)
        }
    }


    companion object {
        fun notifyAllViewDataChanged(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, LatestArticlesWidget::class.java)
            )
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.article_container)
        }

        suspend fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

            val widgetPreferencesManager = WidgetPreferencesManager.getInstance(context)

            val serviceIntent = Intent(context, LatestArticlesWidgetService::class.java).apply {
                // https://stackoverflow.com/questions/11350287/ongetviewfactory-only-called-once-for-multiple-widgets
                setData(Uri.fromParts("content", appWidgetId.toString(), null))
            }

            // Update articles
            val articleViews = RemoteViews(context.packageName, R.layout.latest_articles_widget).apply {
                setRemoteAdapter(R.id.article_container, serviceIntent)

                val headerText = widgetPreferencesManager.headingText.get(appWidgetId)
                Log.d("LatestArticlesWidget", "headerText: $headerText")
                setTextViewText(R.id.header, headerText)

                val viewArticleIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                setPendingIntentTemplate(
                    R.id.article_container,
                    PendingIntent.getActivity(context, 0, viewArticleIntent, PendingIntent.FLAG_MUTABLE)
                )
            }
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.article_container)
            appWidgetManager.updateAppWidget(appWidgetId, articleViews)
        }
    }
}
