package me.ash.reader.data.preference

import android.content.Context
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

@Immutable
object FeedsFilterBarPaddingPreference {
    const val default = 0

    val Context.feedsFilterBarPadding: Flow<Int>
        get() = this.dataStore.data.map {
            it[DataStoreKeys.FeedsFilterBarPadding.key] ?: default
        }

    fun put(context: Context, scope: CoroutineScope, value: Int) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(DataStoreKeys.FeedsFilterBarPadding, value)
        }
    }
}