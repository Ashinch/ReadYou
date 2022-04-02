package me.ash.reader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FeedWithGroup(
    @Embedded
    val feed: Feed,
    @Relation(parentColumn = "groupId", entityColumn = "id")
    val group: Group
)
