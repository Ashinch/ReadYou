package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListFeedName
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowArticleListFeedName =
    compositionLocalOf<FlowArticleListFeedNamePreference> { FlowArticleListFeedNamePreference.default }

sealed class FlowArticleListFeedNamePreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListFeedNamePreference(true)
    object OFF : FlowArticleListFeedNamePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowArticleListFeedName,
                value
            )
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListFeedName]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListFeedNamePreference.not(): FlowArticleListFeedNamePreference =
    when (value) {
        true -> FlowArticleListFeedNamePreference.OFF
        false -> FlowArticleListFeedNamePreference.ON
    }
