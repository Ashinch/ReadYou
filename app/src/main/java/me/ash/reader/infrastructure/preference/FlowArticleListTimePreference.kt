package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListTime
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowArticleListTime =
    compositionLocalOf<FlowArticleListTimePreference> { FlowArticleListTimePreference.default }

sealed class FlowArticleListTimePreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListTimePreference(true)
    object OFF : FlowArticleListTimePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowArticleListTime,
                value
            )
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListTime]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListTimePreference.not(): FlowArticleListTimePreference =
    when (value) {
        true -> FlowArticleListTimePreference.OFF
        false -> FlowArticleListTimePreference.ON
    }
