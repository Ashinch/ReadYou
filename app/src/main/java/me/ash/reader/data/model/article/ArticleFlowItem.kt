package me.ash.reader.data.model.article

import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import me.ash.reader.data.repository.StringsRepository

/**
 * Provide paginated and inserted separator data types for article list view.
 *
 * @see me.ash.reader.ui.page.home.flow.ArticleList
 */
sealed class ArticleFlowItem {

    /**
     * The [Article] item.
     *
     * @see me.ash.reader.ui.page.home.flow.ArticleItem
     */
    class Article(val articleWithFeed: ArticleWithFeed) : ArticleFlowItem()

    /**
     * The feed publication date separator between [Article] items.
     *
     * @see me.ash.reader.ui.page.home.flow.StickyHeader
     */
    class Date(val date: String, val showSpacer: Boolean) : ArticleFlowItem()
}

/**
 * Mapping [ArticleWithFeed] list to [ArticleFlowItem] list.
 */
fun PagingData<ArticleWithFeed>.mapPagingFlowItem(stringsRepository: StringsRepository): PagingData<ArticleFlowItem> =
    map {
        ArticleFlowItem.Article(it.apply {
            article.dateString = stringsRepository.formatAsString(
                date = article.date,
                onlyHourMinute = true
            )
        })
    }.insertSeparators { before, after ->
        val beforeDate =
            stringsRepository.formatAsString(before?.articleWithFeed?.article?.date)
        val afterDate =
            stringsRepository.formatAsString(after?.articleWithFeed?.article?.date)
        if (beforeDate != afterDate) {
            afterDate?.let { ArticleFlowItem.Date(it, beforeDate != null) }
        } else {
            null
        }
    }
