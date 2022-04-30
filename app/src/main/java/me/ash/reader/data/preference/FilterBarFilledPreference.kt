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

sealed class FilterBarFilledPreference(val value: Boolean) : Preference() {
    object ON : FilterBarFilledPreference(true)
    object OFF : FilterBarFilledPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FilterBarFilled,
                value
            )
        }
    }

    companion object {
        val default = OFF
        val values = listOf(ON, OFF)

        val Context.filterBarFilled: Flow<FilterBarFilledPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FilterBarFilled.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun FilterBarFilledPreference.not(): FilterBarFilledPreference =
    when (value) {
        true -> FilterBarFilledPreference.OFF
        false -> FilterBarFilledPreference.ON
    }