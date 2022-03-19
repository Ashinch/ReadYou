package me.ash.reader.data.constant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.ui.graphics.vector.ImageVector

class Filter(
    var index: Int,
    var title: String,
    var description: String,
    var important: Int,
    var icon: ImageVector,
) {
    companion object {
        val Starred = Filter(
            index = 0,
            title = "Starred",
            description = " Starred Items",
            important = 13,
            icon = Icons.Rounded.StarOutline,
        )
        val Unread = Filter(
            index = 1,
            title = "Unread",
            description = " Unread Items",
            important = 666,
            icon = Icons.Outlined.FiberManualRecord,
        )
        val All = Filter(
            index = 2,
            title = "All",
            description = " Unread Items",
            important = 666,
            icon = Icons.Rounded.Subject,
        )
    }
}