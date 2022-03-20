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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.ash.reader.*
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.article.Article
import me.ash.reader.data.article.ArticleDao
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
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
    rssNetworkDataSource: RssNetworkDataSource,
    groupDao: GroupDao,
    accountDao: AccountDao,
    workManager: WorkManager,
) : AbstractRssRepository(
    context, accountDao, articleDao, groupDao,
    feedDao, rssNetworkDataSource, workManager,
) {
    private val mutex = Mutex()
    private val syncState = MutableStateFlow(SyncState())

    override fun getSyncState() = syncState

    override suspend fun updateArticleInfo(article: Article) {
        articleDao.update(article)
    }

    override suspend fun subscribe(feed: Feed, articles: List<Article>) {
        feedDao.insert(feed)
        articleDao.insertList(articles.map {
            it.copy(feedId = feed.id)
        })
    }

    override suspend fun sync(
        context: Context,
        accountDao: AccountDao,
        articleDao: ArticleDao,
        feedDao: FeedDao,
        rssNetworkDataSource: RssNetworkDataSource
    ) {
        mutex.withLock {
            val accountId = context.dataStore.get(DataStoreKeys.CurrentAccountId)
                ?: return
            val feeds = feedDao.queryAll(accountId)
            val feedNotificationMap = mutableMapOf<String, Boolean>()
            feeds.forEach { feed ->
                feedNotificationMap[feed.id] = feed.isNotification
            }
            val preTime = System.currentTimeMillis()
            val chunked = feeds.chunked(6)
            chunked.forEachIndexed { index, item ->
                item.forEach {
                    Log.i("RlOG", "chunked $index: ${it.name}")
                }
            }
            val flows = mutableListOf<Flow<List<Article>>>()
            repeat(chunked.size) {
                flows.add(flow {
                    val articles = mutableListOf<Article>()
                    chunked[it].forEach { feed ->
                        val latest = articleDao.queryLatestByFeedId(accountId, feed.id)
                        articles.addAll(
                            rssHelper.queryRssXml(
                                rssNetworkDataSource,
                                accountId,
                                feed,
                                latest?.title,
                            ).also {
                                if (feed.icon == null && it.isNotEmpty()) {
                                    rssHelper.queryRssIcon(feedDao, feed, it.first().link)
                                }
                            }
                        )
                        syncState.update {
                            it.copy(
                                feedCount = feeds.size,
                                syncedCount = syncState.value.syncedCount + 1,
                                currentFeedName = feed.name
                            )
                        }
                    }
                    emit(articles)
                })
            }
            combine(
                flows
            ) {
                val notificationManager: NotificationManager =
                    getSystemService(
                        context,
                        NotificationManager::class.java
                    ) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationManager.createNotificationChannel(
                        NotificationChannel(
                            NotificationGroupName.ARTICLE_UPDATE,
                            "文章更新",
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                    )
                }
                it.forEach { articleList ->
                    val ids = articleDao.insertList(articleList)
                    articleList.forEachIndexed { index, article ->
                        Log.i("RlOG", "combine ${article.feedId}: ${article.title}")
                        if (feedNotificationMap[article.feedId] == true) {
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
                                        ids[index].toInt(),
                                        Intent(context, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            putExtra(
                                                ExtraName.ARTICLE_ID,
                                                ids[index].toInt()
                                            )
                                        },
                                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                )
                            notificationManager.notify(
                                ids[index].toInt(),
                                builder.build().apply {
                                    flags = Notification.FLAG_AUTO_CANCEL
                                }
                            )
                        }
                    }
                }
            }.buffer().onCompletion {
                val afterTime = System.currentTimeMillis()
                Log.i("RlOG", "onCompletion: ${afterTime - preTime}")
                accountDao.queryById(accountId)?.let { account ->
                    accountDao.update(
                        account.apply {
                            updateAt = Date()
                        }
                    )
                }
                syncState.update {
                    it.copy(
                        feedCount = 0,
                        syncedCount = 0,
                        currentFeedName = ""
                    )
                }
            }.collect()
        }
    }
}