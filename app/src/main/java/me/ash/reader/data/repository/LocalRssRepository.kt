package me.ash.reader.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.ash.reader.MainActivity
import me.ash.reader.R
import me.ash.reader.currentAccountId
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.article.Article
import me.ash.reader.data.article.ArticleDao
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupDao
import me.ash.reader.data.source.RssNetworkDataSource
import me.ash.reader.ui.page.common.ExtraName
import me.ash.reader.ui.page.common.NotificationGroupName
import java.util.*
import javax.inject.Inject

class LocalRssRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
    private val rssNetworkDataSource: RssNetworkDataSource,
    private val accountDao: AccountDao,
    private val groupDao: GroupDao,
    workManager: WorkManager,
) : AbstractRssRepository(
    context, accountDao, articleDao, groupDao,
    feedDao, rssNetworkDataSource, workManager,
) {
    private val notificationManager: NotificationManager =
        (getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.createNotificationChannel(
                    NotificationChannel(
                        NotificationGroupName.ARTICLE_UPDATE,
                        NotificationGroupName.ARTICLE_UPDATE,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }

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
        return UUID.randomUUID().toString().also {
            groupDao.insert(
                Group(
                    id = it,
                    name = name,
                    accountId = context.currentAccountId
                )
            )
        }
    }

    override suspend fun sync() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val preTime = System.currentTimeMillis()
                val accountId = context.currentAccountId
                val articles = mutableListOf<Article>()
                feedDao.queryAll(accountId).also { feed ->
                    updateSyncState {
                        it.copy(
                            feedCount = feed.size,
                        )
                    }
                }.map { feed ->
                    async {
                        syncFeed(accountId, feed)
                    }
                }.awaitAll().forEach {
                    if (it.isNotify) {
                        notify(it.articles)
                    }
                    articles.addAll(it.articles)
                }

                articleDao.insertList(articles)
                Log.i(
                    "RlOG",
                    "onCompletion: ${System.currentTimeMillis() - preTime}"
                )
                accountDao.queryById(accountId)?.let { account ->
                    accountDao.update(
                        account.apply {
                            updateAt = Date()
                        }
                    )
                }
                updateSyncState {
                    it.copy(
                        feedCount = 0,
                        syncedCount = 0,
                        currentFeedName = ""
                    )
                }
            }
        }
    }

    data class ArticleNotify(
        val articles: List<Article>,
        val isNotify: Boolean,
    )

    private suspend fun syncFeed(
        accountId: Int,
        feed: Feed
    ): ArticleNotify {
        val latest = articleDao.queryLatestByFeedId(accountId, feed.id)
        val articles = rssHelper.queryRssXml(
            rssNetworkDataSource,
            accountId,
            feed,
            latest?.link,
        ).also {
            if (feed.icon == null && it.isNotEmpty()) {
                rssHelper.queryRssIcon(feedDao, feed, it.first().link)
            }
        }
        updateSyncState {
            it.copy(
                syncedCount = it.syncedCount + 1,
                currentFeedName = feed.name
            )
        }
        return ArticleNotify(
            articles = articles,
            isNotify = articles.isNotEmpty() && feed.isNotification
        )
    }

    private fun notify(
        articles: List<Article>,
    ) {
        articles.forEach { article ->
            val builder = NotificationCompat.Builder(
                context,
                NotificationGroupName.ARTICLE_UPDATE
            ).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setGroup(NotificationGroupName.ARTICLE_UPDATE)
                .setContentTitle(article.title)
                .setContentText(article.shortDescription)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        Random().nextInt() + article.id.hashCode(),
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(
                                ExtraName.ARTICLE_ID,
                                article.id
                            )
                        },
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            notificationManager.notify(
                Random().nextInt() + article.id.hashCode(),
                builder.build().apply {
                    flags = Notification.FLAG_AUTO_CANCEL
                }
            )
        }
    }
}