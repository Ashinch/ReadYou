package me.ash.reader.data.feed

import androidx.room.*
import me.ash.reader.data.group.Group

@Entity(
    tableName = "feed",
    foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
    )],
)
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo
    val name: String,
    @ColumnInfo
    var icon: String? = null,
    @ColumnInfo
    val url: String,
    @ColumnInfo(index = true)
    var groupId: Int,
    @ColumnInfo(index = true)
    val accountId: Int,
    @ColumnInfo(defaultValue = "false")
    var isFullContent: Boolean = false,
) {
    @Ignore
    var important: Int? = 0
}
