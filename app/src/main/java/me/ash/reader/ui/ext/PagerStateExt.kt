package me.ash.reader.ui.ext

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
fun PagerState.animateScrollToPage(
    scope: CoroutineScope,
    targetPage: Int,
    callback: () -> Unit = {},
) {
    scope.launch {
        if (pageCount > targetPage) {
            animateScrollToPage(targetPage)
            callback()
        }
    }
}
