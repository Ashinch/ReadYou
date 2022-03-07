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
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.ash.reader.*
import me.ash.reader.R
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.article.Article
import me.ash.reader.data.article.ArticleDao
import me.ash.reader.data.constant.Symbol
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.feed.FeedWithArticle
import me.ash.reader.data.source.ReaderDatabase
import me.ash.reader.data.source.RssNetworkDataSource
import net.dankito.readability4j.Readability4J
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@DelicateCoroutinesApi
class RssRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val accountDao: AccountDao,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssNetworkDataSource: RssNetworkDataSource,
    private val workManager: WorkManager,
) {
    @Throws(Exception::class)
    suspend fun searchFeed(feedLink: String): FeedWithArticle {
        val accountId = context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        val parseRss = rssNetworkDataSource.parseRss(feedLink)
        val feed = Feed(
            name = parseRss.title!!,
            url = feedLink,
            groupId = 0,
            accountId = accountId,
        )
        val articles = mutableListOf<Article>()
        parseRss.items.forEach {
            articles.add(
                Article(
                    accountId = accountId,
                    feedId = feed.id ?: 0,
                    date = Date(it.publishDate.toString()),
                    title = it.title.toString(),
                    author = it.author,
                    rawDescription = it.description.toString(),
                    shortDescription = (Readability4JExtended("", it.description.toString())
                        .parse().textContent ?: "").trim().run {
                        if (this.length > 100) this.substring(0, 100)
                        else this
                    },
                    link = it.link ?: "",
                )
            )
        }
        return FeedWithArticle(feed, articles)
    }

    fun parseDescriptionContent(link: String, content: String): String {
        val readability4J: Readability4J = Readability4JExtended(link, content)
        val article = readability4J.parse()
        val element = article.articleContent
        return element.toString()
    }

    fun parseFullContent(link: String, title: String, callback: (String) -> Unit) {
        OkHttpClient()
            .newCall(Request.Builder().url(link).build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val content = response.body?.string()
                    val readability4J: Readability4J =
                        Readability4JExtended(link, content ?: "")
                    val articleContent = readability4J.parse().articleContent
                    if (articleContent == null) {
                        callback("")
                    } else {
                        val h1Element = articleContent.selectFirst("h1")
                        if (h1Element != null && h1Element.hasText() && h1Element.text() == title) {
                            h1Element.remove()
                        }
                        callback(articleContent.toString())
                    }
                }
            })
    }

    fun peekWork(): String {
        return workManager.getWorkInfosByTag("sync").get().size.toString()
    }

    suspend fun sync(isWork: Boolean? = false) {
        if (isWork == true) {
            workManager.cancelAllWork()
            val syncWorkerRequest: WorkRequest =
                PeriodicWorkRequestBuilder<SyncWorker>(
                    15, TimeUnit.MINUTES
                ).setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                ).addTag("sync").build()
            workManager.enqueue(syncWorkerRequest)
        } else {
            normalSync(context, accountDao, articleDao, feedDao, rssNetworkDataSource)
        }
    }

    @DelicateCoroutinesApi
    companion object {
        data class SyncState(
            val feedCount: Int = 0,
            val syncedCount: Int = 0,
            val currentFeedName: String = "",
        ) {
            val isSyncing: Boolean = feedCount != 0 || syncedCount != 0 || currentFeedName != ""
            val isNotSyncing: Boolean = !isSyncing
        }

        val syncState = MutableStateFlow(SyncState())
        private val mutex = Mutex()

        suspend fun normalSync(
            context: Context,
            accountDao: AccountDao,
            articleDao: ArticleDao,
            feedDao: FeedDao,
            rssNetworkDataSource: RssNetworkDataSource
        ) {
            doSync(context, accountDao, articleDao, feedDao, rssNetworkDataSource)
        }

        suspend fun workerSync(context: Context) {
            val db = ReaderDatabase.getInstance(context)
            doSync(
                context,
                db.accountDao(),
                db.articleDao(),
                db.feedDao(),
                RssNetworkDataSource.getInstance()
            )
        }

        private suspend fun doSync(
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
                val feedNotificationMap = mutableMapOf<Int, Boolean>()
                feeds.forEach { feed ->
                    feedNotificationMap[feed.id ?: 0] = feed.isNotification
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
                            val latest = articleDao.queryLatestByFeedId(accountId, feed.id ?: 0)
                            articles.addAll(
                                queryRssXml(
                                    rssNetworkDataSource,
                                    accountId,
                                    feed,
                                    latest?.title,
                                ).also {
                                    if (feed.icon == null && it.isNotEmpty()) {
                                        queryRssIcon(feedDao, feed, it.first().link)
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
                                Symbol.NOTIFICATION_CHANNEL_GROUP_ARTICLE_UPDATE,
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
                                    Symbol.NOTIFICATION_CHANNEL_GROUP_ARTICLE_UPDATE
                                ).setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setGroup(Symbol.NOTIFICATION_CHANNEL_GROUP_ARTICLE_UPDATE)
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
                                                    Symbol.EXTRA_ARTICLE_ID,
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

        private suspend fun queryRssXml(
            rssNetworkDataSource: RssNetworkDataSource,
            accountId: Int,
            feed: Feed,
            latestTitle: String? = null,
        ): List<Article> {
            val a = mutableListOf<Article>()
            try {
                val parseRss = rssNetworkDataSource.parseRss(feed.url)
                parseRss.items.forEach {
                    if (latestTitle != null && latestTitle == it.title) return a
                    Log.i("RLog", "request rss ${feed.name}: ${it.title}")
                    a.add(
                        Article(
                            accountId = accountId,
                            feedId = feed.id ?: 0,
                            date = Date(it.publishDate.toString()),
                            title = it.title.toString(),
                            author = it.author,
                            rawDescription = it.description.toString(),
                            shortDescription = (Readability4JExtended("", it.description.toString())
                                .parse().textContent ?: "").trim().run {
                                if (this.length > 100) this.substring(0, 100)
                                else this
                            },
                            link = it.link ?: "",
                        )
                    )
                }
                return a
            } catch (e: Exception) {
                Log.e("RLog", "error ${feed.name}: ${e.message}")
                return a
            }
        }

        private suspend fun queryRssIcon(
            feedDao: FeedDao,
            feed: Feed,
            articleLink: String?,
        ) {
            if (articleLink == null) return
            val execute = OkHttpClient()
                .newCall(Request.Builder().url(articleLink).build())
                .execute()
            val content = execute.body?.string()
            val regex =
                Regex("""<link(.+?)rel="shortcut icon"(.+?)href="(.+?)"""")
            if (content != null) {
                var iconLink = regex
                    .find(content)
                    ?.groups?.get(3)
                    ?.value
                Log.i("rlog", "queryRssIcon: $iconLink")
                if (iconLink != null) {
                    if (iconLink.startsWith("//")) {
                        iconLink = "http:$iconLink"
                    }
                    if (iconLink.startsWith("/")) {
                        val domainRegex =
                            Regex("""http(s)?://(([\w-]+\.)+\w+(:\d{1,5})?)""")
                        iconLink =
                            "http://${domainRegex.find(articleLink)?.groups?.get(2)?.value}$iconLink"
                    }
                    saveRssIcon(feedDao, feed, iconLink)
                } else {
//                    saveRssIcon(feedDao, feed, "")
                }
            } else {
//                saveRssIcon(feedDao, feed, "")
            }
        }

        private suspend fun saveRssIcon(feedDao: FeedDao, feed: Feed, iconLink: String) {
            val execute = OkHttpClient()
                .newCall(Request.Builder().url(iconLink).build())
                .execute()
            feedDao.update(
                feed.apply {
                    icon = execute.body?.bytes()
                }
            )
        }
    }
}

@DelicateCoroutinesApi
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Log.i("RLog", "doWork: ")
        RssRepository.workerSync(applicationContext)
        return Result.success()
    }
}