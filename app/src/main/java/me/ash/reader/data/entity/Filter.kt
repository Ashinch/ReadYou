package me.ash.reader.data.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.ui.graphics.vector.ImageVector

class Filter(
    val index: Int,
    val iconOutline: ImageVector,
    val iconFilled: ImageVector,
) {
    fun isStarred(): Boolean = this == Starred
    fun isUnread(): Boolean = this == Unread
    fun isAll(): Boolean = this == All

    companion object {
        val Starred = Filter(
            index = 0,
            iconOutline = Icons.Rounded.StarOutline,
            iconFilled = Icons.Rounded.Star,
        )
        val Unread = Filter(
            index = 1,
            iconOutline = Icons.Outlined.FiberManualRecord,
            iconFilled = Icons.Rounded.FiberManualRecord,
        )
        val All = Filter(
            index = 2,
            iconOutline = Icons.Rounded.Subject,
            iconFilled = Icons.Rounded.Subject,
        )
    }
}