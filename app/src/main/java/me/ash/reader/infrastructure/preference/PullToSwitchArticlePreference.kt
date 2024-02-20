package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

class PullToSwitchArticlePreference(val value: Boolean) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKeys.PullToSwitchArticle, value)
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) =
        PullToSwitchArticlePreference(!value).put(context, scope)

    companion object {
        val default = PullToSwitchArticlePreference(true)
        fun fromPreference(preference: Preferences): PullToSwitchArticlePreference {
            return PullToSwitchArticlePreference(
                preference[DataStoreKeys.PullToSwitchArticle.key] ?: return default
            )
        }
    }
}