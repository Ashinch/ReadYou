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

sealed class FilterBarStylePreference(val value: Int) : Preference() {
    object Icon : FilterBarStylePreference(0)
    object IconLabel : FilterBarStylePreference(1)
    object IconLabelOnlySelected : FilterBarStylePreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FilterBarStyle,
                value
            )
        }
    }

    fun getDesc(context: Context): String =
        when (this) {
            Icon -> "图标"
            IconLabel -> "图标 + 标签"
            IconLabelOnlySelected -> "图标 + 标签（仅选中时）"
        }

    companion object {
        val default = Icon
        val values = listOf(Icon, IconLabel, IconLabelOnlySelected)

        val Context.filterBarStyle: Flow<FilterBarStylePreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FilterBarStyle.key]) {
                    0 -> Icon
                    1 -> IconLabel
                    2 -> IconLabelOnlySelected
                    else -> default
                }
            }
    }
}