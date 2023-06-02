package me.ash.reader.data.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowArticleListStarredAlphaPreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListStarredAlphaPreference(true)
    object OFF : FlowArticleListStarredAlphaPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListStarredAlpha,
                value
            )
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowArticleListStarredAlpha.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListStarredAlphaPreference.not(): FlowArticleListStarredAlphaPreference =
    when (value) {
        true -> FlowArticleListStarredAlphaPreference.OFF
        false -> FlowArticleListStarredAlphaPreference.ON
    }
