package me.ash.reader.data.article

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId
        AND (
            title LIKE :keyword
            OR rawDescription LIKE :keyword
            OR fullContent LIKE :keyword
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleWithFeedWhenIsAll(
        accountId: Int,
        keyword: String,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE isUnread = :isUnread 
        AND accountId = :accountId
        AND (
            title LIKE :keyword
            OR rawDescription LIKE :keyword
            OR fullContent LIKE :keyword
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleWithFeedWhenIsUnread(
        accountId: Int,
        isUnread: Boolean,
        keyword: String,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE isStarred = :isStarred 
        AND accountId = :accountId
        AND (
            title LIKE :keyword
            OR rawDescription LIKE :keyword
            OR fullContent LIKE :keyword
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleWithFeedWhenIsStarred(
        accountId: Int,
        isStarred: Boolean,
        keyword: String,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT COUNT(*) AS important, a.feedId, b.groupId
        FROM article AS a
        LEFT JOIN feed AS b
        ON a.feedId = b.id
        WHERE a.isUnread = :isUnread
        AND a.accountId = :accountId
        GROUP BY a.feedId
        """
    )
    fun queryImportantCountWhenIsUnread(
        accountId: Int,
        isUnread: Boolean
    ): Flow<List<ImportantCount>>

    @Transaction
    @Query(
        """
        SELECT COUNT(*) AS important, a.feedId, b.groupId 
        FROM article AS a
        LEFT JOIN feed AS b
        ON a.feedId = b.id
        WHERE a.isStarred = :isStarred
        AND a.accountId = :accountId
        GROUP BY a.feedId
        """
    )
    fun queryImportantCountWhenIsStarred(
        accountId: Int,
        isStarred: Boolean
    ): Flow<List<ImportantCount>>

    @Transaction
    @Query(
        """
        SELECT COUNT(*) AS important, a.feedId, b.groupId 
        FROM article AS a
        LEFT JOIN feed AS b
        ON a.feedId = b.id
        WHERE a.accountId = :accountId
        GROUP BY a.feedId
        """
    )
    fun queryImportantCountWhenIsAll(accountId: Int): Flow<List<ImportantCount>>

    @Transaction
    @Query(
        """
        SELECT * FROM article 
        WHERE accountId = :accountId
        ORDER BY date DESC
        """
    )
    fun queryArticleWithFeedWhenIsAll(accountId: Int): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE isStarred = :isStarred 
        AND accountId = :accountId
        ORDER BY date DESC
        """
    )
    fun queryArticleWithFeedWhenIsStarred(
        accountId: Int,
        isStarred: Boolean
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article 
        WHERE isUnread = :isUnread 
        AND accountId = :accountId
        ORDER BY date DESC
        """
    )
    fun queryArticleWithFeedWhenIsUnread(
        accountId: Int,
        isUnread: Boolean
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.accountId = :accountId
        ORDER BY a.date DESC
        """
    )
    fun queryArticleWithFeedByGroupIdWhenIsAll(
        accountId: Int,
        groupId: Int
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.isStarred = :isStarred
        AND a.accountId = :accountId
        ORDER BY a.date DESC
        """
    )
    fun queryArticleWithFeedByGroupIdWhenIsStarred(
        accountId: Int,
        groupId: Int,
        isStarred: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.isUnread = :isUnread
        AND a.accountId = :accountId
        ORDER BY a.date DESC
        """
    )
    fun queryArticleWithFeedByGroupIdWhenIsUnread(
        accountId: Int,
        groupId: Int,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE feedId = :feedId
        AND accountId = :accountId
        """
    )
    fun queryArticleWithFeedByFeedIdWhenIsAll(
        accountId: Int,
        feedId: Int
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * from article 
        WHERE feedId = :feedId 
        AND isStarred = :isStarred
        AND accountId = :accountId
        """
    )
    fun queryArticleWithFeedByFeedIdWhenIsStarred(
        accountId: Int,
        feedId: Int,
        isStarred: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article 
        WHERE feedId = :feedId 
        AND isUnread = :isUnread
        AND accountId = :accountId
        """
    )
    fun queryArticleWithFeedByFeedIdWhenIsUnread(
        accountId: Int,
        feedId: Int,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred 
        FROM article AS a, feed AS b 
        WHERE a.feedId = :feedId 
        AND a.accountId = :accountId
        ORDER BY date DESC LIMIT 1
        """
    )
    suspend fun queryLatestByFeedId(accountId: Int, feedId: Int): Article?

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE id = :id
        """
    )
    suspend fun queryById(id: Int): ArticleWithFeed?

    @Insert
    suspend fun insert(article: Article): Long

    @Insert
    suspend fun insertList(articles: List<Article>): List<Long>

    @Update
    suspend fun update(vararg article: Article)

    @Delete
    suspend fun delete(vararg article: Article)
}