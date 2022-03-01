package me.ash.reader.data.feed

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.group.Group

data class FeedWithGroup(
    @Embedded
    val feed: Feed,
    @Relation(parentColumn = "groupId", entityColumn = "id")
    val group: Group
)
