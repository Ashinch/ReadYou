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

sealed class FlowArticleListFeedNamePreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListFeedNamePreference(true)
    object OFF : FlowArticleListFeedNamePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FlowArticleListFeedName,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.flowArticleListFeedName: Flow<FlowArticleListFeedNamePreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FlowArticleListFeedName.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun FlowArticleListFeedNamePreference.not(): FlowArticleListFeedNamePreference =
    when (value) {
        true -> FlowArticleListFeedNamePreference.OFF
        false -> FlowArticleListFeedNamePreference.ON
    }