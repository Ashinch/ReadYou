package me.ash.reader.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowArticleListDescPreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListDescPreference(true)
    object OFF : FlowArticleListDescPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListDesc,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowArticleListDesc.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListDescPreference.not(): FlowArticleListDescPreference =
    when (value) {
        true -> FlowArticleListDescPreference.OFF
        false -> FlowArticleListDescPreference.ON
    }