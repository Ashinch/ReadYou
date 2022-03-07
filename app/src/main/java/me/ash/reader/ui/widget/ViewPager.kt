package me.ash.reader.ui.widget

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewPager(
    modifier: Modifier = Modifier,
    state: PagerState = com.google.accompanist.pager.rememberPagerState(),
    composableList: List<@Composable () -> Unit>,
    userScrollEnabled: Boolean = true,
) {
    HorizontalPager(
        count = composableList.size,
        state = state,
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .animateContentSize()
            .wrapContentHeight()
            .fillMaxWidth(),
        userScrollEnabled = userScrollEnabled,
    ) { page ->
        composableList[page]()
    }
}