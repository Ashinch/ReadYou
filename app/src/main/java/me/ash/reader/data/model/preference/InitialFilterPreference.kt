package me.ash.reader.data.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class InitialFilterPreference(val value: Int) : Preference() {
    object Starred : InitialFilterPreference(0)
    object Unread : InitialFilterPreference(1)
    object All : InitialFilterPreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.InitialFilter,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Starred -> context.getString(R.string.starred)
            Unread -> context.getString(R.string.unread)
            All -> context.getString(R.string.all)
        }

    companion object {

        val default = All
        val values = listOf(Starred, Unread, All)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.InitialFilter.key]) {
                0 -> Starred
                1 -> Unread
                2 -> All
                else -> default
            }
    }
}
