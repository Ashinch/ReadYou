package me.ash.reader.data.group

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "group")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo
    val name: String,
    @ColumnInfo(index = true)
    val accountId: Int,
) {
    @Ignore
    var important: Int? = 0
}