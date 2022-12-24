package me.ash.reader.data.repository

import android.content.Context
import be.ceau.opml.OpmlWriter
import be.ceau.opml.entity.Body
import be.ceau.opml.entity.Head
import be.ceau.opml.entity.Opml
import be.ceau.opml.entity.Outline
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.data.dao.AccountDao
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.dao.GroupDao
import me.ash.reader.data.model.feed.Feed
import me.ash.reader.data.source.OPMLDataSource
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.getDefaultGroupId
import java.io.InputStream
import java.util.*
import javax.inject.Inject

/**
 * Supports import and export from OPML files.
 */
class OpmlRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val accountDao: AccountDao,
    private val rssRepository: RssRepository,
    private val OPMLDataSource: OPMLDataSource,
) {

    /**
     * Imports OPML file.
     *
     * @param [inputStream] input stream of OPML file
     */
    @Throws(Exception::class)
    suspend fun saveToDatabase(inputStream: InputStream) {
        val defaultGroup = groupDao.queryById(getDefaultGroupId(context.currentAccountId))!!
        val groupWithFeedList =
            OPMLDataSource.parseFileInputStream(inputStream, defaultGroup)
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
            feedDao.insertList((groupWithFeed.feeds subtract repeatList.toSet()).toList())
        }
    }

    /**
     * Exports OPML file.
     */
    @Throws(Exception::class)
    suspend fun saveToString(accountId: Int): String {
        val defaultGroup = groupDao.queryById(getDefaultGroupId(accountId))!!
        return OpmlWriter().write(
            Opml(
                "2.0",
                Head(
                    accountDao.queryById(accountId)?.name,
                    Date().toString(), null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                ),
                Body(groupDao.queryAllGroupWithFeed(accountId).map {
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

    private fun getDefaultGroupId(accountId: Int): String = accountId.getDefaultGroupId()
}
