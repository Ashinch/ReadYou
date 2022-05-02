package me.ash.reader.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowArticleListTimePreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListTimePreference(true)
    object OFF : FlowArticleListTimePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListTime,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowArticleListTime.key]) {
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