package me.ash.reader.ui.widget

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlin.math.absoluteValue

@ExperimentalPagerApi
@Composable
fun BoxScope.MaskBox(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    currentPage: Int = 0,
) {
    val transition = updateTransition(targetState = pagerState, label = "")
    val maskAlpha by transition.animateFloat(
        label = "",
        transitionSpec = {
            spring()
        }
    ) {
        when {
            it.targetPage == currentPage -> {
                if (it.currentPage > currentPage) {
                    1f - it.currentPageOffset.absoluteValue
                } else {
                    0f
                }
            }
            it.targetPage > currentPage -> {
                it.currentPageOffset.absoluteValue
            }
            else -> 0f
        }
    }

    Box(
        modifier
            .alpha(maskAlpha)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}