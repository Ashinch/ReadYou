package me.ash.reader.data.article

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import me.ash.reader.data.feed.Feed
import java.util.*

@Entity(
    tableName = "article",
    foreignKeys = [ForeignKey(
        entity = Feed::class,
        parentColumns = ["id"],
        childColumns = ["feedId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class Article(
    @PrimaryKey
    val id: String,
    @ColumnInfo
    val date: Date,
    @ColumnInfo
    val title: String,
    @ColumnInfo
    val author: String? = null,
    @ColumnInfo
    var rawDescription: String,
    @ColumnInfo
    var shortDescription: String,
    @ColumnInfo
    var fullContent: String? = null,
    @ColumnInfo
    val link: String,
    @ColumnInfo(index = true)
    val feedId: String,
    @ColumnInfo(index = true)
    val accountId: Int,
    @ColumnInfo(defaultValue = "true")
    var isUnread: Boolean = true,
    @ColumnInfo(defaultValue = "false")
    var isStarred: Boolean = false,
)