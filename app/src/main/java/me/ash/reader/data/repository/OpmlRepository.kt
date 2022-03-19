package me.ash.reader.data.repository

import android.util.Log
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
                groupWithFeed.feeds.forEach { it.groupId = groupWithFeed.group.id }
                groupWithFeed.feeds.removeIf {
                    rssRepository.get().isExist(it.url)
                }
                feedDao.insertList(groupWithFeed.feeds)
            }
        } catch (e: Exception) {
            Log.e("saveToDatabase", "${e.message}")
        }
    }
}