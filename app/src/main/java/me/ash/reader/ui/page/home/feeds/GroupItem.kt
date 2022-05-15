package me.ash.reader.ui.page.home.feeds

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Group
import me.ash.reader.ui.ext.alphaLN
import me.ash.reader.ui.page.home.feeds.option.group.GroupOptionViewAction
import me.ash.reader.ui.page.home.feeds.option.group.GroupOptionViewModel

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupItem(
    modifier: Modifier = Modifier,
    tonalElevation: Dp,
    group: Group,
    feeds: List<Feed>,
    isExpanded: Boolean = true,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    groupOnClick: () -> Unit = {},
    feedOnClick: (feed: Feed) -> Unit = {},
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(isExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                MaterialTheme.colorScheme.secondary.copy(alpha = tonalElevation.alphaLN(weight = 1.2f))
            )
            .combinedClickable(
                onClick = {
                    groupOnClick()
                },
                onLongClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    groupOptionViewModel.dispatch(GroupOptionViewAction.Show(scope, group.id))
                }
            )
            .padding(top = 22.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 28.dp),
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.surfaceTint.copy(
                            alpha = tonalElevation.alphaLN(weight = 1.4f)
                        )
                    )
                    .clickable {
                        expanded = !expanded
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(if (expanded) R.string.expand_less else R.string.expand_more),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                feeds.forEach { feed ->
                    FeedItem(
                        feed = feed,
                        tonalElevation = tonalElevation,
                    ) {
                        feedOnClick(feed)
                    }
                }
                if (feeds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}