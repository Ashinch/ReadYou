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
import kotlinx.coroutines.withContext
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.FeedWithArticle
import me.ash.reader.data.entity.Group
import me.ash.reader.data.module.DispatcherDefault
import me.ash.reader.data.module.DispatcherIO
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
    @DispatcherDefault
    private val dispatcherDefault: CoroutineDispatcher,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
    workManager: WorkManager,
) : AbstractRssRepository(
    context, accountDao, articleDao, groupDao,
    feedDao, workManager, dispatcherIO
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

    override suspend fun sync(coroutineWorker: CoroutineWorker): ListenableWorker.Result {
        return withContext(dispatcherDefault) {
            val preTime = System.currentTimeMillis()
            val accountId = context.currentAccountId
            feedDao.queryAll(accountId)
                .also { coroutineWorker.setProgress(setIsSyncing(true)) }
                .map { feed -> async { syncFeed(feed) } }
                .awaitAll()
                .forEach {
                    if (it.isNotify) {
                        notificationHelper.notify(
                            FeedWithArticle(
                                it.feedWithArticle.feed,
                                articleDao.insertListIfNotExist(it.feedWithArticle.articles)
                            )
                        )
                    } else {
                        articleDao.insertListIfNotExist(it.feedWithArticle.articles)
                    }
                }
            Log.i("RlOG", "onCompletion: ${System.currentTimeMillis() - preTime}")
            accountDao.queryById(accountId)?.let { account ->
                accountDao.update(
                    account.apply {
                        updateAt = Date()
                    }
                )
            }
            coroutineWorker.setProgress(setIsSyncing(false))
            ListenableWorker.Result.success()
        }
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

    data class ArticleNotify(
        val feedWithArticle: FeedWithArticle,
        val isNotify: Boolean,
    )

    private suspend fun syncFeed(feed: Feed): ArticleNotify {
        val latest = articleDao.queryLatestByFeedId(context.currentAccountId, feed.id)
        val articles: List<Article>?
        try {
            articles = rssHelper.queryRssXml(feed, latest?.link)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "queryRssXml[${feed.name}]: ${e.message}")
            return ArticleNotify(FeedWithArticle(feed, listOf()), false)
        }
        try {
//            if (feed.icon == null && !articles.isNullOrEmpty()) {
//                rssHelper.queryRssIcon(feedDao, feed, articles.first().link)
//            }
        } catch (e: Exception) {
            Log.e("RLog", "queryRssIcon[${feed.name}]: ${e.message}")
            return ArticleNotify(FeedWithArticle(feed, listOf()), false)
        }
        return ArticleNotify(
            feedWithArticle = FeedWithArticle(feed, articles),
            isNotify = articles.isNotEmpty() && feed.isNotification
        )
    }
}