package me.ash.reader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GroupWithFeed(
    @Embedded
    val group: Group,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    val feeds: MutableList<Feed>
)
