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

sealed class FlowArticleListDatePreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListDatePreference(true)
    object OFF : FlowArticleListDatePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListDate,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.flowArticleListDate: Flow<FlowArticleListDatePreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FlowArticleListDate.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun FlowArticleListDatePreference.not(): FlowArticleListDatePreference =
    when (value) {
        true -> FlowArticleListDatePreference.OFF
        false -> FlowArticleListDatePreference.ON
    }