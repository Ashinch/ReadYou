package me.ash.reader.ui.ext

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.snap
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import kotlinx.coroutines.launch
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

@OptIn(
    ExperimentalFoundationApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)
@Composable
@SuppressLint("ComposableModifierFactory")
fun Modifier.combinedFeedbackClickable(
    isHaptic: Boolean? = false,
    isSound: Boolean? = false,
    onPressDown: (() -> Unit)? = null,
    onPressUp: (() -> Unit)? = null,
    onTap: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
): Modifier {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    return if (onPressDown != null || onPressUp != null || onTap != null) {
        indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onPressDown?.let {
                            it()
                            if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            onPressUp?.invoke()
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                    },
                    onTap = {
                        onTap?.let {
                            if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            if (isSound == true) view.playSoundEffect(SoundEffectConstants.CLICK)
                            it()
                        }
                    }
                )
            }
    } else {
        combinedClickable(
            onClick = {
                onClick?.let {
                    if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (isSound == true) view.playSoundEffect(SoundEffectConstants.CLICK)
                    it()
                }
            },
            onLongClick = {
                onLongClick?.let {
                    if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (isSound == true) view.playSoundEffect(SoundEffectConstants.CLICK)
                    it()
                }
            },
            onDoubleClick = {
                onDoubleClick?.let {
                    if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (isSound == true) view.playSoundEffect(SoundEffectConstants.CLICK)
                    it()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun Modifier.swipeableUpDown(onUp: () -> Unit, onDown: () -> Unit): Modifier = composed {
    var screenHeight by rememberSaveable { mutableStateOf(0f) }
    val swipeableState = rememberSwipeableState(
            SwipeDirection.Initial,
            animationSpec = snap()
    )
    val connection = remember {
        object: NestedScrollConnection {
            // Let the children eat first, we consume nothing
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset { return Offset.Zero }

            // Let it scroll...
            override suspend fun onPreFling(available: Velocity): Velocity { return Velocity.Zero }

            // We consume the rest
            override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
            ): Offset {
                // use leftover delta to swipe parent
                return Offset(0f, swipeableState.performDrag(available.y))
            }

            // We fling but with zero speed (needed to trigger the event, but too fast will overshoot)
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // perform fling on parent to trigger state change
                swipeableState.performFling(velocity = 0f)
                return available
            }
        }
    }
    val anchorHeight = remember(screenHeight) {
        if (screenHeight == 0f) {
            1f
        } else {
            screenHeight
        }
    }
    val scope = rememberCoroutineScope()
    if (swipeableState.isAnimationRunning) {
        DisposableEffect(Unit) {
            onDispose {
                when (swipeableState.currentValue) {
                    SwipeDirection.Up -> {
                        onUp()
                    }
                    SwipeDirection.Down -> {
                        onDown()
                    }
                    else -> {
                        return@onDispose
                    }
                }
                scope.launch {
                    swipeableState.snapTo(SwipeDirection.Initial)
                }
            }
        }
    }
    return@composed Modifier
            .onSizeChanged { screenHeight = it.height.toFloat() }
            .nestedScroll(connection)
            .swipeable(
                    state = swipeableState,
                    anchors = mapOf(
                            0f to SwipeDirection.Up,
                            anchorHeight / 2 to SwipeDirection.Initial,
                            anchorHeight to SwipeDirection.Down,
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Vertical,
            )
}
enum class SwipeDirection(val raw: Int) {
    Initial(0),
    Up(1),
    Down(2),
}
