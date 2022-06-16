package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.model.article.Article
import me.ash.reader.data.model.feed.Feed
import me.ash.reader.data.model.feed.FeedWithArticle
import me.ash.reader.data.model.group.Group
import me.ash.reader.data.module.DefaultDispatcher
import me.ash.reader.data.module.IODispatcher
import me.ash.reader.data.repository.SyncWorker.Companion.setIsSyncing
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.spacerDollar
import java.util.*
import javax.inject.Inject

class LocalRssRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
    private val notificationHelper: NotificationHelper,
    private val accountDao: AccountDao,
    private val groupDao: GroupDao,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    workManager: WorkManager,
) : AbstractRssRepository(
    context, accountDao, articleDao, groupDao,
    feedDao, workManager, ioDispatcher, defaultDispatcher
) {

    override suspend fun updateArticleInfo(article: Article) {
        articleDao.update(article)
    }

    override suspend fun subscribe(feed: Feed, articles: List<Article>) {
        feedDao.insert(feed)
        articleDao.insertList(articles.map {
            it.copy(feedId = feed.id)
        })
    }

    override suspend fun addGroup(name: String): String {
        context.currentAccountId.let { accountId ->
            return accountId.spacerDollar(UUID.randomUUID().toString()).also {
                groupDao.insert(
                    Group(
                        id = it,
                        name = name,
                        accountId = accountId
                    )
                )
            }
        }
    }

    override suspend fun sync(coroutineWorker: CoroutineWorker): ListenableWorker.Result =
        supervisorScope {
            val preTime = System.currentTimeMillis()
            val accountId = context.currentAccountId
            feedDao.queryAll(accountId)
                .also { coroutineWorker.setProgress(setIsSyncing(true)) }
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
            coroutineWorker.setProgress(setIsSyncing(false))
            ListenableWorker.Result.success()
        }

    override suspend fun markAsRead(
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

            feedId != null -> {
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

    private suspend fun syncFeed(feed: Feed): FeedWithArticle {
        val latest = articleDao.queryLatestByFeedId(context.currentAccountId, feed.id)
        val articles = rssHelper.queryRssXml(feed, latest?.link)
//        try {
//            if (feed.icon == null && !articles.isNullOrEmpty()) {
//                rssHelper.queryRssIcon(feedDao, feed, articles.first().link)
//            }
//        } catch (e: Exception) {
//            Log.e("RLog", "queryRssIcon[${feed.name}]: ${e.message}")
//            return FeedWithArticle(
//                feed = feed.apply { isNotification = false },
//                articles = listOf()
//            )
//        }
        return FeedWithArticle(
            feed = feed.apply { isNotification = feed.isNotification && articles.isNotEmpty() },
            articles = articles
        )
    }
}
