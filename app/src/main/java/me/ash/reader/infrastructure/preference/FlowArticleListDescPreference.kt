package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListDesc
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowArticleListDesc =
    compositionLocalOf<FlowArticleListDescPreference> { FlowArticleListDescPreference.default }

sealed class FlowArticleListDescPreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListDescPreference(true)
    object OFF : FlowArticleListDescPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowArticleListDesc,
                value
            )
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListDesc]?.key as Preferences.Key<Boolean>]) {
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
