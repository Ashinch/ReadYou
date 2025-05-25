package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.pullToSwitchFeed
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

class PullToSwitchFeedPreference(val value: Boolean) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.pullToSwitchFeed, value)
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) =
        PullToSwitchFeedPreference(!value).put(context, scope)

    companion object {
        val default = PullToSwitchFeedPreference(true)
        fun fromPreference(preference: Preferences): PullToSwitchFeedPreference {
            return PullToSwitchFeedPreference(
                preference[DataStoreKey.keys[pullToSwitchFeed]?.key as Preferences.Key<Boolean>]
                    ?: return default
            )
        }
    }
}