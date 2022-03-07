package me.ash.reader.ui.page.home.read

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReadPageTopBar(
    btnBackOnClickListener: () -> Unit = {},
) {
    SmallTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { btnBackOnClickListener() }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}