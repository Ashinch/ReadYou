package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.data.entity.Group
import me.ash.reader.ui.component.ViewPager

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SubscribeViewPager(
    viewState: SubscribeViewState,
    modifier: Modifier = Modifier,
    onLinkValueChange: (String) -> Unit = {},
    onSearchKeyboardAction: () -> Unit = {},
    groups: List<Group> = emptyList(),
    onNewGroupValueChange: (String) -> Unit = {},
    changeNewGroupSelected: (Boolean) -> Unit = {},
    allowNotificationPresetOnClick: () -> Unit = {},
    parseFullContentPresetOnClick: () -> Unit = {},
    onGroupClick: (groupId: String) -> Unit = {},
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
        state = viewState.pagerState,
        userScrollEnabled = false,
        composableList = listOf(
            {
                SearchViewPage(
                    pagerState = viewState.pagerState,
                    readOnly = viewState.lockLinkInput,
                    inputLink = viewState.linkContent,
                    errorMessage = viewState.errorMessage,
                    onLinkValueChange = onLinkValueChange,
                    onKeyboardAction = onSearchKeyboardAction,
                )
            },
            {
                ResultViewPage(
                    link = viewState.linkContent,
                    groups = groups,
                    selectedAllowNotificationPreset = viewState.allowNotificationPreset,
                    selectedParseFullContentPreset = viewState.parseFullContentPreset,
                    selectedGroupId = viewState.selectedGroupId,
                    newGroupContent = viewState.newGroupContent,
                    onNewGroupValueChange = onNewGroupValueChange,
                    newGroupSelected = viewState.newGroupSelected,
                    changeNewGroupSelected = changeNewGroupSelected,
                    allowNotificationPresetOnClick = allowNotificationPresetOnClick,
                    parseFullContentPresetOnClick = parseFullContentPresetOnClick,
                    onGroupClick = onGroupClick,
                    onKeyboardAction = onResultKeyboardAction,
                )
            }
        )
    )
}