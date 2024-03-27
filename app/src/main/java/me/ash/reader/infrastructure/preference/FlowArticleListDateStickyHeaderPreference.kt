package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowArticleListDateStickyHeaderPreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListDateStickyHeaderPreference(true)
    object OFF : FlowArticleListDateStickyHeaderPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListDateStickyHeader,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowArticleListDateStickyHeader.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListDateStickyHeaderPreference.not(): FlowArticleListDateStickyHeaderPreference =
    when (value) {
        true -> FlowArticleListDateStickyHeaderPreference.OFF
        false -> FlowArticleListDateStickyHeaderPreference.ON
    }
