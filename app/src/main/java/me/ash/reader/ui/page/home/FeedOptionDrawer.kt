package me.ash.reader.ui.page.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.ui.page.home.feeds.subscribe.ResultViewPage
import me.ash.reader.ui.widget.BottomDrawer
import me.ash.reader.ui.widget.Subtitle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedOptionDrawer(
    modifier: Modifier = Modifier,
    drawerState: ModalBottomSheetState = androidx.compose.material.rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden
    ),
) {
    BottomDrawer(
        drawerState = drawerState,
    ) {
        Column {
            Icon(
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = stringResource(R.string.subscribe),
            )
            Spacer(modifier = modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Feed",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = modifier.height(16.dp))
            ResultViewPage(
                link = "https://joeycz.github.io/weekly/rss.xml",
                groups = emptyList(),
                selectedAllowNotificationPreset = true,
                selectedParseFullContentPreset = true,
                selectedGroupId = "selectedGroupId",
                newGroupContent = "",
                onNewGroupValueChange = { },
                newGroupSelected = false,
                changeNewGroupSelected = { },
                allowNotificationPresetOnClick = { },
                parseFullContentPresetOnClick = { },
                groupOnClick = { },
                onKeyboardAction = { },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Subtitle(text = "More")
            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.material.FilterChip(
                modifier = modifier,
                colors = ChipDefaults.filterChipColors(
                    backgroundColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                    disabledBackgroundColor = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.7f
                    ),
                    disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    disabledLeadingIconColor = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.7f
                    ),
                    selectedBackgroundColor = Color.Transparent,
                    selectedContentColor = MaterialTheme.colorScheme.error,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                selected = false,
                shape = CircleShape,
                onClick = {
//                            focusManager.clearFocus()
//                            onClick()
                },
                content = {
                    Text(
                        modifier = modifier.padding(
                            start = if (false) 0.dp else 8.dp,
                            top = 8.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        ),
                        text = "Delete",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (true) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                },
            )
        }
    }
}