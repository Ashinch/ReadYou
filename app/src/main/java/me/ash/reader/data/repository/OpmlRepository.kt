package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import be.ceau.opml.OpmlWriter
import be.ceau.opml.entity.Body
import be.ceau.opml.entity.Head
import be.ceau.opml.entity.Opml
import be.ceau.opml.entity.Outline
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.currentAccountId
import me.ash.reader.data.account.AccountDao
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.group.GroupDao
import me.ash.reader.data.source.OpmlLocalDataSource
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
    private val opmlLocalDataSource: OpmlLocalDataSource
) {
    suspend fun saveToDatabase(inputStream: InputStream) {
        try {
            val defaultGroup = groupDao.queryById(opmlLocalDataSource.getDefaultGroupId())!!
            val groupWithFeedList =
                opmlLocalDataSource.parseFileInputStream(inputStream, defaultGroup)
            groupWithFeedList.forEach { groupWithFeed ->
                if (groupWithFeed.group != defaultGroup) {
                    groupDao.insert(groupWithFeed.group)
                }
                val repeatList = mutableListOf<Feed>()
                groupWithFeed.feeds.forEach {
                    it.groupId = groupWithFeed.group.id
                    if (rssRepository.get().isExist(it.url)) {
                        repeatList.add(it)
                    }
                }
                feedDao.insertList((groupWithFeed.feeds subtract repeatList).toList())
            }
        } catch (e: Exception) {
            Log.e("saveToDatabase", "${e.message}")
        }
    }

    suspend fun saveToString(): String? =
        try {
            val defaultGroup = groupDao.queryById(opmlLocalDataSource.getDefaultGroupId())!!
            OpmlWriter().write(
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
            )
        } catch (e: Exception) {
            Log.e("saveToString", "${e.message}")
            null
        }
}