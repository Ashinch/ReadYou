package me.ash.reader.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.abs
import kotlin.math.absoluteValue

@Composable
fun <T> StateFlow<T>.collectAsStateValue(
    context: CoroutineContext = EmptyCoroutineContext
): T = collectAsState(context).value

fun LazyListState.calculateTopBarAnimateValue(start: Float, end: Float): Float =
    if (firstVisibleItemIndex != 0) end
    else {
        val variable = firstVisibleItemScrollOffset.coerceAtLeast(0).toFloat()
        val duration = 256f
        val increase = abs(start - end) * (variable / duration)
        if (start < end) (start + increase).coerceIn(start, end)
        else (start - increase).coerceIn(end, start)
    }

@ExperimentalPagerApi
fun Modifier.pagerAnimate(pagerScope: PagerScope, page: Int): Modifier {
    return graphicsLayer {
        // Calculate the absolute offset for the current page from the
        // scroll position. We use the absolute value which allows us to mirror
        // any effects for both directions
        val pageOffset = pagerScope.calculateCurrentOffsetForPage(page).absoluteValue

        // We animate the scaleX + scaleY, between 85% and 100%
//                        lerp(
//                            start = 0.85f.dp,
//                            stop = 1f.dp,
//                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
//                        ).also { scale ->
//                            scaleX = scale.value
//                            scaleY = scale.value
//                        }

        // We animate the alpha, between 50% and 100%
        alpha = lerp(
            start = 0.2f.dp,
            stop = 1f.dp,
            fraction = 1f - pageOffset.coerceIn(0f, 1f) * 1.5f
        ).value
    }
}

fun Modifier.roundClick(onClick: () -> Unit = {}) = this
    .clip(RoundedCornerShape(8.dp))
    .clickable(onClick = onClick)

fun Modifier.paddingFixedHorizontal(top: Dp = 0.dp, bottom: Dp = 0.dp) = this
    .padding(horizontal = 10.dp)
    .padding(top = top, bottom = bottom)