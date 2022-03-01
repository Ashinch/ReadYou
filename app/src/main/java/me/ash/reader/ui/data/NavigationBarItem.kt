package me.ash.reader.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

class NavigationBarItem(
    var title: String,
    var icon: ImageVector,
) {
    companion object {
        val Starred = NavigationBarItem("STARRED", Icons.Rounded.Star)
        val Unread = NavigationBarItem("UNREAD", Icons.Rounded.FiberManualRecord)
        val All = NavigationBarItem("ALL", Icons.Rounded.Subject)
    }
}