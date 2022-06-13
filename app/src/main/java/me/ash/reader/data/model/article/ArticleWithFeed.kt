package me.ash.reader.data.model.article

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.model.feed.Feed

/**
 * An [article] contains a [feed].
 */
data class ArticleWithFeed(
    @Embedded
    var article: Article,
    @Relation(parentColumn = "feedId", entityColumn = "id")
    var feed: Feed,
)
