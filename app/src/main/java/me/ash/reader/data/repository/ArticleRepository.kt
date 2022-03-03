package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import me.ash.reader.DataStoreKeys
import me.ash.reader.data.article.Article
import me.ash.reader.data.article.ArticleDao
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.data.article.ImportantCount
import me.ash.reader.data.group.GroupDao
import me.ash.reader.data.group.GroupWithFeed
import me.ash.reader.dataStore
import me.ash.reader.get
import javax.inject.Inject

class ArticleRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val articleDao: ArticleDao,
    private val groupDao: GroupDao,
) {

    fun pullFeeds(): Flow<MutableList<GroupWithFeed>> {
        return groupDao.queryAllGroupWithFeed(
            context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        )
    }

    fun pullArticles(
        groupId: Int? = null,
        feedId: Int? = null,
        isStarred: Boolean = false,
        isUnread: Boolean = false,
    ): PagingSource<Int, ArticleWithFeed> {
        val accountId = context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        Log.i(
            "RLog",
            "pullArticles: accountId: ${accountId}, groupId: ${groupId}, feedId: ${feedId}, isStarred: ${isStarred}, isUnread: ${isUnread}"
        )
        return when {
            groupId != null -> when {
                isStarred -> articleDao
                    .queryArticleWithFeedByGroupIdWhenIsStarred(accountId, groupId, isStarred)
                isUnread -> articleDao
                    .queryArticleWithFeedByGroupIdWhenIsUnread(accountId, groupId, isUnread)
                else -> articleDao.queryArticleWithFeedByGroupIdWhenIsAll(accountId, groupId)
            }
            feedId != null -> when {
                isStarred -> articleDao
                    .queryArticleWithFeedByFeedIdWhenIsStarred(accountId, feedId, isStarred)
                isUnread -> articleDao
                    .queryArticleWithFeedByFeedIdWhenIsUnread(accountId, feedId, isUnread)
                else -> articleDao.queryArticleWithFeedByFeedIdWhenIsAll(accountId, feedId)
            }
            else -> when {
                isStarred -> articleDao
                    .queryArticleWithFeedWhenIsStarred(accountId, isStarred)
                isUnread -> articleDao
                    .queryArticleWithFeedWhenIsUnread(accountId, isUnread)
                else -> articleDao.queryArticleWithFeedWhenIsAll(accountId)
            }
        }
    }

    fun pullImportant(
        isStarred: Boolean = false,
        isUnread: Boolean = false,
    ): Flow<List<ImportantCount>> {
        val accountId = context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        Log.i(
            "RLog",
            "pullImportant: accountId: ${accountId}, isStarred: ${isStarred}, isUnread: ${isUnread}"
        )
        return when {
            isStarred -> articleDao
                .queryImportantCountWhenIsStarred(accountId, isStarred)
            isUnread -> articleDao
                .queryImportantCountWhenIsUnread(accountId, isUnread)
            else -> articleDao.queryImportantCountWhenIsAll(accountId)
        }
    }

    suspend fun updateArticleInfo(article: Article) {
        articleDao.update(article)
    }

    suspend fun findArticleById(id: Int): ArticleWithFeed? {
        return articleDao.queryById(id)
    }
}