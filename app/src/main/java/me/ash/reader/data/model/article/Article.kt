package me.ash.reader.data.model.article

import androidx.room.*
import me.ash.reader.data.model.feed.Feed
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
    var id: String,
    @ColumnInfo
    var date: Date,
    @ColumnInfo
    var title: String,
    @ColumnInfo
    var author: String? = null,
    @ColumnInfo
    var rawDescription: String,
    @ColumnInfo
    var shortDescription: String,
    @ColumnInfo
    var fullContent: String? = null,
    @ColumnInfo
    var img: String? = null,
    @ColumnInfo
    var link: String,
    @ColumnInfo(index = true)
    var feedId: String,
    @ColumnInfo(index = true)
    var accountId: Int,
    @ColumnInfo(defaultValue = "true")
    var isUnread: Boolean = true,
    @ColumnInfo(defaultValue = "false")
    var isStarred: Boolean = false,
    @ColumnInfo(defaultValue = "false")
    var isReadLater: Boolean = false,
) {

    @Ignore
    var dateString: String? = null
}
