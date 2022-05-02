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

sealed class FlowFilterBarFilledPreference(val value: Boolean) : Preference() {
    object ON : FlowFilterBarFilledPreference(true)
    object OFF : FlowFilterBarFilledPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FlowFilterBarFilled,
                value
            )
        }
    }

    companion object {
        val default = OFF
        val values = listOf(ON, OFF)

        val Context.flowFilterBarFilled: Flow<FlowFilterBarFilledPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FlowFilterBarFilled.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun FlowFilterBarFilledPreference.not(): FlowFilterBarFilledPreference =
    when (value) {
        true -> FlowFilterBarFilledPreference.OFF
        false -> FlowFilterBarFilledPreference.ON
    }