package me.ash.reader.data.source

object FeverApiDto {
    // &groups
    data class Groups(
        val apiVersion: Int,
        val auth: Int,
        val lastRefreshedOnTime: Long,
        val groups: List<GroupItem>,
        val feedsGroups: List<FeedsGroupsItem>,
    )

    data class GroupItem(
        val id: Int,
        val title: String,
    )

    data class FeedsGroupsItem(
        val groupId: Int,
        val feedsIds: String,
    )

    // &feeds
    data class Feed(
        val apiVersion: Int,
        val auth: Int,
        val lastRefreshedOnTime: Long,
        val feeds: List<FeedItem>,
        val feedsGroups: List<FeedsGroupsItem>,
    )

    data class FeedItem(
        val id: Int,
        val favicon_id: Int,
        val title: String,
        val url: String,
        val siteUrl: String,
        val isSpark: Int,
        val lastRefreshedOnTime: Long,
    )

    // &favicons
    data class Favicons(
        val apiVersion: Int,
        val auth: Int,
        val lastRefreshedOnTime: Long,
        val favicons: List<FaviconItem>,
    )

    data class FaviconItem(
        val id: Int,
        val data: String,
    )
}