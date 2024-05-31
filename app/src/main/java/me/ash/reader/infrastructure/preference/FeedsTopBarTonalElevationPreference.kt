package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.constant.ElevationTokens
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.feedsTopBarTonalElevation
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFeedsTopBarTonalElevation =
    compositionLocalOf<FeedsTopBarTonalElevationPreference> { FeedsTopBarTonalElevationPreference.default }

sealed class FeedsTopBarTonalElevationPreference(val value: Int) : Preference() {
    object Level0 : FeedsTopBarTonalElevationPreference(ElevationTokens.Level0)
    object Level1 : FeedsTopBarTonalElevationPreference(ElevationTokens.Level1)
    object Level2 : FeedsTopBarTonalElevationPreference(ElevationTokens.Level2)
    object Level3 : FeedsTopBarTonalElevationPreference(ElevationTokens.Level3)
    object Level4 : FeedsTopBarTonalElevationPreference(ElevationTokens.Level4)
    object Level5 : FeedsTopBarTonalElevationPreference(ElevationTokens.Level5)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.feedsTopBarTonalElevation,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Level0 -> "Level 0 (${ElevationTokens.Level0}dp)"
            Level1 -> "Level 1 (${ElevationTokens.Level1}dp)"
            Level2 -> "Level 2 (${ElevationTokens.Level2}dp)"
            Level3 -> "Level 3 (${ElevationTokens.Level3}dp)"
            Level4 -> "Level 4 (${ElevationTokens.Level4}dp)"
            Level5 -> "Level 5 (${ElevationTokens.Level5}dp)"
        }

    companion object {

        val default = Level0
        val values = listOf(Level0, Level1, Level2, Level3, Level4, Level5)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[feedsTopBarTonalElevation]?.key as Preferences.Key<Int>]) {
                ElevationTokens.Level0 -> Level0
                ElevationTokens.Level1 -> Level1
                ElevationTokens.Level2 -> Level2
                ElevationTokens.Level3 -> Level3
                ElevationTokens.Level4 -> Level4
                ElevationTokens.Level5 -> Level5
                else -> default
            }
    }
}
