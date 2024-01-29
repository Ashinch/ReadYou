package me.ash.reader.domain.service

import android.content.Context
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
import me.ash.reader.domain.model.account.Account
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
import me.ash.reader.infrastructure.html.Readability
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofCategoryStreamIdToId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofFeedStreamIdToId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofItemStreamIdToId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderDTO
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.ext.spacerDollar
import java.util.Calendar
import java.util.Date
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

    override val addSubscription: Boolean = true
    override val moveSubscription: Boolean = true
    override val deleteSubscription: Boolean = true
    override val updateSubscription: Boolean = true

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

    override suspend fun validCredentials(account: Account): Boolean {
        return getGoogleReaderAPI().validCredentials().also { success ->
            if (success) try {
                getGoogleReaderAPI().getUserInfo().userName?.let {
                    accountDao.update(account.copy(name = it))
                }
            } catch (ignore: Exception) {
                Log.e("RLog", "get user info is failed: ", ignore)
            }
        }
    }

    override suspend fun clearAuthorization() {
        GoogleReaderAPI.clearInstance()
    }

    override suspend fun subscribe(
        feedLink: String, searchedFeed: SyndFeed, groupId: String,
        isNotification: Boolean, isFullContent: Boolean,
    ) {
        val accountId = context.currentAccountId
        val quickAdd = getGoogleReaderAPI().subscriptionQuickAdd(feedLink)
        val feedId = quickAdd.streamId?.ofFeedStreamIdToId()!!
        getGoogleReaderAPI().subscriptionEdit(
            destFeedId = feedId,
            destCategoryId = groupId.dollarLast(),
            destFeedName = searchedFeed.title!!
        )
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

    override suspend fun addGroup(
        destFeed: Feed?,
        newGroupName: String,
    ): String {
        val accountId = context.currentAccountId
        getGoogleReaderAPI().subscriptionEdit(
            destFeedId = destFeed?.id?.dollarLast(),
            destCategoryId = newGroupName
        )
        val id = accountId.spacerDollar(newGroupName)
        groupDao.insert(
            Group(
                id = id,
                name = newGroupName,
                accountId = accountId
            )
        )
        return id
    }

    override suspend fun renameGroup(group: Group) {
        getGoogleReaderAPI().renameTag(
            categoryId = group.id.dollarLast(),
            renameToName = group.name
        )
        // TODO: Whether to switch the old ID to the new ID?
        super.renameGroup(group)
    }

    override suspend fun moveFeed(originGroupId: String, feed: Feed) {
        getGoogleReaderAPI().subscriptionEdit(
            destFeedId = feed.id.dollarLast(),
            destCategoryId = feed.groupId.dollarLast(),
            originCategoryId = originGroupId.dollarLast(),
        )
        super.moveFeed(originGroupId, feed)
    }

    override suspend fun changeFeedUrl(feed: Feed) {
        throw Exception("Unsupported")
    }

    override suspend fun renameFeed(feed: Feed) {
        getGoogleReaderAPI().subscriptionEdit(
            destFeedId = feed.id.dollarLast(),
            destFeedName = feed.name
        )
        // TODO: Whether to switch the old ID to the new ID?
        super.renameFeed(feed)
    }

    override suspend fun deleteGroup(group: Group, onlyDeleteNoStarred: Boolean?) {
        feedDao.queryByGroupId(context.currentAccountId, group.id)
            .forEach { deleteFeed(it) }
        getGoogleReaderAPI().disableTag(group.id.dollarLast())
        super.deleteGroup(group, false)
    }

    override suspend fun deleteFeed(feed: Feed, onlyDeleteNoStarred: Boolean?) {
        getGoogleReaderAPI().subscriptionEdit(
            action = "unsubscribe",
            destFeedId = feed.id.dollarLast()
        )
        super.deleteFeed(feed, false)
    }

    /**
     * Google Reader API synchronous processing with object's ID to ensure idempotence
     * and handle foreign key relationships such as read status, starred status, etc.
     *
     * 1. /reader/api/0/tag/list
     *     - Full list of categories/folders and tags/labels - and for InnoReader compatibility,
     *     including the number of unread items in each tags/labels.
     *
     * 2. /reader/api/0/subscription/list
     *     - Full list of subscriptions/feeds, including their category/folder.
     *     - This is where you get a distinction between categories/folders and tags/labels.
     *
     * 3. /reader/api/0/stream/contents/user/-/state/com.google/reading-list
     * (with some filters in parameter to exclude read items with xt,
     * and get only the new ones with ot, cf. log below)
     *     - List of new unread items and their content
     *     - The response contains among other things the read/unread state,
     *     the starred/not-starred state, and the tags/labels for each entry.
     *     - Since this request is very expensive for the client, the network, and the server,
     *     it is important to use the filters appropriately.
     *     - If there is no new item since the last synchronisation, the response should be empty,
     *     and therefore efficient.
     *
     * 4. /reader/api/0/stream/items/ids
     * (with a filter in parameter to exclude read items with xt)
     *     - Longer list of unread items IDs
     *     - This allows updating the read/unread status of the local cache of articles - assuming
     *     the ones not in the list are read.
     *
     * 5. /reader/api/0/stream/contents/user/-/state/com.google/starred
     * (with some filters in parameter to exclude read items with xt,
     * and get only the new ones with ot)
     *     - List of new unread starred items and their content
     *     - If there is no new unread starred item since the last synchronisation,
     *     the response should be empty, and therefore efficient
     *     - This is a bit redundant with request 3 and 6,
     *     but with the advantage of being able to retrieve a larger amount of unread starred items.
     *
     * 6. /reader/api/0/stream/contents/user/-/state/com.google/starred
     * (with some other filters, which includes read starred items)
     *     - List of starred items (also read ones) and their content.
     *
     * 7. /reader/api/0/stream/items/ids
     * (with a filter to get only starred ones)
     *     - Longer list of starred items IDs
     *     - This allows updating the starred/non-starred status of
     *     the local cache of articles - assuming the ones not in the list are not starred
     *     - Similar than request 4 but for the starred status.
     *
     * @link https://github.com/FreshRSS/FreshRSS/issues/2566#issuecomment-541317776
     * @link https://github.com/bazqux/bazqux-api?tab=readme-ov-file
     * @link https://github.com/theoldreader/api
     */
    override suspend fun sync(coroutineWorker: CoroutineWorker): ListenableWorker.Result =
        supervisorScope {
            coroutineWorker.setProgress(SyncWorker.setIsSyncing(true))

            try {
                val preTime = System.currentTimeMillis()
                val accountId = context.currentAccountId
                val account = accountDao.queryById(accountId)!!
                val googleReaderAPI = getGoogleReaderAPI()
                val groupIds = mutableSetOf<String>()
                val feedIds = mutableSetOf<String>()
                val lastUpdateAt = Calendar.getInstance().apply {
                    if (account.updateAt != null) {
                        time = account.updateAt!!
                        add(Calendar.HOUR, -1)
                    } else {
                        time = Date(preTime)
                        add(Calendar.MONTH, -1)
                    }
                }.time.time / 1000

                // 1. Fetch tags (not supported yet)

                // 2. Fetch folder and subscription list
                googleReaderAPI.getSubscriptionList()
                    .subscriptions.groupBy { it.categories?.first() }
                    .forEach { (category, feeds) ->
                        val groupId =
                            accountId.spacerDollar(category?.id?.ofCategoryStreamIdToId()!!)

                        // Handle folders
                        groupDao.insertOrUpdate(
                            listOf(Group(
                                id = groupId,
                                name = category.label!!,
                                accountId = accountId,
                            ))
                        )
                        groupIds.add(groupId)

                        // Handle feeds
                        feedDao.insertOrUpdate(
                            feeds.map {
                                val feedId = accountId.spacerDollar(it.id?.ofFeedStreamIdToId()!!)
                                Feed(
                                    id = feedId,
                                    name = it.title.decodeHTML()
                                        ?: context.getString(R.string.empty),
                                    url = it.url!!,
                                    groupId = groupId,
                                    accountId = accountId,
                                    icon = it.iconUrl
                                ).also {
                                    feedIds.add(feedId)
                                }
                            }
                        )
                    }

                // Handle empty icon for feeds
                feedDao.queryNoIcon(accountId).let {
                    it.forEach { feed ->
                        feed.icon = rssHelper.queryRssIconLink(feed.url)
                    }
                    feedDao.update(*it.toTypedArray())
                }

                // 2. Fetch latest unread item contents since last sync
                var unreadIds = fetchItemIdsAndContinue {
                    googleReaderAPI.getUnreadItemIds(since = lastUpdateAt, continuationId = it)
                }
                fetchItemsContents(
                    itemIds = unreadIds,
                    googleReaderAPI = googleReaderAPI,
                    accountId = accountId,
                    feedIds = feedIds,
                    unreadIds = unreadIds,
                    starredIds = listOf())

                // 3. Fetch all starred item contents
                val starredIds = fetchItemIdsAndContinue {
                    googleReaderAPI.getStarredItemIds(continuationId = it)
                }
                fetchItemsContents(
                    itemIds = starredIds,
                    googleReaderAPI = googleReaderAPI,
                    accountId = accountId,
                    feedIds = feedIds,
                    unreadIds = unreadIds,
                    starredIds = starredIds
                )

                // 4. Mark/unmarked items read/starred (/tagged)
                // Fetch all unread item id list
                unreadIds = fetchItemIdsAndContinue {
                    googleReaderAPI.getUnreadItemIds(continuationId = it)
                }
                val articlesMeta = articleDao.queryMetadataAll(accountId)
                for (meta: ArticleMeta in articlesMeta) {
                    val articleId = meta.id.dollarLast()
                    val shouldBeRead = !unreadIds.contains(articleId)
                    val shouldBeUnStarred = !starredIds.contains(articleId)
                    if (meta.isUnread && shouldBeRead) {
                        articleDao.markAsReadByArticleId(accountId, meta.id, true)
                    }
                    if (meta.isStarred && shouldBeUnStarred) {
                        articleDao.markAsStarredByArticleId(accountId, meta.id, false)
                    }
                }

                // 5. Remove orphaned groups and feeds, after synchronizing the starred/un-starred
                groupDao.queryAll(accountId)
                    .filter { it.id !in groupIds }
                    .forEach { super.deleteGroup(it, true) }
                feedDao.queryAll(accountId)
                    .filter { it.id !in feedIds }
                    .forEach { super.deleteFeed(it, true) }

                // 6. Record the time of this synchronization
                Log.i("RLog", "onCompletion: ${System.currentTimeMillis() - preTime}")
                accountDao.update(account.apply {
                    updateAt = Date(preTime)
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

    private suspend fun fetchItemIdsAndContinue(getItemIdsFunc: suspend (continuationId: String?) -> GoogleReaderDTO.ItemIds): MutableList<String> {
        var result = getItemIdsFunc(null)
        val ids = result.itemRefs?.mapNotNull { it.id }?.toMutableList() ?: return mutableListOf()
        while (result.continuation != null) {
            result = getItemIdsFunc(result.continuation)
            result.itemRefs?.mapNotNull { it.id }?.let { ids.addAll(it) }
        }
        return ids
    }

    private suspend fun fetchItemsContents(
        itemIds: List<String>?,
        googleReaderAPI: GoogleReaderAPI,
        accountId: Int,
        feedIds: MutableSet<String>,
        unreadIds: List<String>?,
        starredIds: List<String?>?,
    ) {
        itemIds?.chunked(100)?.forEach { chunkedIds ->
            articleDao.insert(
                *googleReaderAPI.getItemsContents(chunkedIds).items?.map {
                    val articleId = it.id!!.ofItemStreamIdToId()
                    Article(
                        id = accountId.spacerDollar(articleId),
                        date = it.published?.run { Date(this * 1000) } ?: Date(),
                        title = it.title.decodeHTML() ?: context.getString(R.string.empty),
                        author = it.author,
                        rawDescription = it.summary?.content ?: "",
                        shortDescription = Readability
                            .parseToText(it.summary?.content, findArticleURL(it)).take(110),
                        fullContent = it.summary?.content ?: "",
                        img = rssHelper.findImg(it.summary?.content ?: ""),
                        link = findArticleURL(it),
                        feedId = accountId.spacerDollar(it.origin?.streamId?.ofFeedStreamIdToId()
                            ?: feedIds.first()),
                        accountId = accountId,
                        isUnread = unreadIds?.contains(articleId) ?: true,
                        isStarred = starredIds?.contains(articleId) ?: false,
                        updateAt = it.crawlTimeMsec?.run { Date(this.toLong()) } ?: Date(),
                    )
                }?.toTypedArray() ?: emptyArray()
            )
        }
    }

    private fun findArticleURL(it: GoogleReaderDTO.Item) = it.canonical?.first()?.href
        ?: it.alternate?.first()?.href
        ?: it.origin?.htmlUrl ?: ""

    override suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        super.markAsRead(groupId, feedId, articleId, before, isUnread)
        val accountId = context.currentAccountId
        val googleReaderAPI = getGoogleReaderAPI()
        val markList: List<String> = when {
            groupId != null -> {
                if (before == null) {
                    articleDao.queryMetadataByGroupId(accountId, groupId)
                } else {
                    articleDao.queryMetadataByGroupId(accountId, groupId, before)
                }.map { it.id.dollarLast() }
            }

            feedId != null -> {
                if (before == null) {
                    articleDao.queryMetadataByFeedId(accountId, feedId)
                } else {
                    articleDao.queryMetadataByFeedId(accountId, feedId, before)
                }.map { it.id.dollarLast() }
            }

            articleId != null -> {
                listOf(articleId.dollarLast())
            }

            else -> {
                if (before == null) {
                    articleDao.queryMetadataAll(accountId)
                } else {
                    articleDao.queryMetadataAll(accountId, before)
                }.map { it.id.dollarLast() }
            }
        }
        if (markList.isNotEmpty()) googleReaderAPI.editTag(
            itemIds = markList,
            mark = if (!isUnread) GoogleReaderAPI.Stream.READ.tag else null,
            unmark = if (isUnread) GoogleReaderAPI.Stream.READ.tag else null,
        )
    }

    override suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        super.markAsStarred(articleId, isStarred)
        getGoogleReaderAPI().editTag(
            itemIds = listOf(articleId.dollarLast()),
            mark = if (isStarred) GoogleReaderAPI.Stream.STARRED.tag else null,
            unmark = if (!isStarred) GoogleReaderAPI.Stream.STARRED.tag else null,
        )
    }
}
