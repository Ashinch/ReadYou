package me.ash.reader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FeedWithGroup(
    @Embedded
    var feed: Feed,
    @Relation(parentColumn = "groupId", entityColumn = "id")
    var group: Group
)
