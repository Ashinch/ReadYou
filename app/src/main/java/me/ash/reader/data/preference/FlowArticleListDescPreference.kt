package me.ash.reader.data.preference

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowArticleListDescPreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListDescPreference(true)
    object OFF : FlowArticleListDescPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListDesc,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.flowArticleListDesc: Flow<FlowArticleListDescPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FlowArticleListDesc.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun FlowArticleListDescPreference.not(): FlowArticleListDescPreference =
    when (value) {
        true -> FlowArticleListDescPreference.OFF
        false -> FlowArticleListDescPreference.ON
    }