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

sealed class FeedsFilterBarFilledPreference(val value: Boolean) : Preference() {
    object ON : FeedsFilterBarFilledPreference(true)
    object OFF : FeedsFilterBarFilledPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FeedsFilterBarFilled,
                value
            )
        }
    }

    companion object {
        val default = OFF
        val values = listOf(ON, OFF)

        val Context.feedsFilterBarFilled: Flow<FeedsFilterBarFilledPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FeedsFilterBarFilled.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun FeedsFilterBarFilledPreference.not(): FeedsFilterBarFilledPreference =
    when (value) {
        true -> FeedsFilterBarFilledPreference.OFF
        false -> FeedsFilterBarFilledPreference.ON
    }