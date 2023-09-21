package me.ash.reader.infrastructure.rss.provider.greader

object GoogleReaderApiDto {
    // subscription/list?output=json
    data class SubscriptionList(
        val subscriptions: List<SubscriptionItem>? = null,
    )

    data class SubscriptionItem(
        val id: String? = null,
        val title: String? = null,
        val categories: List<CategoryItem>? = null,
        val url: String? = null,
        val htmlUrl: String? = null,
        val iconUrl: String? = null,
    )

    data class CategoryItem(
        val id: String? = null,
        val label: String? = null,
    )

    // unread-count?output=json
    data class UnreadCount(
        val max: Int? = null,
        val unreadcounts: List<UnreadCountItem>? = null,
    )

    data class UnreadCountItem(
        val id: String? = null,
        val count: Int? = null,
        val newestItemTimestampUsec: String? = null,
    )

    // tag/list?output=json
    data class TagList(
        val tags: List<TagItem>? = null,
    )

    data class TagItem(
        val id: String? = null,
        val type: String? = null,
    )

    // stream/contents/reading-list?output=json
    data class ReadingList(
        val id: String? = null,
        val updated: Long? = null,
        val items: List<Item>? = null,
    )

    data class Item(
        val id: String? = null,
        val crawlTimeMsec: String? = null,
        val timestampUsec: String? = null,
        val published: Long? = null,
        val title: String? = null,
        val summary: Summary? = null,
        val categories: List<String>? = null,
        val origin: List<OriginItem>? = null,
        val author: String? = null,
    )

    data class Summary(
        val content: String? = null,
        val canonical: List<CanonicalItem>? = null,
        val alternate: List<CanonicalItem>? = null,
    )

    data class CanonicalItem(
        val href: String? = null,
    )

    data class OriginItem(
        val streamId: String? = null,
        val title: String? = null,
    )
}
