package me.ash.reader.ui.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.ash.reader.R
import me.ash.reader.data.entity.Filter

@Composable
fun Filter.getName(): String = when (this) {
    Filter.Unread -> stringResource(R.string.unread)
    Filter.Starred -> stringResource(R.string.starred)
    else -> stringResource(R.string.all)
}
