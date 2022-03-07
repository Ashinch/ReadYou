package me.ash.reader.ui.extension

import androidx.compose.foundation.lazy.LazyListState
import kotlin.math.abs

fun LazyListState.calculateTopBarAnimateValue(start: Float, end: Float): Float =
    if (firstVisibleItemIndex != 0) end
    else {
        val variable = firstVisibleItemScrollOffset.coerceAtLeast(0).toFloat()
        val duration = 256f
        val increase = abs(start - end) * (variable / duration)
        if (start < end) (start + increase).coerceIn(start, end)
        else (start - increase).coerceIn(end, start)
    }
