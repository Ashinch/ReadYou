package me.ash.reader.data.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import me.ash.reader.data.model.preference.SyncBlockList
import me.ash.reader.data.model.preference.SyncBlockListPreference

/**
 * Provide [TypeConverter] of [SyncBlockListPreference] for [RoomDatabase].
 */
class SyncBlockListConverters {

    @TypeConverter
    fun toBlockList(syncBlockList: String): SyncBlockList {
        if (!syncBlockList.contains(",")) return emptyList()
        return syncBlockList.split(",")
    }

    @TypeConverter
    fun fromBlockList(syncBlockList: SyncBlockList?): String {
        return syncBlockList?.joinToString { "$it," } ?: ""
    }
}
