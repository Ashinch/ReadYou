package me.ash.reader.ui.extension

import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
fun PagerState.animateScrollToPage(
    scope: CoroutineScope,
    targetPage: Int,
    callback: () -> Unit = {}
) {
    scope.launch {
        animateScrollToPage(targetPage)
        callback()
    }
}