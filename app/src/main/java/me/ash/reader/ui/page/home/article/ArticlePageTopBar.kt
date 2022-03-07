package me.ash.reader.ui.page.home.article

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.runtime.Composable

@Composable
fun ArticlePageTopBar(
    backOnClick: () -> Unit = {},
    readAllOnClick: () -> Unit = {},
    searchOnClick: () -> Unit = {},
) {
    SmallTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = backOnClick) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = readAllOnClick) {
                Icon(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = "Done All",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = searchOnClick) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
    )
}