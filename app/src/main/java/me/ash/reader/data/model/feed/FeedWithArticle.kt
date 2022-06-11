package me.ash.reader.data.model.feed

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.model.article.Article

data class FeedWithArticle(
    @Embedded
    var feed: Feed,
    @Relation(parentColumn = "id", entityColumn = "feedId")
    var articles: List<Article>,
)
