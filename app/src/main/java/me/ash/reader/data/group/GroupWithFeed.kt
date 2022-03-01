package me.ash.reader.data.group

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.feed.Feed

data class GroupWithFeed(
    @Embedded
    val group: Group,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    val feeds: MutableList<Feed>
)
