package me.ash.reader.ui.page.home.feed.subscribe

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
    selectedNotificationPreset: Boolean = false,
    selectedFullContentParsePreset: Boolean = false,
    selectedGroupId: Int = 0,
    pagerState: PagerState = com.google.accompanist.pager.rememberPagerState(),
    notificationPresetOnClick: () -> Unit = {},
    fullContentParsePresetOnClick: () -> Unit = {},
    groupOnClick: (groupId: Int) -> Unit = {},
    onResultKeyboardAction: () -> Unit = {},
) {
    ViewPager(
        modifier = Modifier.height(height),
        state = pagerState,
        userScrollEnabled = false,
        composableList = listOf(
            {
                SearchViewPage(
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
                    selectedNotificationPreset = selectedNotificationPreset,
                    selectedFullContentParsePreset = selectedFullContentParsePreset,
                    selectedGroupId = selectedGroupId,
                    notificationPresetOnClick = notificationPresetOnClick,
                    fullContentParsePresetOnClick = fullContentParsePresetOnClick,
                    groupOnClick = groupOnClick,
                    onKeyboardAction = onResultKeyboardAction,
                )
            }
        )
    )
}