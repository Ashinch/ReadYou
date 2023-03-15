package me.ash.reader.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.ash.reader.data.model.group.Group
import me.ash.reader.data.model.group.GroupWithFeed

@Dao
interface GroupDao {

    @Query(
        """
        SELECT * FROM `group`
        WHERE id = :id
        """
    )
    suspend fun queryById(id: String): Group?

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
    suspend fun queryAllGroupWithFeed(accountId: Int): List<GroupWithFeed>

    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroup(accountId: Int): Flow<MutableList<Group>>

    @Query(
        """
        DELETE FROM `group`
        WHERE accountId = :accountId
        """
    )
    suspend fun deleteByAccountId(accountId: Int)

    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    suspend fun queryAll(accountId: Int): List<Group>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg group: Group)

    @Update
    suspend fun update(vararg group: Group)

    @Delete
    suspend fun delete(vararg group: Group)

    suspend fun insertOrUpdate(groups: List<Group>)  {
        groups.forEach {
            val group = queryById(it.id)
            if (group == null) {
                insert(it)
            } else {
                update(it)
            }
        }
    }
}
