package me.ash.reader.data.feed

import androidx.room.*

@Dao
interface FeedDao {
    @Query(
        """
        SELECT * FROM feed
        WHERE accountId = :accountId
        """
    )
    suspend fun queryAll(accountId: Int): List<Feed>

    @Query(
        """
        SELECT * FROM feed
        WHERE accountId = :accountId
        and url = :url
        """
    )
    fun queryByLink(accountId: Int, url: String): List<Feed>

    @Insert
    suspend fun insert(feed: Feed): Long

    @Insert
    suspend fun insertList(feeds: List<Feed>): List<Long>

    @Update
    suspend fun update(vararg feed: Feed)

    @Delete
    suspend fun delete(vararg feed: Feed)
}