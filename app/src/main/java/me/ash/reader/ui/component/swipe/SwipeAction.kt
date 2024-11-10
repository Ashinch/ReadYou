package me.ash.reader.ui.component.swipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * Represents an action that can be shown in [SwipeableActionsBox].
 *
 * @param background Color used as the background of [SwipeableActionsBox] while
 * this action is visible. If this action is swiped, its background color is
 * also used for drawing a ripple over the content for providing a visual
 * feedback to the user.
 *
 * @param weight The proportional width to give to this element, as related
 * to the total of all weighted siblings. [SwipeableActionsBox] will divide its
 * horizontal space and distribute it to actions according to their weight.
 *
 * @param isUndo Determines the direction in which a ripple is drawn when this
 * action is swiped. When false, the ripple grows from this action's position
 * to consume the entire composable, and vice versa. This can be used for
 * actions that can be toggled on and off.
 */
class SwipeAction(
  val onSwipe: () -> Unit,
  val icon: @Composable () -> Unit,
  val background: Color,
  val weight: Double = 1.0,
  val isUndo: Boolean = false
) {
  init {
    require(weight > 0.0) { "invalid weight $weight; must be greater than zero" }
  }

  fun copy(
    onSwipe: () -> Unit = this.onSwipe,
    icon: @Composable () -> Unit = this.icon,
    background: Color = this.background,
    weight: Double = this.weight,
    isUndo: Boolean = this.isUndo,
  ) = SwipeAction(
    onSwipe = onSwipe,
    icon = icon,
    background = background,
    weight = weight,
    isUndo = isUndo
  )
}

/**
 * See [SwipeAction] for documentation.
 */
fun SwipeAction(
  onSwipe: () -> Unit,
  icon: Painter,
  background: Color,
  weight: Double = 1.0,
  isUndo: Boolean = false
): SwipeAction {
  return SwipeAction(
    icon = {
      Image(
        modifier = Modifier.padding(16.dp),
        painter = icon,
        contentDescription = null
      )
    },
    background = background,
    weight = weight,
    onSwipe = onSwipe,
    isUndo = isUndo
  )
}
