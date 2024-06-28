package me.ash.reader.ui.ext

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerAnimate(pagerState: PagerState, page: Int): Modifier {
    return graphicsLayer {
        // Calculate the absolute offset for the current page from the
        // scroll position. We use the absolute value which allows us to mirror
        // any effects for both directions
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

        // We animate the alpha, between 20% and 100%
        alpha = lerp(
            start = 0.2f.dp,
            stop = 1f.dp,
            fraction = 1f - pageOffset.coerceIn(0f, 1f) * 1.5f
        ).value
    }
}

fun Modifier.roundClick(enabled: Boolean = true, onClick: () -> Unit = {}) = this
    .clip(RoundedCornerShape(8.dp))
    .clickable(enabled = enabled, onClick = onClick)

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
