package me.ash.reader.data.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ReduceAnimationPreference(val value: Boolean) : Preference() {
    object ON : ReduceAnimationPreference(true)
    object OFF : ReduceAnimationPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.ReduceAnimation,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            ON -> context.getString(R.string.on)
            OFF -> context.getString(R.string.off)
        }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.ReduceAnimation.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReduceAnimationPreference.not(): ReduceAnimationPreference =
    when (value) {
        true -> ReduceAnimationPreference.OFF
        false -> ReduceAnimationPreference.ON
    }
