package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.work.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import me.ash.reader.DataStoreKeys
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.article.Article
import me.ash.reader.data.article.ArticleDao
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.data.article.ImportantCount
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupDao
import me.ash.reader.data.group.GroupWithFeed
import me.ash.reader.data.source.ReaderDatabase
import me.ash.reader.data.source.RssNetworkDataSource
import me.ash.reader.dataStore
import me.ash.reader.get
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class AbstractRssRepository constructor(
    private val context: Context,
    private val accountDao: AccountDao,
    private val articleDao: ArticleDao,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val rssNetworkDataSource: RssNetworkDataSource,
    private val workManager: WorkManager,
) {
    data class SyncState(
        val feedCount: Int = 0,
        val syncedCount: Int = 0,
        val currentFeedName: String = "",
    ) {
        val isSyncing: Boolean = feedCount != 0 || syncedCount != 0 || currentFeedName != ""
        val isNotSyncing: Boolean = !isSyncing
    }

    abstract suspend fun updateArticleInfo(article: Article)

    abstract suspend fun subscribe(feed: Feed, articles: List<Article>)

    abstract suspend fun sync(
        context: Context,
        accountDao: AccountDao,
        articleDao: ArticleDao,
        feedDao: FeedDao,
        rssNetworkDataSource: RssNetworkDataSource
    )

    fun pullGroups(): Flow<MutableList<Group>> {
        val accountId = context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        return groupDao.queryAllGroup(accountId)
    }

    fun pullFeeds(): Flow<MutableList<GroupWithFeed>> {
        return groupDao.queryAllGroupWithFeed(
            context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        )
    }

    fun pullArticles(
        groupId: String? = null,
        feedId: String? = null,
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

    suspend fun findArticleById(id: Int): ArticleWithFeed? {
        return articleDao.queryById(id)
    }

    fun peekWork(): String {
        return workManager.getWorkInfosByTag("sync").get().size.toString()
    }

    suspend fun doSync(isWork: Boolean? = false) {
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
            sync(context, accountDao, articleDao, feedDao, rssNetworkDataSource)
        }
    }
}

@DelicateCoroutinesApi
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var rssRepository: RssRepository

    @Inject
    lateinit var rssNetworkDataSource: RssNetworkDataSource

    override suspend fun doWork(): Result {
        Log.i("RLog", "doWork: ")
        val db = ReaderDatabase.getInstance(applicationContext)
        rssRepository.get().sync(
            applicationContext,
            db.accountDao(),
            db.articleDao(),
            db.feedDao(),
            rssNetworkDataSource
        )
        return Result.success()
    }
}