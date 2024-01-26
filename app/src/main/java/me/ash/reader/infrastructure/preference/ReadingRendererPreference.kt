package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ReadingRendererPreference(val value: Int) : Preference() {
    object WebView : ReadingRendererPreference(0)
    object NativeComponent : ReadingRendererPreference(1)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.ReadingRenderer,
                value
            )
        }
    }

    @Stable
    fun toDesc(context: Context): String =
        when (this) {
            WebView -> context.getString(R.string.web_view)
            NativeComponent -> context.getString(R.string.native_component)
        }

    companion object {

        val default = WebView
        val values = listOf(WebView, NativeComponent)

        fun fromPreferences(preferences: Preferences): ReadingRendererPreference =
            when (preferences[DataStoreKeys.ReadingRenderer.key]) {
                0 -> WebView
                1 -> NativeComponent
                else -> default
            }
    }
}
