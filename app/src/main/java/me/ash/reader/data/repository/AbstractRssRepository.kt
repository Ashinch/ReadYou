package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.paging.PagingSource
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.entity.*
import me.ash.reader.data.source.RssNetworkDataSource
import me.ash.reader.ui.ext.currentAccountId
import java.util.*
import java.util.concurrent.TimeUnit

abstract class AbstractRssRepository constructor(
    private val context: Context,
    private val accountDao: AccountDao,
    private val articleDao: ArticleDao,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val rssNetworkDataSource: RssNetworkDataSource,
    private val workManager: WorkManager,
    private val dispatcherIO: CoroutineDispatcher,
) {
    abstract suspend fun updateArticleInfo(article: Article)

    abstract suspend fun subscribe(feed: Feed, articles: List<Article>)

    abstract suspend fun addGroup(name: String): String

    abstract suspend fun sync(coroutineWorker: CoroutineWorker): ListenableWorker.Result

    abstract suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    )

    fun doSync() {
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            SyncWorker.repeatingRequest
        )
    }

    fun pullGroups(): Flow<MutableList<Group>> {
        return groupDao.queryAllGroup(context.currentAccountId).flowOn(dispatcherIO)
    }

    fun pullFeeds(): Flow<MutableList<GroupWithFeed>> {
        return groupDao.queryAllGroupWithFeedAsFlow(context.currentAccountId).flowOn(dispatcherIO)
    }

    fun pullArticles(
        groupId: String? = null,
        feedId: String? = null,
        isStarred: Boolean = false,
        isUnread: Boolean = false,
    ): PagingSource<Int, ArticleWithFeed> {
        val accountId = context.currentAccountId
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
        val accountId = context.currentAccountId
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
        }.flowOn(dispatcherIO)
    }

    suspend fun findFeedById(id: String): Feed? {
        return feedDao.queryById(id)
    }

    suspend fun findGroupById(id: String): Group? {
        return groupDao.queryById(id)
    }

    suspend fun findArticleById(id: String): ArticleWithFeed? {
        return articleDao.queryById(id)
    }

    suspend fun isFeedExist(url: String): Boolean {
        return feedDao.queryByLink(context.currentAccountId, url).isNotEmpty()
    }

    fun peekWork(): String {
        return workManager.getWorkInfosByTag("sync").get().size.toString()
    }

    suspend fun updateGroup(group: Group) {
        groupDao.update(group)
    }

    suspend fun updateFeed(feed: Feed) {
        feedDao.update(feed)
    }

    suspend fun deleteGroup(group: Group) {
        articleDao.deleteByGroupId(context.currentAccountId, group.id)
        feedDao.deleteByGroupId(context.currentAccountId, group.id)
        groupDao.delete(group)
    }

    suspend fun deleteFeed(feed: Feed) {
        articleDao.deleteByFeedId(context.currentAccountId, feed.id)
        feedDao.delete(feed)
    }

    suspend fun groupParseFullContent(group: Group, isFullContent: Boolean) {
        feedDao.updateIsFullContentByGroupId(context.currentAccountId, group.id, isFullContent)
    }

    suspend fun groupAllowNotification(group: Group, isNotification: Boolean) {
        feedDao.updateIsNotificationByGroupId(context.currentAccountId, group.id, isNotification)
    }

    suspend fun groupMoveToTargetGroup(group: Group, targetGroup: Group) {
        feedDao.updateTargetGroupIdByGroupId(context.currentAccountId, group.id, targetGroup.id)
    }

    fun searchArticles(
        content: String,
        groupId: String? = null,
        feedId: String? = null,
        isStarred: Boolean = false,
        isUnread: Boolean = false,
    ): PagingSource<Int, ArticleWithFeed> {
        val accountId = context.currentAccountId
        Log.i(
            "RLog",
            "searchArticles: content: ${content}, accountId: ${accountId}, groupId: ${groupId}, feedId: ${feedId}, isStarred: ${isStarred}, isUnread: ${isUnread}"
        )
        return when {
            groupId != null -> when {
                isStarred -> articleDao
                    .searchArticleByGroupIdWhenIsStarred(accountId, content, groupId, isStarred)
                isUnread -> articleDao
                    .searchArticleByGroupIdWhenIsUnread(accountId, content, groupId, isUnread)
                else -> articleDao.searchArticleByGroupIdWhenAll(accountId, content, groupId)
            }
            feedId != null -> when {
                isStarred -> articleDao
                    .searchArticleByFeedIdWhenIsStarred(accountId, content, feedId, isStarred)
                isUnread -> articleDao
                    .searchArticleByFeedIdWhenIsUnread(accountId, content, feedId, isUnread)
                else -> articleDao.searchArticleByFeedIdWhenAll(accountId, content, feedId)
            }
            else -> when {
                isStarred -> articleDao.searchArticleWhenIsStarred(accountId, content, isStarred)
                isUnread -> articleDao.searchArticleWhenIsUnread(accountId, content, isUnread)
                else -> articleDao.searchArticleWhenAll(accountId, content)
            }
        }
    }
}

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rssRepository: RssRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("RLog", "doWork: ")
        return rssRepository.get().sync(this)
    }

    companion object {
        const val WORK_NAME = "article.sync"

        val UUID: UUID

        val repeatingRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .build()
        ).addTag(WORK_NAME).build().also {
            UUID = it.id
        }

        fun setIsSyncing(boolean: Boolean) = workDataOf("isSyncing" to boolean)
        fun Data.getIsSyncing(): Boolean = getBoolean("isSyncing", false)
    }
}