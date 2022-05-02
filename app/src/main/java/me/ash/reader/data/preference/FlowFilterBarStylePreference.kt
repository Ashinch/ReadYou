package me.ash.reader.data.preference

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowFilterBarStylePreference(val value: Int) : Preference() {
    object Icon : FlowFilterBarStylePreference(0)
    object IconLabel : FlowFilterBarStylePreference(1)
    object IconLabelOnlySelected : FlowFilterBarStylePreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FlowFilterBarStyle,
                value
            )
        }
    }

    fun getDesc(context: Context): String =
        when (this) {
            Icon -> context.getString(R.string.icons)
            IconLabel -> context.getString(R.string.icons_and_labels)
            IconLabelOnlySelected -> context.getString(R.string.icons_and_label_only_selected)
        }

    companion object {
        val default = Icon
        val values = listOf(Icon, IconLabel, IconLabelOnlySelected)

        val Context.flowFilterBarStyle: Flow<FlowFilterBarStylePreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FlowFilterBarStyle.key]) {
                    0 -> Icon
                    1 -> IconLabel
                    2 -> IconLabelOnlySelected
                    else -> default
                }
            }
    }
}