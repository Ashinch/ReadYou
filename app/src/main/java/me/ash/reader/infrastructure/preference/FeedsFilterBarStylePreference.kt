package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.feedsFilterBarStyle
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFeedsFilterBarStyle =
    compositionLocalOf<FeedsFilterBarStylePreference> { FeedsFilterBarStylePreference.default }

sealed class FeedsFilterBarStylePreference(val value: Int) : Preference() {
    object Icon : FeedsFilterBarStylePreference(0)
    object IconLabel : FeedsFilterBarStylePreference(1)
    object IconLabelOnlySelected : FeedsFilterBarStylePreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.feedsFilterBarStyle,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Icon -> context.getString(R.string.icons)
            IconLabel -> context.getString(R.string.icons_and_labels)
            IconLabelOnlySelected -> context.getString(R.string.icons_and_label_only_selected)
        }

    companion object {

        val default = Icon
        val values = listOf(Icon, IconLabel, IconLabelOnlySelected)

        fun fromPreferences(preferences: Preferences): FeedsFilterBarStylePreference =
            when (preferences[DataStoreKey.keys[feedsFilterBarStyle]?.key as Preferences.Key<Int>]) {
                0 -> Icon
                1 -> IconLabel
                2 -> IconLabelOnlySelected
                else -> default
            }
    }
}
