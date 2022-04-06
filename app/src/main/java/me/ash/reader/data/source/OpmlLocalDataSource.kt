package me.ash.reader.data.source

import android.content.Context
import be.ceau.opml.OpmlParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Group
import me.ash.reader.data.entity.GroupWithFeed
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.spacerDollar
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class OpmlLocalDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
) {
    @Throws(Exception::class)
    suspend fun parseFileInputStream(
        inputStream: InputStream,
        defaultGroup: Group
    ): List<GroupWithFeed> {
        return withContext(dispatcherIO) {
            val accountId = context.currentAccountId
            val opml = OpmlParser().parse(inputStream)
            val groupWithFeedList = mutableListOf<GroupWithFeed>().also {
                it.addGroup(defaultGroup)
            }

            opml.body.outlines.forEach {
                // Only feeds
                if (it.subElements.isEmpty()) {
                    // It's a empty group
                    if (it.attributes["xmlUrl"] == null) {
                        if (!it.attributes["isDefault"].toBoolean()) {
                            groupWithFeedList.addGroup(
                                Group(
                                    id = context.currentAccountId.spacerDollar(
                                        UUID.randomUUID().toString()
                                    ),
                                    name = it.attributes["title"] ?: it.text!!,
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
                                name = it.attributes["title"] ?: it.text!!,
                                url = it.attributes["xmlUrl"]!!,
                                groupId = defaultGroup.id,
                                accountId = accountId,
                                isNotification = it.attributes["isNotification"].toBoolean(),
                                isFullContent = it.attributes["isFullContent"].toBoolean(),
                            )
                        )
                    }
                } else {
                    var groupId = defaultGroup.id
                    if (!it.attributes["isDefault"].toBoolean()) {
                        groupId =
                            context.currentAccountId.spacerDollar(UUID.randomUUID().toString())
                        groupWithFeedList.addGroup(
                            Group(
                                id = groupId,
                                name = it.attributes["title"] ?: it.text!!,
                                accountId = accountId,
                            )
                        )
                    }
                    it.subElements.forEach { outline ->
                        groupWithFeedList.addFeed(
                            Feed(
                                id = context.currentAccountId.spacerDollar(
                                    UUID.randomUUID().toString()
                                ),
                                name = outline.attributes["title"] ?: outline.text!!,
                                url = outline.attributes["xmlUrl"]!!,
                                groupId = groupId,
                                accountId = accountId,
                                isNotification = outline.attributes["isNotification"].toBoolean(),
                                isFullContent = outline.attributes["isFullContent"].toBoolean(),
                            )
                        )
                    }
                }
            }
            groupWithFeedList
        }
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