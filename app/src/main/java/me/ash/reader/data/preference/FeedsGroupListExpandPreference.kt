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

sealed class FeedsGroupListExpandPreference(val value: Boolean) : Preference() {
    object ON : FeedsGroupListExpandPreference(true)
    object OFF : FeedsGroupListExpandPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FeedsGroupListExpand,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        val Context.feedsGroupListExpand: Flow<FeedsGroupListExpandPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FeedsGroupListExpand.key]) {
                    true -> ON
                    false -> OFF
                    else -> default
                }
            }
    }
}

operator fun FeedsGroupListExpandPreference.not(): FeedsGroupListExpandPreference =
    when (value) {
        true -> FeedsGroupListExpandPreference.OFF
        false -> FeedsGroupListExpandPreference.ON
    }