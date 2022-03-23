package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import me.ash.reader.data.group.Group
import me.ash.reader.ui.widget.ViewPager

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SubscribeViewPager(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    inputLink: String = "",
    errorMessage: String = "",
    onLinkValueChange: (String) -> Unit = {},
    onSearchKeyboardAction: () -> Unit = {},
    link: String = "",
    groups: List<Group> = emptyList(),
    selectedAllowNotificationPreset: Boolean = false,
    selectedParseFullContentPreset: Boolean = false,
    selectedGroupId: String = "",
    newGroupContent: String = "",
    onNewGroupValueChange: (String) -> Unit = {},
    newGroupSelected: Boolean,
    changeNewGroupSelected: (Boolean) -> Unit = {},
    pagerState: PagerState = com.google.accompanist.pager.rememberPagerState(),
    allowNotificationPresetOnClick: () -> Unit = {},
    parseFullContentPresetOnClick: () -> Unit = {},
    groupOnClick: (groupId: String) -> Unit = {},
    onResultKeyboardAction: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    ViewPager(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    focusManager.clearFocus()
                }
            )
        },
        state = pagerState,
        userScrollEnabled = false,
        composableList = listOf(
            {
                SearchViewPage(
                    pagerState = pagerState,
                    readOnly = readOnly,
                    inputLink = inputLink,
                    errorMessage = errorMessage,
                    onLinkValueChange = onLinkValueChange,
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
                    newGroupContent = newGroupContent,
                    onNewGroupValueChange = onNewGroupValueChange,
                    newGroupSelected = newGroupSelected,
                    changeNewGroupSelected = changeNewGroupSelected,
                    allowNotificationPresetOnClick = allowNotificationPresetOnClick,
                    parseFullContentPresetOnClick = parseFullContentPresetOnClick,
                    groupOnClick = groupOnClick,
                    onKeyboardAction = onResultKeyboardAction,
                )
            }
        )
    )
}