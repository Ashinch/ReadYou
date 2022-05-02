package me.ash.reader.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowArticleListImagePreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListImagePreference(true)
    object OFF : FlowArticleListImagePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListImage,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowArticleListImage.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListImagePreference.not(): FlowArticleListImagePreference =
    when (value) {
        true -> FlowArticleListImagePreference.OFF
        false -> FlowArticleListImagePreference.ON
    }