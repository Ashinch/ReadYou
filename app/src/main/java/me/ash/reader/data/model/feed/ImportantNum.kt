package me.ash.reader.data.model.feed

/**
 * Counting the [important] number of articles in feeds and groups is generally
 * used in three situations.
 *
 * - Unread: Articles that have not been read yet
 * - Starred: Articles that have been marked as starred
 * - All: All articles
 */
data class ImportantNum(
    val important: Int,
    val feedId: String,
    val groupId: String,
)
