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
    private val opmlLocalDataSource: OpmlLocalDataSource
) {
    suspend fun saveToDatabase(inputStream: InputStream) {
        try {
            val groupWithFeedList = opmlLocalDataSource.parseFileInputStream(inputStream)
            groupWithFeedList.forEach { groupWithFeed ->
                val id = groupDao.insert(groupWithFeed.group).toInt()
                groupWithFeed.feeds.forEach { it.groupId = id }
                feedDao.insertList(groupWithFeed.feeds)
            }
        } catch (e: Exception) {
            Log.e("saveToDatabase", "${e.message}")
        }
    }
}