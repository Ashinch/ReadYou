package me.ash.reader.data.feed

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.article.Article

data class FeedWithArticle(
    @Embedded
    val feed: Feed,
    @Relation(parentColumn = "id", entityColumn = "feedId")
    val articles: List<Article>
)
