package me.ash.reader.data.model.article

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/**
 * Data class for article metadata processing only.
 */

data class ArticleMeta(
    @PrimaryKey
    var id: String,
    @ColumnInfo
    var isUnread: Boolean = true,
    @ColumnInfo
    var isStarred: Boolean = false,
)
