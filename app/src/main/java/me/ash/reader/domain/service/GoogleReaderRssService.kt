package me.ash.reader.domain.service

import android.content.Context
import android.text.Html
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.rometools.rome.feed.synd.SyndFeed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.domain.model.account.security.GoogleReaderSecurityKey
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleMeta
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.di.MainDispatcher
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofCategoryPathToId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofFeedPathToId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofItemPathToId
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.ext.spacerDollar
import net.dankito.readability4j.extended.Readability4JExtended
import java.util.*
import javax.inject.Inject

class GoogleReaderRssService @Inject constructor(
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
    private val workManager: WorkManager,
) : AbstractRssRepository(
    context, accountDao, articleDao, groupDao,
    feedDao, workManager, rssHelper, notificationHelper, ioDispatcher, defaultDispatcher
) {

    // override val subscribe: Boolean = true
    // override val move: Boolean = true
    // override val delete: Boolean = true
    // override val update: Boolean = true

    private suspend fun getGoogleReaderAPI() =
        GoogleReaderSecurityKey(accountDao.queryById(context.currentAccountId)!!.securityKey).run {
            GoogleReaderAPI.getInstance(
                serverUrl = serverUrl!!,
                username = username!!,
                password = password!!,
                httpUsername = null,
                httpPassword = null,
            )
        }

    override suspend fun validCredentials(): Boolean = getGoogleReaderAPI().validCredentials()

    override suspend fun clearAuthorization() {
        GoogleReaderAPI.clearInstance()
    }

    override suspend fun subscribe(
        feedLink: String, searchedFeed: SyndFeed, groupId: String,
        isNotification: Boolean, isFullContent: Boolean,
    ) {
        val accountId = context.currentAccountId
        val quickAdd = getGoogleReaderAPI().subscriptionQuickAdd(feedLink)
        val feedId = quickAdd.streamId?.ofFeedPathToId()!!
        getGoogleReaderAPI().subscriptionEdit(feedId, groupId.dollarLast())
        // TODO: Support rename while adding a subscription
        feedDao.insert(Feed(
            id = accountId.spacerDollar(feedId),
            name = searchedFeed.title!!,
            url = feedLink,
            groupId = groupId,
            accountId = accountId,
            isNotification = isNotification,
            isFullContent = isFullContent,
        ))
        SyncWorker.enqueueOneTimeWork(workManager)
    }

    override suspend fun addGroup(name: String): String {
        throw Exception("Unsupported")
    }

    /**
     * Google Reader API synchronous processing with object's ID to ensure idempotence
     * and handle foreign key relationships such as read status, starred status, etc.
     *
     * 1. Fetch list of feeds and folders.
     * 2. Fetch list of tags (it contains folders too, so you need to remove folders found in previous call to get
     * tags).
     * 3. Fetch ids of unread items (user can easily have 1000000 unread items so, please, add a limit on how many
     * articles you sync, 25000 could be a good default, customizable limit is even better).
     * 4. Fetch ids of starred items (100k starred items are possible, so, please, limit them too, 10-25k limit is a
     * good default).
     * 5. Fetch tagged item ids by passing s=user/-/label/TagName parameter.
     * 6. Remove items that are no longer in unread/starred/tagged ids lists from your local database.
     * 7. Fetch contents of items missing in database.
     * 8. Mark/unmark items read/starred/tagged in you app comparing local state and ids you've got from the Google Reader API.
     *
     * Use edit-tag to sync read/starred/tagged status from your app to Google Reader API.
     *
     * @link https://github.com/bazqux/bazqux-api?tab=readme-ov-file
     */
    override suspend fun sync(coroutineWorker: CoroutineWorker): ListenableWorker.Result = supervisorScope {
        coroutineWorker.setProgress(SyncWorker.setIsSyncing(true))

        try {
            val preTime = System.currentTimeMillis()
            val accountId = context.currentAccountId
            val account = accountDao.queryById(accountId)!!
            val googleReaderAPI = getGoogleReaderAPI()
            val groupIds = mutableSetOf<String>()
            val feedIds = mutableSetOf<String>()

            // 1. Fetch list of feeds and folders
            googleReaderAPI.getSubscriptionList()
                .subscriptions.groupBy { it.categories?.first() }
                .forEach { (category, feeds) ->
                    val groupId = accountId.spacerDollar(category?.id?.ofCategoryPathToId()!!)

                    // Handle folders
                    groupDao.insert(
                        Group(
                            id = groupId,
                            name = category.label!!,
                            accountId = accountId,
                        )
                    )
                    groupIds.add(groupId)

                    // Handle feeds
                    feedDao.insert(
                        *feeds.map {
                            val feedId = accountId.spacerDollar(it.id?.ofFeedPathToId()!!)
                            Feed(
                                id = feedId,
                                name = it.title ?: context.getString(R.string.empty),
                                url = it.url!!,
                                groupId = groupId,
                                accountId = accountId,
                                icon = it.iconUrl
                            ).also {
                                feedIds.add(feedId)
                            }
                        }.toTypedArray()
                    )
                }

            // Remove orphaned groups and feeds
            groupDao.queryAll(accountId)
                .filter { it.id !in groupIds }
                .forEach { super.deleteGroup(it) }
            feedDao.queryAll(accountId)
                .filter { it.id !in feedIds }
                .forEach { super.deleteFeed(it) }

            // 3. Fetch ids of unread items
            val unreadIds = googleReaderAPI.getUnreadItemIds().itemRefs?.map { it.id }

            // 4. Fetch ids of starred items
            val starredIds = googleReaderAPI.getStarredItemIds().itemRefs?.map { it.id }

            // 5. Fetch ids of read items since last month
            val readIds = googleReaderAPI.getReadItemIds(
                Calendar.getInstance().apply {
                    time = Date()
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time.time / 1000
            ).itemRefs

            // 6. Fetch items contents for ids
            readIds?.map { it.id!! }?.chunked(100)?.forEach { chunkedIds ->
                articleDao.insert(
                    *googleReaderAPI.getItemsContents(chunkedIds).items?.map {
                        val articleId = it.id!!.ofItemPathToId()
                        Article(
                            id = accountId.spacerDollar(articleId),
                            date = it.published?.run { Date(this * 1000) } ?: Date(),
                            title = Html.fromHtml(it.title ?: context.getString(R.string.empty)).toString(),
                            author = it.author,
                            rawDescription = it.summary?.content ?: "",
                            shortDescription = (Readability4JExtended("", it.summary?.content ?: "")
                                .parse().textContent ?: "")
                                .take(110)
                                .trim(),
                            fullContent = it.summary?.content ?: "",
                            img = rssHelper.findImg(it.summary?.content ?: ""),
                            link = it.canonical?.first()?.href
                                ?: it.alternate?.first()?.href
                                ?: it.origin?.htmlUrl ?: "",
                            feedId = accountId.spacerDollar(it.origin?.streamId?.ofFeedPathToId() ?: feedIds.first()),
                            accountId = accountId,
                            isUnread = unreadIds?.contains(articleId) ?: true,
                            isStarred = starredIds?.contains(articleId) ?: false,
                            updateAt = it.crawlTimeMsec?.run { Date(this.toLong()) } ?: Date(),
                        )
                    }?.toTypedArray() ?: emptyArray()
                )
            }


            // 7. Remove items that are no longer in unread/starred/tagged ids lists from your local database

            // 8. Fetch contents of items missing in database.

            // 9. Mark/unmark items read/starred/tagged in you app comparing local state and ids you've got from the
            // GoogleReader

            val articlesMeta = articleDao.queryArticleMetadataAll(accountId)
            for (meta: ArticleMeta in articlesMeta) {
                val articleId = meta.id.dollarLast()
                val shouldBeUnread = unreadIds?.contains(articleId)
                val shouldBeStarred = starredIds?.contains(articleId)
                if (meta.isUnread != shouldBeUnread) {
                    articleDao.markAsReadByArticleId(accountId, meta.id, shouldBeUnread ?: true)
                }
                if (meta.isStarred != shouldBeStarred) {
                    articleDao.markAsStarredByArticleId(accountId, meta.id, shouldBeStarred ?: false)
                }
            }

            Log.i("RLog", "onCompletion: ${System.currentTimeMillis() - preTime}")
            accountDao.update(account.apply {
                updateAt = Date()
                readIds?.takeIf { it.isNotEmpty() }?.first()?.id?.let {
                    lastArticleId = accountId.spacerDollar(it)
                }
            })
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
        val googleReaderAPI = getGoogleReaderAPI()
        val beforeUnixTimestamp = (before?.time ?: Date(Long.MAX_VALUE).time) / 1000
        when {
            groupId != null -> {
                // googleReaderAPI.markGroup(
                //     status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                //     id = groupId.dollarLast().toLong(),
                //     before = beforeUnixTimestamp
                // )
            }

            feedId != null -> {
                // googleReaderAPI.markFeed(
                //     status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                //     id = feedId.dollarLast().toLong(),
                //     before = beforeUnixTimestamp
                // )
            }

            articleId != null -> {
                // googleReaderAPI.markItem(
                //     status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                //     id = articleId.dollarLast(),
                // )
            }

            else -> {
                feedDao.queryAll(context.currentAccountId).forEach {
                    // googleReaderAPI.markFeed(
                    //     status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    //     id = it.id.dollarLast().toLong(),
                    //     before = beforeUnixTimestamp
                    // )
                }
            }
        }
    }

    override suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        super.markAsStarred(articleId, isStarred)
        val googleReaderAPI = getGoogleReaderAPI()
        // googleReaderAPI.markItem(
        //     status = if (isStarred) FeverDTO.StatusEnum.Saved else FeverDTO.StatusEnum.Unsaved,
        //     id = articleId.dollarLast()
        // )
    }
}
