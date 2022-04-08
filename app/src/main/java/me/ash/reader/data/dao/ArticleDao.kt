package me.ash.reader.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.entity.ImportantCount
import java.util.*

@Dao
interface ArticleDao {
    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId IN (
            SELECT id FROM feed WHERE groupId = :groupId
        )
        AND isUnread = :isUnread
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleByGroupIdWhenIsUnread(
        accountId: Int,
        text: String,
        groupId: String,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId IN (
            SELECT id FROM feed WHERE groupId = :groupId
        )
        AND isStarred = :isStarred
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleByGroupIdWhenIsStarred(
        accountId: Int,
        text: String,
        groupId: String,
        isStarred: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId IN (
            SELECT id FROM feed WHERE groupId = :groupId
        )
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleByGroupIdWhenAll(
        accountId: Int,
        text: String,
        groupId: String,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId = :feedId
        AND isUnread = :isUnread
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleByFeedIdWhenIsUnread(
        accountId: Int,
        text: String,
        feedId: String,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId = :feedId
        AND isStarred = :isStarred
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleByFeedIdWhenIsStarred(
        accountId: Int,
        text: String,
        feedId: String,
        isStarred: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId = :feedId 
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleByFeedIdWhenAll(
        accountId: Int,
        text: String,
        feedId: String,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND isUnread = :isUnread
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleWhenIsUnread(
        accountId: Int,
        text: String,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND isStarred = :isStarred
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleWhenIsStarred(
        accountId: Int,
        text: String,
        isStarred: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY date DESC
        """
    )
    fun searchArticleWhenAll(
        accountId: Int,
        text: String,
    ): PagingSource<Int, ArticleWithFeed>

    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE accountId = :accountId
        AND date < :before
        """
    )
    suspend fun markAllAsRead(
        accountId: Int,
        isUnread: Boolean,
        before: Date,
    )

    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE feedId IN (
            SELECT id FROM feed 
            WHERE groupId = :groupId
        )
        AND accountId = :accountId
        AND date < :before
        """
    )
    suspend fun markAllAsReadByGroupId(
        accountId: Int,
        groupId: String,
        isUnread: Boolean,
        before: Date,
    )

    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE feedId = :feedId
        AND accountId = :accountId
        AND date < :before
        """
    )
    suspend fun markAllAsReadByFeedId(
        accountId: Int,
        feedId: String,
        isUnread: Boolean,
        before: Date,
    )

    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE id = :articleId
        AND accountId = :accountId
        """
    )
    suspend fun markAsReadByArticleId(
        accountId: Int,
        articleId: String,
        isUnread: Boolean,
    )

    @Query(
        """
        DELETE FROM article
        WHERE accountId = :accountId
        AND feedId = :feedId
        """
    )
    suspend fun deleteByFeedId(accountId: Int, feedId: String)

    @Query(
        """
        DELETE FROM article
        WHERE id IN (
            SELECT a.id FROM article AS a, feed AS b, `group` AS c
            WHERE a.accountId = :accountId
            AND a.feedId = b.id
            AND b.groupId = c.id
            AND c.id = :groupId
        )
        """
    )
    suspend fun deleteByGroupId(accountId: Int, groupId: String)

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
        a.accountId, a.isUnread, a.isStarred, a.isReadLater 
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
        groupId: String,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred, a.isReadLater 
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
        groupId: String,
        isStarred: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred, a.isReadLater 
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
        groupId: String,
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
        feedId: String
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
        feedId: String,
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
        feedId: String,
        isUnread: Boolean,
    ): PagingSource<Int, ArticleWithFeed>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred, a.isReadLater 
        FROM article AS a LEFT JOIN feed AS b 
        ON a.feedId = b.id
        WHERE a.feedId = :feedId 
        AND a.accountId = :accountId
        ORDER BY date DESC LIMIT 1
        """
    )
    suspend fun queryLatestByFeedId(accountId: Int, feedId: String): Article?

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE id = :id
        """
    )
    suspend fun queryById(id: String): ArticleWithFeed?

    @Insert
    suspend fun insert(article: Article): Long

    @Insert
    suspend fun insertList(articles: List<Article>): List<Long>

    @Update
    suspend fun update(vararg article: Article)

    @Delete
    suspend fun delete(vararg article: Article)
}