package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.rometools.rome.feed.synd.SyndFeed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.supervisorScope
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.FeedWithArticle
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.preference.KeepArchivedPreference
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.spacerDollar
import java.util.*

abstract class AbstractRssRepository(
    private val context: Context,
    private val accountDao: AccountDao,
    private val articleDao: ArticleDao,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val workManager: WorkManager,
    private val rssHelper: RssHelper,
    private val notificationHelper: NotificationHelper,
    private val dispatcherIO: CoroutineDispatcher,
    private val dispatcherDefault: CoroutineDispatcher,
) {

    open val addSubscription: Boolean = true
    open val moveSubscription: Boolean = true
    open val deleteSubscription: Boolean = true
    open val updateSubscription: Boolean = true

    open suspend fun validCredentials(account: Account): Boolean = true

    open suspend fun clearAuthorization() {}

    open suspend fun subscribe(
        feedLink: String, searchedFeed: SyndFeed, groupId: String,
        isNotification: Boolean, isFullContent: Boolean
    ) {
        val accountId = context.currentAccountId
        val feed = Feed(
            id = accountId.spacerDollar(UUID.randomUUID().toString()),
            name = searchedFeed.title.decodeHTML()!!,
            url = feedLink,
            groupId = groupId,
            accountId = accountId,
            icon = searchedFeed.icon?.link
        )
        val articles = searchedFeed.entries.map { rssHelper.buildArticleFromSyndEntry(feed, accountId, it) }
        feedDao.insert(feed)
        articleDao.insertList(articles.map {
            it.copy(feedId = feed.id)
        })
    }

    open suspend fun addGroup(
        destFeed: Feed?,
        newGroupName: String
    ): String {
        context.currentAccountId.let { accountId ->
            return accountId.spacerDollar(UUID.randomUUID().toString()).also {
                groupDao.insert(
                    Group(
                        id = it,
                        name = newGroupName,
                        accountId = accountId
                    )
                )
            }
        }
    }

    open suspend fun sync(coroutineWorker: CoroutineWorker): ListenableWorker.Result =
        supervisorScope {
            coroutineWorker.setProgress(SyncWorker.setIsSyncing(true))
            val preTime = System.currentTimeMillis()
            val accountId = context.currentAccountId
            feedDao.queryAll(accountId)
                .chunked(16)
                .forEach {
                    it.map { feed -> async { syncFeed(feed) } }
                        .awaitAll()
                        .forEach {
                            if (it.feed.isNotification) {
                                notificationHelper.notify(it.apply {
                                    articles = articleDao.insertListIfNotExist(it.articles)
                                })
                            } else {
                                articleDao.insertListIfNotExist(it.articles)
                            }
                        }
                }

            Log.i("RlOG", "onCompletion: ${System.currentTimeMillis() - preTime}")
            accountDao.queryById(accountId)?.let { account ->
                accountDao.update(account.apply { updateAt = Date() })
            }
            coroutineWorker.setProgress(SyncWorker.setIsSyncing(false))
            ListenableWorker.Result.success()
        }

    open suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        val accountId = context.currentAccountId
        when {
            groupId != null -> {
                articleDao.markAllAsReadByGroupId(
                    accountId = accountId,
                    groupId = groupId,
                    isUnread = isUnread,
                    before = before ?: Date(Long.MAX_VALUE)
                )
            }

            feedId != null && articleId == null -> {
                articleDao.markAllAsReadByFeedId(
                    accountId = accountId,
                    feedId = feedId,
                    isUnread = isUnread,
                    before = before ?: Date(Long.MAX_VALUE)
                )
            }

            articleId != null -> {
                articleDao.markAsReadByArticleId(accountId, articleId, isUnread)
            }

            else -> {
                articleDao.markAllAsRead(accountId, isUnread, before ?: Date(Long.MAX_VALUE))
            }
        }
    }

    open suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        val accountId = context.currentAccountId
        articleDao.markAsStarredByArticleId(accountId, articleId, isStarred)
    }

    private suspend fun syncFeed(feed: Feed): FeedWithArticle {
        val latest = articleDao.queryLatestByFeedId(context.currentAccountId, feed.id)
        val articles = rssHelper.queryRssXml(feed, latest?.link)
        if (feed.icon == null) {
            val iconLink = rssHelper.queryRssIconLink(feed.url)
            if (iconLink != null) {
                rssHelper.saveRssIcon(feedDao, feed, iconLink)
            }
        }
        return FeedWithArticle(
            feed = feed.apply { isNotification = feed.isNotification && articles.isNotEmpty() },
            articles = articles
        )
    }

    suspend fun clearKeepArchivedArticles() {
        accountDao.queryById(context.currentAccountId)!!
            .takeIf { it.keepArchived != KeepArchivedPreference.Always }
            ?.let {
                articleDao.deleteAllArchivedBeforeThan(it.id!!,
                    Date(System.currentTimeMillis() - it.keepArchived.value))
            }
    }

    fun cancelSync() {
        workManager.cancelAllWork()
    }

    suspend fun doSync(isOnStart: Boolean = false) {
        workManager.cancelAllWork()
        accountDao.queryById(context.currentAccountId)?.let {
            if (isOnStart) {
                if (it.syncOnStart.value) {
                    SyncWorker.enqueueOneTimeWork(workManager)
                }
                if (it.syncInterval.value != SyncIntervalPreference.Manually.value) {
                    SyncWorker.enqueuePeriodicWork(
                        workManager = workManager,
                        syncInterval = it.syncInterval,
                        syncOnlyWhenCharging = it.syncOnlyWhenCharging,
                        syncOnlyOnWiFi = it.syncOnlyOnWiFi,
                    )
                }
            } else {
                SyncWorker.enqueueOneTimeWork(workManager)
                if (it.syncInterval.value != SyncIntervalPreference.Manually.value) {
                    SyncWorker.enqueuePeriodicWork(
                        workManager = workManager,
                        syncInterval = it.syncInterval,
                        syncOnlyWhenCharging = it.syncOnlyWhenCharging,
                        syncOnlyOnWiFi = it.syncOnlyOnWiFi,
                    )
                }
            }
        }
    }

    fun pullGroups(): Flow<MutableList<Group>> =
        groupDao.queryAllGroup(context.currentAccountId).flowOn(dispatcherIO)

    fun pullFeeds(): Flow<MutableList<GroupWithFeed>> =
        groupDao.queryAllGroupWithFeedAsFlow(context.currentAccountId).flowOn(dispatcherIO)

    fun pullArticles(
        groupId: String?,
        feedId: String?,
        isStarred: Boolean,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed> {
        val accountId = context.currentAccountId
        Log.i(
            "RLog",
            "pullArticles: accountId: ${accountId}, groupId: ${groupId}, feedId: ${feedId}, isStarred: ${isStarred}, isUnread: ${isUnread}"
        )
        return when {
            groupId != null -> when {
                isStarred -> articleDao.queryArticleWithFeedByGroupIdWhenIsStarred(accountId, groupId, true)
                isUnread -> articleDao.queryArticleWithFeedByGroupIdWhenIsUnread(accountId, groupId, true)
                else -> articleDao.queryArticleWithFeedByGroupIdWhenIsAll(accountId, groupId)
            }

            feedId != null -> when {
                isStarred -> articleDao.queryArticleWithFeedByFeedIdWhenIsStarred(accountId, feedId, true)
                isUnread -> articleDao.queryArticleWithFeedByFeedIdWhenIsUnread(accountId, feedId, true)
                else -> articleDao.queryArticleWithFeedByFeedIdWhenIsAll(accountId, feedId)
            }

            else -> when {
                isStarred -> articleDao.queryArticleWithFeedWhenIsStarred(accountId, true)
                isUnread -> articleDao.queryArticleWithFeedWhenIsUnread(accountId, true)
                else -> articleDao.queryArticleWithFeedWhenIsAll(accountId)
            }
        }
    }

    fun pullImportant(
        isStarred: Boolean,
        isUnread: Boolean,
    ): Flow<Map<String, Int>> {
        val accountId = context.currentAccountId
        Log.i(
            "RLog",
            "pullImportant: accountId: ${accountId}, isStarred: ${isStarred}, isUnread: ${isUnread}"
        )
        return when {
            isStarred -> articleDao.queryImportantCountWhenIsStarred(accountId, true)
            isUnread -> articleDao.queryImportantCountWhenIsUnread(accountId, true)
            else -> articleDao.queryImportantCountWhenIsAll(accountId)
        }.mapLatest {
            mapOf(
                // Groups
                *(it.groupBy { it.groupId }.map { it.key to it.value.sumOf { it.important } }.toTypedArray()),
                // Feeds
                *(it.map { it.feedId to it.important }.toTypedArray()),
                // All summary
                "sum" to it.sumOf { it.important }
            )
        }.flowOn(dispatcherDefault)
    }

    suspend fun findFeedById(id: String): Feed? = feedDao.queryById(id)

    suspend fun findGroupById(id: String): Group? = groupDao.queryById(id)

    suspend fun findArticleById(id: String): ArticleWithFeed? = articleDao.queryById(id)

    suspend fun isFeedExist(url: String): Boolean = feedDao.queryByLink(context.currentAccountId, url).isNotEmpty()

    open suspend fun renameGroup(group: Group) {
        groupDao.update(group)
    }

    open suspend fun renameFeed(feed: Feed) {
       updateFeed(feed)
    }

    open suspend fun moveFeed(originGroupId: String, feed: Feed) {
       updateFeed(feed)
    }

    open suspend fun changeFeedUrl(feed: Feed) {
       updateFeed(feed)
    }

    internal suspend fun updateFeed(feed: Feed) {
        feedDao.update(feed)
    }

    open suspend fun deleteGroup(group: Group) {
        deleteArticles(group = group)
        feedDao.deleteByGroupId(context.currentAccountId, group.id)
        groupDao.delete(group)
    }

    open suspend fun deleteFeed(feed: Feed) {
        deleteArticles(feed = feed)
        feedDao.delete(feed)
    }

    suspend fun deleteArticles(group: Group? = null, feed: Feed? = null) {
        when {
            group != null -> articleDao.deleteByGroupId(context.currentAccountId, group.id)
            feed != null -> articleDao.deleteByFeedId(context.currentAccountId, feed.id)
        }
    }

    suspend fun deleteAccountArticles(accountId: Int) {
        articleDao.deleteByAccountId(accountId)
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
        groupId: String?,
        feedId: String?,
        isStarred: Boolean,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed> {
        val accountId = context.currentAccountId
        Log.i(
            "RLog",
            "searchArticles: content: ${content}, accountId: ${accountId}, groupId: ${groupId}, feedId: ${feedId}, isStarred: ${isStarred}, isUnread: ${isUnread}"
        )
        return when {
            groupId != null -> when {
                isStarred -> articleDao.searchArticleByGroupIdWhenIsStarred(accountId, content, groupId, true)
                isUnread -> articleDao.searchArticleByGroupIdWhenIsUnread(accountId, content, groupId, true)
                else -> articleDao.searchArticleByGroupIdWhenAll(accountId, content, groupId)
            }

            feedId != null -> when {
                isStarred -> articleDao.searchArticleByFeedIdWhenIsStarred(accountId, content, feedId, true)
                isUnread -> articleDao.searchArticleByFeedIdWhenIsUnread(accountId, content, feedId, true)
                else -> articleDao.searchArticleByFeedIdWhenAll(accountId, content, feedId)
            }

            else -> when {
                isStarred -> articleDao.searchArticleWhenIsStarred(accountId, content, true)
                isUnread -> articleDao.searchArticleWhenIsUnread(accountId, content, true)
                else -> articleDao.searchArticleWhenAll(accountId, content)
            }
        }
    }
}
