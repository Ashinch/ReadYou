package me.ash.reader.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowFilterBarTonalElevationPreference(val value: Int) : Preference() {
    object Level0 : FlowFilterBarTonalElevationPreference(0)
    object Level1 : FlowFilterBarTonalElevationPreference(1)
    object Level2 : FlowFilterBarTonalElevationPreference(3)
    object Level3 : FlowFilterBarTonalElevationPreference(6)
    object Level4 : FlowFilterBarTonalElevationPreference(8)
    object Level5 : FlowFilterBarTonalElevationPreference(12)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowFilterBarTonalElevation,
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

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowFilterBarTonalElevation.key]) {
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