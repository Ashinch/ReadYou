package me.ash.reader.data.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "account")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo
    var name: String,
    @ColumnInfo
    var type: Int,
    @ColumnInfo
    var updateAt: Date? = null,
) {
    object Type {
        const val LOCAL = 1
        const val FRESH_RSS = 2
    }
}