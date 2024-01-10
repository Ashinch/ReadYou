package me.ash.reader.data.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import me.ash.reader.data.model.preference.SyncOnStartPreference

/**
 * Provide [TypeConverter] of [SyncOnStartPreference] for [RoomDatabase].
 */
class SyncOnStartConverters {

    @TypeConverter
    fun toSyncOnStart(syncOnStart: Boolean): SyncOnStartPreference {
        return SyncOnStartPreference.values.find { it.value == syncOnStart } ?: SyncOnStartPreference.default
    }

    @TypeConverter
    fun fromSyncOnStart(syncOnStart: SyncOnStartPreference): Boolean {
        return syncOnStart.value
    }
}
