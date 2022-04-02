package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Group
import me.ash.reader.data.module.ApplicationScope
import me.ash.reader.data.module.DispatcherDefault
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.source.FeverApiDataSource
import me.ash.reader.data.source.RssNetworkDataSource
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.spacerDollar
import net.dankito.readability4j.extended.Readability4JExtended
import java.util.*
import javax.inject.Inject
import kotlin.collections.set

class FeverRssRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val groupDao: GroupDao,
    private val rssHelper: RssHelper,
    private val feverApiDataSource: FeverApiDataSource,
    private val accountDao: AccountDao,
    rssNetworkDataSource: RssNetworkDataSource,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
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
        applicationScope.launch(dispatcherDefault) {
            mutex.withLock {
                val accountId = context.currentAccountId

                updateSyncState {
                    it.copy(
                        feedCount = 1,
                        syncedCount = 1,
                        currentFeedName = "Fever"
                    )
                }

                if (feedDao.queryAll(accountId).isNullOrEmpty()) {
                    // Temporary add feeds
                    val feverFeeds = feverApiDataSource.feeds().execute().body()!!.feeds
                    val feverGroupsBody = feverApiDataSource.groups().execute().body()!!
                    Log.i("RLog", "Fever groups: $feverGroupsBody")
                    feverGroupsBody.groups.forEach {
                        groupDao.insert(
                            Group(
                                id = accountId.spacerDollar(it.id),
                                name = it.title,
                                accountId = accountId,
                            )
                        )
                    }
                    val feverFeedsGroupsMap = mutableMapOf<Int, Int>()
                    feverGroupsBody.feeds_groups.forEach { item ->
                        item.feed_ids
                            .split(",")
                            .map { it.toInt() }
                            .forEach { id ->
                                feverFeedsGroupsMap[id] = item.group_id
                            }
                    }
                    val feeds = feverFeeds.map {
                        Feed(
                            id = accountId.spacerDollar(it.id),
                            name = it.title,
                            url = it.url,
                            groupId = feverFeedsGroupsMap[it.id].toString(),
                            accountId = accountId
                        )
                    }
                    feedDao.insertList(feeds)
                }

                // Add articles
                val articles = mutableListOf<Article>()
                feverApiDataSource.itemsBySince(since = 1647444325925621L)
                    .execute().body()!!.items
                    .forEach {
                        articles.add(
                            Article(
                                id = accountId.spacerDollar(it.id),
                                date = Date(it.created_on_time * 1000),
                                title = it.title,
                                author = it.author,
                                rawDescription = it.html,
                                shortDescription = (
                                        Readability4JExtended("", it.html)
                                            .parse().textContent ?: ""
                                        ).take(100).trim(),
                                link = it.url,
                                accountId = accountId,
                                feedId = it.feed_id.toString(),
                                isUnread = it.is_read == 0,
                                isStarred = it.is_saved == 1,
                            )
                        )
                    }
                articleDao.insertList(articles)

                // Complete sync
                accountDao.update(accountDao.queryById(accountId)!!.apply {
                    updateAt = Date()
                })
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
}