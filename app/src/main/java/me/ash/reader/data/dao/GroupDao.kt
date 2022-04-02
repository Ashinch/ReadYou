package me.ash.reader.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.ash.reader.data.entity.Group
import me.ash.reader.data.entity.GroupWithFeed

@Dao
interface GroupDao {
    @Query(
        """
        SELECT * FROM `group`
        WHERE id = :id
        """
    )
    fun queryById(id: String): Group?

    @Transaction
    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroupWithFeedAsFlow(accountId: Int): Flow<MutableList<GroupWithFeed>>

    @Transaction
    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroupWithFeed(accountId: Int): List<GroupWithFeed>

    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroup(accountId: Int): Flow<MutableList<Group>>

    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    suspend fun queryAll(accountId: Int): List<Group>

    @Insert
    suspend fun insert(group: Group): Long

    @Update
    suspend fun update(vararg group: Group)

    @Delete
    suspend fun delete(vararg group: Group)
}