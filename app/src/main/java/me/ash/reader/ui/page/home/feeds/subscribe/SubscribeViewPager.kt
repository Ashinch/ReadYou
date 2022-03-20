package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import me.ash.reader.data.group.Group
import me.ash.reader.ui.widget.ViewPager

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SubscribeViewPager(
    height: Dp = Dp.Unspecified,
    inputContent: String = "",
    errorMessage: String = "",
    onValueChange: (String) -> Unit = {},
    onSearchKeyboardAction: () -> Unit = {},
    link: String = "",
    groups: List<Group> = emptyList(),
    selectedAllowNotificationPreset: Boolean = false,
    selectedParseFullContentPreset: Boolean = false,
    selectedGroupId: String = "",
    pagerState: PagerState = com.google.accompanist.pager.rememberPagerState(),
    allowNotificationPresetOnClick: () -> Unit = {},
    parseFullContentPresetOnClick: () -> Unit = {},
    groupOnClick: (groupId: String) -> Unit = {},
    onResultKeyboardAction: () -> Unit = {},
) {
    ViewPager(
        modifier = Modifier.height(height),
        state = pagerState,
        userScrollEnabled = false,
        composableList = listOf(
            {
                SearchViewPage(
                    pagerState = pagerState,
                    inputContent = inputContent,
                    errorMessage = errorMessage,
                    onValueChange = onValueChange,
                    onKeyboardAction = onSearchKeyboardAction,
                )
            },
            {
                ResultViewPage(
                    link = link,
                    groups = groups,
                    selectedAllowNotificationPreset = selectedAllowNotificationPreset,
                    selectedParseFullContentPreset = selectedParseFullContentPreset,
                    selectedGroupId = selectedGroupId,
                    allowNotificationPresetOnClick = allowNotificationPresetOnClick,
                    parseFullContentPresetOnClick = parseFullContentPresetOnClick,
                    groupOnClick = groupOnClick,
                    onKeyboardAction = onResultKeyboardAction,
                )
            }
        )
    )
}