package me.ash.reader.domain.model.feed

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.domain.model.article.Article

/**
 * A [feed] contains many [articles].
 */
data class FeedWithArticle(
    @Embedded
    var feed: Feed,
    @Relation(parentColumn = "id", entityColumn = "feedId")
    var articles: List<Article>,
)
