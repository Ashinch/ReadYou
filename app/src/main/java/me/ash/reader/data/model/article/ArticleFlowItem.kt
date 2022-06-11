package me.ash.reader.data.model.article

import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import me.ash.reader.data.repository.StringsRepository

sealed class ArticleFlowItem {
    class Article(val articleWithFeed: ArticleWithFeed) : ArticleFlowItem()
    class Date(val date: String, val showSpacer: Boolean) : ArticleFlowItem()
}

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
