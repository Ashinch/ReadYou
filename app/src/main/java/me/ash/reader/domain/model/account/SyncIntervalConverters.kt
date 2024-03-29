package me.ash.reader.domain.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import me.ash.reader.infrastructure.preference.SyncIntervalPreference

/**
 * Provide [TypeConverter] of [SyncIntervalPreference] for [RoomDatabase].
 */
class SyncIntervalConverters {

    @TypeConverter
    fun toSyncInterval(syncInterval: Long): SyncIntervalPreference {
        return SyncIntervalPreference.values.find { it.value == syncInterval } ?: SyncIntervalPreference.default
    }

    @TypeConverter
    fun fromSyncInterval(syncInterval: SyncIntervalPreference): Long {
        return syncInterval.value
    }
}
