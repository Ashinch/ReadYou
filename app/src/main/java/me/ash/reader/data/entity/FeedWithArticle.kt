package me.ash.reader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FeedWithArticle(
    @Embedded
    val feed: Feed,
    @Relation(parentColumn = "id", entityColumn = "feedId")
    val articles: List<Article>
)
