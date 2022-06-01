package me.ash.reader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FeedWithArticle(
    @Embedded
    var feed: Feed,
    @Relation(parentColumn = "id", entityColumn = "feedId")
    var articles: List<Article>
)
