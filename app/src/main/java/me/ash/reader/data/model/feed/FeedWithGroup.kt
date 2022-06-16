package me.ash.reader.data.model.feed

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.data.model.group.Group

/**
 * A [feed] contains a [group].
 */
data class FeedWithGroup(
    @Embedded
    var feed: Feed,
    @Relation(parentColumn = "groupId", entityColumn = "id")
    var group: Group,
)
