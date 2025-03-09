package me.ash.reader.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.ash.reader.infrastructure.preference.widget.WidgetPreferencesManager
import me.ash.reader.ui.widget.ui.theme.ReadYouTheme

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



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReadYouTheme {
        Greeting("Android")
    }
}
