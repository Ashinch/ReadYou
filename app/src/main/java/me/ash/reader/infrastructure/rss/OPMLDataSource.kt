package me.ash.reader.infrastructure.rss

import android.content.Context
import be.ceau.opml.OpmlParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.extractDomain
import me.ash.reader.ui.ext.spacerDollar
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class OPMLDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {

    @Throws(Exception::class)
    suspend fun parseFileInputStream(
        inputStream: InputStream,
        defaultGroup: Group,
    ): List<GroupWithFeed> {
        val accountId = context.currentAccountId
        val opml = OpmlParser().parse(inputStream)
        val groupWithFeedList = mutableListOf<GroupWithFeed>().also {
            it.addGroup(defaultGroup)
        }

        opml.body.outlines.forEach {
            // Only feeds
            if (it.subElements.isEmpty()) {
                // It's a empty group
                if (!it.attributes.containsKey("xmlUrl")) {
                    if (!it.attributes.getOrDefault("isDefault", null).toBoolean()) {
                        groupWithFeedList.addGroup(
                            Group(
                                id = context.currentAccountId.spacerDollar(
                                    UUID.randomUUID().toString()
                                ),
                                name = it.attributes.getOrDefault("title", null) ?: it.text!!,
                                accountId = accountId,
                            )
                        )
                    }
                } else {
                    groupWithFeedList.addFeedToDefault(
                        Feed(
                            id = context.currentAccountId.spacerDollar(
                                UUID.randomUUID().toString()
                            ),
                            name = it.attributes.getOrDefault("title", null) ?: it.text!!,
                            url = it.attributes.getOrDefault("xmlUrl", null)
                                ?: throw IllegalArgumentException("xmlUrl is null"),
                            groupId = defaultGroup.id,
                            accountId = accountId,
                            isNotification = it.attributes.getOrDefault("isNotification", null)
                                .toBoolean(),
                            isFullContent = it.attributes.getOrDefault("isFullContent", null)
                                .toBoolean(),
                        )
                    )
                }
            } else {
                var groupId = defaultGroup.id
                if (!it.attributes.getOrDefault("isDefault", null).toBoolean()) {
                    groupId =
                        context.currentAccountId.spacerDollar(UUID.randomUUID().toString())
                    groupWithFeedList.addGroup(
                        Group(
                            id = groupId,
                            name = it.attributes.getOrDefault("title", null) ?: it.text!!,
                            accountId = accountId,
                        )
                    )
                }
                it.subElements.forEach { outline ->
                    if (outline != null && outline.attributes != null) {
                        val xmlUrl = outline.attributes.getOrDefault("xmlUrl", null)
                            ?: throw IllegalArgumentException("${outline.attributes} xmlUrl is null")
                        groupWithFeedList.addFeed(
                            Feed(
                                id = context.currentAccountId.spacerDollar(
                                    UUID.randomUUID().toString()
                                ),
                                name = outline.attributes.getOrDefault("title", null)
                                    ?: outline.text ?: xmlUrl.extractDomain(),
                                url = xmlUrl,
                                groupId = groupId,
                                accountId = accountId,
                                isNotification = outline.attributes.getOrDefault("isNotification",
                                    null).toBoolean(),
                                isFullContent = outline.attributes.getOrDefault("isFullContent",
                                    null).toBoolean(),
                            )
                        )
                    }
                }
            }
        }
        return groupWithFeedList
    }

    private fun MutableList<GroupWithFeed>.addGroup(group: Group) {
        add(GroupWithFeed(group = group, feeds = mutableListOf()))
    }

    private fun MutableList<GroupWithFeed>.addFeed(feed: Feed) {
        last().feeds.add(feed)
    }

    private fun MutableList<GroupWithFeed>.addFeedToDefault(feed: Feed) {
        first().feeds.add(feed)
    }
}
