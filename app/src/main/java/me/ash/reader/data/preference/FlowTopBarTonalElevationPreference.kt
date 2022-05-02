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

sealed class FlowTopBarTonalElevationPreference(val value: Int) : Preference() {
    object Level0 : FlowTopBarTonalElevationPreference(0)
    object Level1 : FlowTopBarTonalElevationPreference(1)
    object Level2 : FlowTopBarTonalElevationPreference(3)
    object Level3 : FlowTopBarTonalElevationPreference(6)
    object Level4 : FlowTopBarTonalElevationPreference(8)
    object Level5 : FlowTopBarTonalElevationPreference(12)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(
                DataStoreKeys.FlowTopBarTonalElevation,
                value
            )
        }
    }

    fun getDesc(context: Context): String =
        when (this) {
            Level0 -> "Level 0 (0dp)"
            Level1 -> "Level 1 (1dp)"
            Level2 -> "Level 2 (3dp)"
            Level3 -> "Level 3 (6dp)"
            Level4 -> "Level 4 (8dp)"
            Level5 -> "Level 5 (12dp)"
        }

    companion object {
        val default = Level0
        val values = listOf(Level0, Level1, Level2, Level3, Level4, Level5)

        val Context.flowTopBarTonalElevation: Flow<FlowTopBarTonalElevationPreference>
            get() = this.dataStore.data.map {
                when (it[DataStoreKeys.FlowTopBarTonalElevation.key]) {
                    0 -> Level0
                    1 -> Level1
                    3 -> Level2
                    6 -> Level3
                    8 -> Level4
                    12 -> Level5
                    else -> default
                }
            }
    }
}