package me.ash.reader.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "account")
data class Account(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo
    var name: String,
    @ColumnInfo
    var type: Int,
    @ColumnInfo
    var updateAt: Date? = null,
) {
    object Type {
        const val LOCAL = 1
        const val FEVER = 2
        const val GOOGLE_READER = 3
    }
}