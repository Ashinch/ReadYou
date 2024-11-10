@file:Suppress("NAME_SHADOWING")

package me.ash.reader.ui.component.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Stable
internal class SwipeRippleState {
  private var ripple = mutableStateOf<SwipeRipple?>(null)

  suspend fun animate(
      action: SwipeActionMeta,
  ) {
    val drawOnRightSide = action.isOnRightSide
    val action = action.value

    ripple.value = SwipeRipple(
      isUndo = action.isUndo,
      rightSide = drawOnRightSide,
      color = action.background,
      alpha = 0f,
      progress = 0f
    )

    // Reverse animation feels faster (especially for larger swipe distances) so slow it down further.
    val animationDurationMs = (animationDurationMs * (if (action.isUndo) 1.75f else 1f)).roundToInt()

    coroutineScope {
      launch {
        Animatable(initialValue = 0f).animateTo(
          targetValue = 1f,
          animationSpec = tween(durationMillis = animationDurationMs),
          block = {
            ripple.value = ripple.value!!.copy(progress = value)
          }
        )
      }
      launch {
        Animatable(initialValue = if (action.isUndo) 0f else 0.25f).animateTo(
          targetValue = if (action.isUndo) 0.5f else 0f,
          animationSpec = tween(
            durationMillis = animationDurationMs,
            delayMillis = if (action.isUndo) 0 else animationDurationMs / 2
          ),
          block = {
            ripple.value = ripple.value!!.copy(alpha = value)
          }
        )
      }
    }
  }

  fun draw(scope: DrawScope) {
    ripple.value?.run {
      scope.clipRect {
        val size = scope.size
        // Start the ripple with a radius equal to the available height so that it covers the entire edge.
        val startRadius = if (isUndo) size.width + size.height else size.height
        val endRadius = if (!isUndo) size.width + size.height else size.height
        val radius = lerp(startRadius, endRadius, fraction = progress)

        drawCircle(
          color = color,
          radius = radius,
          alpha = alpha,
          center = this.center.copy(x = if (rightSide) this.size.width + this.size.height else 0f - this.size.height)
        )
      }
    }
  }
}

private data class SwipeRipple(
  val isUndo: Boolean,
  val rightSide: Boolean,
  val color: Color,
  val alpha: Float,
  val progress: Float,
)

private fun lerp(start: Float, stop: Float, fraction: Float) =
  (start * (1 - fraction) + stop * fraction)
