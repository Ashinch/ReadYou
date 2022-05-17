package me.ash.reader.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import me.ash.reader.R

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
        val values = listOf(Starred, Unread, All)
    }
}

@Composable
fun Filter.getName(): String = when (this) {
    Filter.Unread -> stringResource(R.string.unread)
    Filter.Starred -> stringResource(R.string.starred)
    else -> stringResource(R.string.all)
}