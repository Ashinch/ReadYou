package me.ash.reader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GroupWithFeed(
    @Embedded
    var group: Group,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    var feeds: MutableList<Feed>
)
