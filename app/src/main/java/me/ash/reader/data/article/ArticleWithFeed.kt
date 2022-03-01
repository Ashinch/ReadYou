package me.ash.reader.data.article

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.feed.Feed

data class ArticleWithFeed(
    @Embedded
    val article: Article,
    @Relation(parentColumn = "feedId", entityColumn = "id")
    val feed: Feed,
)
