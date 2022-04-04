package me.ash.reader.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import me.ash.reader.MainActivity
import me.ash.reader.R
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Group
import me.ash.reader.data.module.DispatcherDefault
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.repository.SyncWorker.Companion.setIsSyncing
import me.ash.reader.data.source.RssNetworkDataSource
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.spacerDollar
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
    @DispatcherDefault
    private val dispatcherDefault: CoroutineDispatcher,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
    workManager: WorkManager,
) : AbstractRssRepository(
    context, accountDao, articleDao, groupDao,
    feedDao, rssNetworkDataSource, workManager,
    dispatcherIO
) {
    private val notificationManager: NotificationManager =
        (getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager).also {
            it.createNotificationChannel(
                NotificationChannel(
                    NotificationGroupName.ARTICLE_UPDATE,
                    NotificationGroupName.ARTICLE_UPDATE,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
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
            val articles = mutableListOf<Article>()
            feedDao.queryAll(accountId)
                .also { coroutineWorker.setProgress(setIsSyncing(true)) }
                .map { feed -> async { syncFeed(feed) } }
                .awaitAll()
                .forEach {
                    if (it.isNotify) {
                        notify(it.articles)
                    }
                    articles.addAll(it.articles)
                }

            articleDao.insertList(articles)
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

    data class ArticleNotify(
        val articles: List<Article>,
        val isNotify: Boolean,
    )

    private suspend fun syncFeed(feed: Feed): ArticleNotify {
        val latest = articleDao.queryLatestByFeedId(context.currentAccountId, feed.id)
        var articles: List<Article>? = null
        try {
            articles = rssHelper.queryRssXml(feed, latest?.link)
        } catch (e: Exception) {
            Log.e("RLog", "queryRssXml[${feed.name}]: ${e.message}")
            return ArticleNotify(listOf(), false)
        }
        try {
            if (feed.icon == null && !articles.isNullOrEmpty()) {
                rssHelper.queryRssIcon(feedDao, feed, articles.first().link)
            }
        } catch (e: Exception) {
            Log.e("RLog", "queryRssIcon[${feed.name}]: ${e.message}")
            return ArticleNotify(listOf(), false)
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