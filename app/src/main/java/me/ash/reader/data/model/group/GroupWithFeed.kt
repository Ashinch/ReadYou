package me.ash.reader.data.model.group

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.model.feed.Feed

data class GroupWithFeed(
    @Embedded
    var group: Group,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    var feeds: MutableList<Feed>,
)
