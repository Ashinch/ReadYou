package me.ash.reader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ArticleWithFeed(
    @Embedded
    var article: Article,
    @Relation(parentColumn = "feedId", entityColumn = "id")
    var feed: Feed,
)
