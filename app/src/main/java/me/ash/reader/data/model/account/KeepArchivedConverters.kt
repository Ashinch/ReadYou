package me.ash.reader.data.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import me.ash.reader.data.model.preference.KeepArchivedPreference

/**
 * Provide [TypeConverter] of [KeepArchivedPreference] for [RoomDatabase].
 */
class KeepArchivedConverters {

    @TypeConverter
    fun toKeepArchived(keepArchived: Long): KeepArchivedPreference {
        return KeepArchivedPreference.values.find { it.value == keepArchived } ?: KeepArchivedPreference.default
    }

    @TypeConverter
    fun fromKeepArchived(keepArchived: KeepArchivedPreference): Long {
        return keepArchived.value
    }
}
