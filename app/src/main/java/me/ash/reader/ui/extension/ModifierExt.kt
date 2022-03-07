package me.ash.reader.ui.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import kotlin.math.absoluteValue

@OptIn(ExperimentalPagerApi::class)
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
