package me.ash.reader.data.repository

import android.util.Log
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.group.GroupDao
import me.ash.reader.data.source.OpmlLocalDataSource
import java.io.InputStream
import javax.inject.Inject

class OpmlRepository @Inject constructor(
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val rssRepository: RssRepository,
    private val opmlLocalDataSource: OpmlLocalDataSource
) {
    suspend fun saveToDatabase(inputStream: InputStream) {
        try {
            val groupWithFeedList = opmlLocalDataSource.parseFileInputStream(inputStream)
            groupWithFeedList.forEach { groupWithFeed ->
                groupDao.insert(groupWithFeed.group)
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
}