package me.ash.reader.data.model.group

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "group")
data class Group(
    @PrimaryKey
    var id: String,
    @ColumnInfo
    var name: String,
    @ColumnInfo(index = true)
    var accountId: Int,
) {

    @Ignore
    var important: Int? = 0
}
