package me.ash.reader.ui.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.ash.reader.R
import me.ash.reader.data.constant.Filter

@Composable
fun Filter.getName(): String = when (this) {
    Filter.Unread -> stringResource(R.string.unread)
    Filter.Starred -> stringResource(R.string.starred)
    else -> stringResource(R.string.all)
}

@Composable
fun Filter.getDesc(): String = when (this) {
    Filter.Unread -> stringResource(R.string.unread_desc, this.important)
    Filter.Starred -> stringResource(R.string.starred_desc, this.important)
    else -> stringResource(R.string.unread_desc, this.important)
}