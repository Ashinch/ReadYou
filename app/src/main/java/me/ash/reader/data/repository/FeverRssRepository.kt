package me.ash.reader.data.repository

import android.content.Context
import android.text.Html
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.ArticleDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.model.account.security.FeverSecurityKey
import me.ash.reader.data.model.article.Article
import me.ash.reader.data.model.feed.Feed
import me.ash.reader.data.model.group.Group
import me.ash.reader.data.module.DefaultDispatcher
import me.ash.reader.data.module.IODispatcher
import me.ash.reader.data.module.MainDispatcher
import me.ash.reader.data.provider.fever.FeverAPI
import me.ash.reader.data.provider.fever.FeverDTO
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.ext.spacerDollar
import net.dankito.readability4j.extended.Readability4JExtended
import java.util.*
import javax.inject.Inject

class FeverRssRepository @Inject constructor(
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
    @MainDispatcher
    private val mainDispatcher: CoroutineDispatcher,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    workManager: WorkManager,
) : AbstractRssRepository(
    context, accountDao, articleDao, groupDao,
    feedDao, workManager, rssHelper, notificationHelper, ioDispatcher, defaultDispatcher
) {

    override val subscribe = false
    override val move: Boolean = false

    private suspend fun getFeverAPI() =
        FeverSecurityKey(accountDao.queryById(context.currentAccountId)!!.securityKey).run {
            FeverAPI.getInstance(
                serverUrl = serverUrl!!,
                username = username!!,
                password = password!!,
                httpUsername = null,
                httpPassword = null,
            )
        }

    override suspend fun validCredentials(): Boolean = getFeverAPI().validCredentials() > 0

    override suspend fun subscribe(feed: Feed, articles: List<Article>) {
        throw Exception("Unsupported")
    }

    override suspend fun addGroup(name: String): String {
        throw Exception("Unsupported")
    }

    /**
     * Sync handling for the Fever API.
     *
     * 1. Fetch the Fever groups
     * 2. Fetch the Fever feeds
     * 3. Fetch the Fever articles
     * 4. Fetch the Fever favicons
     */
    override suspend fun sync(coroutineWorker: CoroutineWorker): ListenableWorker.Result =
        supervisorScope {
            coroutineWorker.setProgress(SyncWorker.setIsSyncing(true))

            try {
                val preTime = System.currentTimeMillis()
                val accountId = context.currentAccountId
                val feverAPI = getFeverAPI()

                // 1. Fetch the Fever groups
                groupDao.insert(
                    *feverAPI.getGroups().groups?.map {
                        Group(
                            id = accountId.spacerDollar(it.id!!),
                            name = it.title ?: context.getString(R.string.empty),
                            accountId = accountId,
                        )
                    }?.toTypedArray() ?: emptyArray()
                )

                // 2. Fetch the Fever feeds
                val feedsBody = feverAPI.getFeeds()
                val feedsGroupsMap = mutableMapOf<String, String>()
                feedsBody.feeds_groups?.forEach { feedsGroups ->
                    feedsGroups.group_id?.toString()?.let { groupId ->
                        feedsGroups.feed_ids?.split(",")?.forEach { feedId ->
                            feedsGroupsMap[feedId] = groupId
                        }
                    }
                }
                feedDao.insert(
                    *feedsBody.feeds?.map {
                        Feed(
                            id = accountId.spacerDollar(it.id!!),
                            name = it.title ?: context.getString(R.string.empty),
                            url = it.url!!,
                            groupId = accountId.spacerDollar(feedsGroupsMap[it.id.toString()]!!),
                            accountId = accountId,
                        )
                    }?.toTypedArray() ?: emptyArray()
                )

                // 3. Fetch the Fever articles (up to unlimited counts)
                var sinceId = ""
                var itemsBody = feverAPI.getItemsSince(sinceId)
                while (itemsBody.items?.isEmpty() == false) {
                    articleDao.insert(
                        *itemsBody.items?.map {
                            Article(
                                id = accountId.spacerDollar(it.id!!),
                                date = it.created_on_time?.run { Date(this * 1000) } ?: Date(),
                                title = Html.fromHtml(it.title ?: context.getString(R.string.empty)).toString(),
                                author = it.author,
                                rawDescription = it.html ?: "",
                                shortDescription = (Readability4JExtended("", it.html ?: "")
                                    .parse().textContent ?: "")
                                    .take(110)
                                    .trim(),
                                fullContent = it.html,
                                img = rssHelper.findImg(it.html ?: ""),
                                link = it.url ?: "",
                                feedId = accountId.spacerDollar(it.feed_id!!),
                                accountId = accountId,
                                isUnread = (it.is_read ?: 0) <= 0,
                                isStarred = (it.is_saved ?: 0) > 0,
                                updateAt = Date(),
                            ).also {
                                sinceId = it.id.dollarLast()
                            }
                        }?.toTypedArray() ?: emptyArray()
                    )
                    itemsBody = feverAPI.getItemsSince(sinceId)
                }

                // TODO: 4. Fetch the Fever favicons

                Log.i("RLog", "onCompletion: ${System.currentTimeMillis() - preTime}")
                accountDao.queryById(accountId)?.let { account ->
                    accountDao.update(account.apply { updateAt = Date() })
                }
                ListenableWorker.Result.success(SyncWorker.setIsSyncing(false))
            } catch (e: Exception) {
                Log.e("RLog", "On sync exception: ${e.message}", e)
                withContext(mainDispatcher) {
                    context.showToast(e.message)
                }
                ListenableWorker.Result.failure(SyncWorker.setIsSyncing(false))
            }
        }

    override suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        super.markAsRead(groupId, feedId, articleId, before, isUnread)
        val feverAPI = getFeverAPI()
        when {
            groupId != null -> {
                feverAPI.markGroup(
                    status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    id = groupId.dollarLast().toLong(),
                    before = before?.time ?: Date(Long.MAX_VALUE).time
                )
            }

            feedId != null -> {
                feverAPI.markFeed(
                    status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    id = feedId.dollarLast().toLong(),
                    before = before?.time ?: Date(Long.MAX_VALUE).time
                )
            }

            articleId != null -> {
                feverAPI.markItem(
                    status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    id = articleId.dollarLast(),
                )
            }

            else -> {
                feedDao.queryAll(context.currentAccountId).forEach {
                    feverAPI.markFeed(
                        status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                        id = it.id.dollarLast().toLong(),
                        before = before?.time ?: Date(Long.MAX_VALUE).time
                    )
                }
            }
        }
    }

    override suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        super.markAsStarred(articleId, isStarred)
        val feverAPI = getFeverAPI()
        feverAPI.markItem(
            status = if (isStarred) FeverDTO.StatusEnum.Saved else FeverDTO.StatusEnum.Unsaved,
            id = articleId.dollarLast()
        )
    }
}
