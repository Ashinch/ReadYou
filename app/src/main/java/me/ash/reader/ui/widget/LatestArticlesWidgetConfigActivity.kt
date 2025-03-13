package me.ash.reader.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.widget.WidgetPreferencesManager
import me.ash.reader.ui.theme.AppTheme

class LatestArticlesWidgetConfigActivity : ComponentActivity() {

    private lateinit var widgetPreferencesManager: WidgetPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        widgetPreferencesManager = WidgetPreferencesManager.getInstance(this)

        setContent {
            // Extract MaterialTheme colors for use in (non-Compose) widget
            val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
            val onPrimaryColor = MaterialTheme.colorScheme.onPrimary.toArgb()

            LaunchedEffect(Unit) {
                widgetPreferencesManager.primaryColor.put(appWidgetId, primaryColor)
                widgetPreferencesManager.onPrimaryColor.put(appWidgetId, onPrimaryColor)
            }
            AppTheme(useDarkTheme = LocalDarkTheme.current.isDarkTheme()) {
                LatestArticlesWidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    widgetPreferencesManager = widgetPreferencesManager
                ) {
                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}