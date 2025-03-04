package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.widgetDataStore
import kotlin.reflect.KProperty

data class LatestArticleWidgetSettings(
    var showFeedIcon: ShowFeedIconPreference = ShowFeedIconPreference.default,
    var showFeedName: ShowFeedNamePreference = ShowFeedNamePreference.default
) {

    fun clear(context: Context, scope: CoroutineScope) {
        showFeedIcon.delete(context, scope)
        showFeedName.delete(context, scope)
    }

    fun load(context: Context) {
        val preferences = runBlocking {
            context.widgetDataStore.data.first()
        }
        showFeedIcon = ShowFeedIconPreference.fromPreferences(preferences)
        showFeedName = ShowFeedNamePreference.fromPreferences(preferences)
    }

    companion object {
        fun fromDataStore(context: Context): LatestArticleWidgetSettings {
            return LatestArticleWidgetSettings().apply {
                load(context)
            }
        }
    }
}

class LatestArticlesWidgetSettingDelegate {
    private var settings: LatestArticleWidgetSettings? = null
    operator fun getValue(thisRef: Context, property: KProperty<*>): LatestArticleWidgetSettings {
        if (settings == null) {
            settings = LatestArticleWidgetSettings.fromDataStore(thisRef)
        }
        return settings!!
    }
}

val Context.latestArticleWidgetSettings by LatestArticlesWidgetSettingDelegate()

@Composable
fun LatestArticleWidgetSettingsProvider(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember {
        context.widgetDataStore.data.map {
            Log.i("RLog", "AppTheme: ${it}")
            it.toLatestArticleWidgetSettings()
        }
    }.collectAsStateValue(initial = LatestArticleWidgetSettings())

    CompositionLocalProvider(
        LocalShowFeedIcon provides settings.showFeedIcon,
        LocalShowFeedName provides settings.showFeedName
    ) {
        content()
    }
}