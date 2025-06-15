package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.FeedWithArticle
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.ui.ext.currentAccountId
import java.util.Date
import javax.inject.Inject

private const val TAG = "LocalRssService"

class LocalRssService @Inject constructor(
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
    private val workManager: WorkManager,
    accountService: AccountService,
) : AbstractRssRepository(
    accountDao,
    articleDao,
    groupDao,
    feedDao,
    workManager,
    rssHelper,
    notificationHelper,
    ioDispatcher,
    defaultDispatcher,
    accountService
) {

    fun doSyncOneTime(feedId: String?, groupId: String?) {
        SyncWorker.enqueueOneTimeWork(
            workManager,
            workDataOf("feedId" to feedId, "groupId" to groupId)
        )
    }

    override suspend fun sync(feedId: String?, groupId: String?) = supervisorScope {
        val preTime = System.currentTimeMillis()
        val preDate = Date(preTime)
        val accountId = context.currentAccountId
        val semaphore = Semaphore(16)

        val feedsToSync = when {
            feedId != null -> listOfNotNull(feedDao.queryById(feedId))
            groupId != null -> feedDao.queryByGroupId(accountId, groupId)
            else -> feedDao.queryAll(accountId)
        }

        feedsToSync.mapIndexed { _, currentFeed ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    val archivedArticles =
                        feedDao.queryArchivedArticles(currentFeed.id).map { it.link }.toSet()
                    val fetchedFeed = syncFeed(currentFeed, preDate)
                    val fetchedArticles = fetchedFeed.articles.filterNot {
                        archivedArticles.contains(it.link)
                    }

                    val newArticles =
                        articleDao.insertListIfNotExist(
                            articles = fetchedArticles,
                            feed = currentFeed
                        )
                    if (currentFeed.isNotification && newArticles.isNotEmpty()) {
                        notificationHelper.notify(
                            fetchedFeed.copy(
                                articles = newArticles,
                                feed = currentFeed
                            )
                        )
                    }
                }
            }
        }.awaitAll()

        Log.i("RlOG", "onCompletion: ${System.currentTimeMillis() - preTime}")
        accountDao.queryById(accountId)?.let { account ->
            accountDao.update(account.apply { updateAt = Date() })
        }
        ListenableWorker.Result.success()
    }

    private suspend fun syncFeed(feed: Feed, preDate: Date = Date()): FeedWithArticle {
        val articles = rssHelper.queryRssXml(feed, "", preDate)
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
}
