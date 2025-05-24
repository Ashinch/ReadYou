package me.ash.reader.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

enum class Direction {
    Downward, Upward
}

fun SharedYAxisTransition(direction: Direction): ContentTransform {
    val direction = when (direction) {
        Direction.Downward -> -1
        Direction.Upward -> 1
    }
    return materialSharedAxisY(
        initialOffsetY = { it / 6 * direction },
        targetOffsetY = { -it / 6 * direction },
        durationMillis = 500
    )
}

fun SharedYAxisTransitionSlow(direction: Direction): ContentTransform {
    val direction = when (direction) {
        Direction.Downward -> -1
        Direction.Upward -> 1
    }
    val exit = 150
    val enter = exit * 2
    return (slideInVertically(
        initialOffsetY = { (it * 0.2f * direction).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    ) + fadeIn(
        tween(
            delayMillis = exit,
            durationMillis = enter,
            easing = LinearOutSlowInEasing
        )
    )) togetherWith (slideOutVertically(
        targetOffsetY = { (it * -0.1f * direction).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    ) + fadeOut(
        tween(durationMillis = exit, easing = FastOutLinearInEasing)
    ))
}


fun SharedYAxisTransitionFast(direction: Direction): ContentTransform {
    val direction = when (direction) {
        Direction.Downward -> -1
        Direction.Upward -> 1
    }
    val exit = 50
    val enter = exit * 2
    return (slideInVertically(
        initialOffsetY = { (it * 0.2f * direction).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    ) + fadeIn(
        tween(
            delayMillis = exit,
            durationMillis = enter,
            easing = LinearOutSlowInEasing
        )
    )) togetherWith (slideOutVertically(
        targetOffsetY = { (it * -0.1f * direction).toInt() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    ) + fadeOut(
        tween(durationMillis = exit, easing = FastOutLinearInEasing)
    ))
}