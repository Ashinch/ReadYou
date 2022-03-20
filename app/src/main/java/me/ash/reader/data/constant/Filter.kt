package me.ash.reader.data.constant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.ui.graphics.vector.ImageVector

class Filter(
    var index: Int,
    var important: Int,
    var icon: ImageVector,
) {
    fun isStarred(): Boolean = this == Starred
    fun isUnread(): Boolean = this == Unread
    fun isAll(): Boolean = this == All

    companion object {
        val Starred = Filter(
            index = 0,
            important = 13,
            icon = Icons.Rounded.StarOutline,
        )
        val Unread = Filter(
            index = 1,
            important = 666,
            icon = Icons.Outlined.FiberManualRecord,
        )
        val All = Filter(
            index = 2,
            important = 666,
            icon = Icons.Rounded.Subject,
        )
    }
}