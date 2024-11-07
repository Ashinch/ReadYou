package me.ash.reader.ui.component.swipe

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A composable that can be swiped left or right for revealing actions.
 *
 * @param swipeThreshold Minimum drag distance before any [SwipeAction] is
 * activated and can be swiped.
 *
 * @param backgroundUntilSwipeThreshold Color drawn behind the content until
 * [swipeThreshold] is reached. When the threshold is passed, this color is
 * replaced by the currently visible [SwipeAction]'s background.
 */
@Composable
fun SwipeableActionsBox(
  modifier: Modifier = Modifier,
  state: SwipeableActionsState = rememberSwipeableActionsState(),
  startActions: List<SwipeAction> = emptyList(),
  endActions: List<SwipeAction> = emptyList(),
  swipeThreshold: Dp = 40.dp,
  backgroundUntilSwipeThreshold: Color = Color.DarkGray,
  content: @Composable BoxScope.() -> Unit
) = Box(modifier) {
  state.also {
    it.swipeThresholdPx = LocalDensity.current.run { swipeThreshold.toPx() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    it.actions = remember(endActions, startActions, isRtl) {
      ActionFinder(
        left = if (isRtl) endActions else startActions,
        right = if (isRtl) startActions else endActions,
      )
    }
  }

  val backgroundColor = when {
    state.swipedAction != null -> state.swipedAction!!.value.background
    !state.hasCrossedSwipeThreshold() -> backgroundUntilSwipeThreshold
    state.visibleAction != null -> state.visibleAction!!.value.background
    else -> Color.Transparent
  }
  val animatedBackgroundColor: Color = if (state.layoutWidth == 0) {
    // Use the current color immediately because paparazzi can only capture the 1st frame.
    // https://github.com/cashapp/paparazzi/issues/1261
    backgroundColor
  } else {
    animateColorAsState(backgroundColor).value
  }

  val scope = rememberCoroutineScope()
  Box(
    modifier = Modifier
      .onSizeChanged { state.layoutWidth = it.width }
      .absoluteOffset { IntOffset(x = state.offset.value.roundToInt(), y = 0) }
      .drawOverContent { state.ripple.draw(scope = this) }
      .horizontalDraggable(
        enabled = !state.isResettingOnRelease,
        onDragStopped = {
          scope.launch {
            state.handleOnDragStopped()
          }
        },
        state = state.draggableState,
      ),
    content = content
  )

  (state.swipedAction ?: state.visibleAction)?.let { action ->
    ActionIconBox(
      modifier = Modifier.matchParentSize(),
      action = action,
      offset = state.offset.value,
      backgroundColor = animatedBackgroundColor,
      content = { action.value.icon() }
    )
  }

  val hapticFeedback = LocalHapticFeedback.current
  if (state.hasCrossedSwipeThreshold() && state.swipedAction == null) {
    LaunchedEffect(state.visibleAction) {
      hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
  }
}

@Composable
private fun ActionIconBox(
    action: SwipeActionMeta,
    offset: Float,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
  Row(
    modifier = modifier
      .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(width = placeable.width, height = placeable.height) {
          // Align icon with the left/right edge of the content being swiped.
          val iconOffset = if (action.isOnRightSide) constraints.maxWidth + offset else offset - placeable.width
          placeable.place(x = iconOffset.roundToInt(), y = 0)
        }
      }
      .background(color = backgroundColor),
    horizontalArrangement = if (action.isOnRightSide) Arrangement.Absolute.Left else Arrangement.Absolute.Right,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    content()
  }
}

private fun Modifier.drawOverContent(onDraw: DrawScope.() -> Unit): Modifier {
  return drawWithContent {
    drawContent()
    onDraw(this)
  }
}
