package me.ash.reader.data.group

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Transaction
    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroupWithFeed(accountId: Int): Flow<MutableList<GroupWithFeed>>

    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroup(accountId: Int): Flow<MutableList<Group>>

    @Insert
    suspend fun insert(group: Group): Long

    @Update
    suspend fun update(vararg group: Group)

    @Delete
    suspend fun delete(vararg group: Group)
}