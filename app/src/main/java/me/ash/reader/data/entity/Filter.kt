package me.ash.reader.data.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.ui.graphics.vector.ImageVector

class Filter(
    var index: Int,
    var icon: ImageVector,
) {
    fun isStarred(): Boolean = this == Starred
    fun isUnread(): Boolean = this == Unread
    fun isAll(): Boolean = this == All

    companion object {
        val Starred = Filter(
            index = 0,
            icon = Icons.Rounded.StarOutline,
        )
        val Unread = Filter(
            index = 1,
            icon = Icons.Outlined.FiberManualRecord,
        )
        val All = Filter(
            index = 2,
            icon = Icons.Rounded.Subject,
        )
    }
}