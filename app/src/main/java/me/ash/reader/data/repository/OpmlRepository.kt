package me.ash.reader.data.repository

import android.content.Context
import be.ceau.opml.OpmlWriter
import be.ceau.opml.entity.Body
import be.ceau.opml.entity.Head
import be.ceau.opml.entity.Opml
import be.ceau.opml.entity.Outline
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.R
import me.ash.reader.currentAccountId
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.group.GroupDao
import me.ash.reader.data.source.OpmlLocalDataSource
import me.ash.reader.spacerDollar
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class OpmlRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val accountDao: AccountDao,
    private val rssRepository: RssRepository,
    private val opmlLocalDataSource: OpmlLocalDataSource,
    private val stringsRepository: StringsRepository,
) {
    @Throws(Exception::class)
    suspend fun saveToDatabase(inputStream: InputStream) {
        val defaultGroup = groupDao.queryById(getDefaultGroupId())!!
        val groupWithFeedList =
            opmlLocalDataSource.parseFileInputStream(inputStream, defaultGroup)
        groupWithFeedList.forEach { groupWithFeed ->
            if (groupWithFeed.group != defaultGroup) {
                groupDao.insert(groupWithFeed.group)
            }
            val repeatList = mutableListOf<Feed>()
            groupWithFeed.feeds.forEach {
                it.groupId = groupWithFeed.group.id
                if (rssRepository.get().isFeedExist(it.url)) {
                    repeatList.add(it)
                }
            }
            feedDao.insertList((groupWithFeed.feeds subtract repeatList).toList())
        }
    }

    @Throws(Exception::class)
    suspend fun saveToString(): String {
        val defaultGroup = groupDao.queryById(getDefaultGroupId())!!
        return OpmlWriter().write(
            Opml(
                "2.0",
                Head(
                    accountDao.queryById(context.currentAccountId).name,
                    Date().toString(), null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                ),
                Body(groupDao.queryAllGroupWithFeed(context.currentAccountId).map {
                    Outline(
                        mapOf(
                            "text" to it.group.name,
                            "title" to it.group.name,
                            "isDefault" to (it.group.id == defaultGroup.id).toString()
                        ),
                        it.feeds.map { feed ->
                            Outline(
                                mapOf(
                                    "text" to feed.name,
                                    "title" to feed.name,
                                    "xmlUrl" to feed.url,
                                    "htmlUrl" to feed.url,
                                    "isNotification" to feed.isNotification.toString(),
                                    "isFullContent" to feed.isFullContent.toString(),
                                ),
                                listOf()
                            )
                        }
                    )
                })
            )
        )!!
    }

    private fun getDefaultGroupId(): String {
        val readYouString = stringsRepository.getString(R.string.read_you)
        val defaultString = stringsRepository.getString(R.string.defaults)
        return context.currentAccountId.spacerDollar(readYouString + defaultString)
    }
}