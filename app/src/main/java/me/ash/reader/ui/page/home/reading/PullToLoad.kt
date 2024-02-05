package me.ash.reader.ui.page.home.reading


import android.util.Log
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefreshIndicatorTransform
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.Drag
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.sqrt

private const val TAG = "PullRelease"

/**
 * A nested scroll modifier that provides scroll events to [state].
 *
 * Note that this modifier must be added above a scrolling container, such as a lazy column, in
 * order to receive scroll events. For example:
 *
 * @sample androidx.compose.material.samples.PullRefreshSample
 *
 * @param state The [PullToLoadState] associated with this pull-to-refresh component.
 * The state will be updated by this modifier.
 * @param enabled If not enabled, all scroll delta and fling velocity will be ignored.
 */
// TODO(b/244423199): Move pullRefresh into its own material library similar to material-ripple.
@ExperimentalMaterialApi
fun Modifier.pullToLoad(
    state: PullToLoadState,
    enabled: Boolean = true,
    onScroll: (Float) -> Unit
) = inspectable(inspectorInfo = debugInspectorInfo {
    name = "pullRefresh"
    properties["state"] = state
    properties["enabled"] = enabled
}) {
    Modifier.nestedScroll(
        ReaderNestedScrollConnection(
            state = state,
            enabled = enabled,
            onScroll = onScroll
        )
    )
}

class ReaderNestedScrollConnection(
    private val state: PullToLoadState,
    private val enabled: Boolean,
    private val onScroll: (Float) -> Unit
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset, source: NestedScrollSource
    ): Offset {
        onScroll(available.y)
        return when {
            !enabled || available.y == 0f -> Offset.Zero

            source == Drag && state.offsetFraction.signOpposites(available.y) -> {
                Offset(0f, state.onPull(available.y))
            }

            else -> Offset.Zero
        }

    }

    override fun onPostScroll(
        consumed: Offset, available: Offset, source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == Drag -> Offset(0f, state.onPull(available.y)) // Pull to load
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return if (abs(state.progress) > 1f) {
            Velocity(0f, y = state.onRelease(available.y))
        } else {
            state.animateDistanceTo(0f)
            Velocity.Zero
        }
    }
}


/**
 * Creates a [PullToLoadState] that is remembered across compositions.
 *
 * Changes to [refreshing] will result in [PullToLoadState] being updated.
 *
 * @sample androidx.compose.material.samples.PullRefreshSample
 *
 * @param refreshing A boolean representing whether a refresh is currently occurring.
 * @param onLoadNext The function to be called to trigger a refresh.
 * @param loadThreshold The threshold below which, if a release
 * occurs, [onLoadNext] will be called.
 * @param refreshingOffset The offset at which the indicator will be drawn while refreshing. This
 * offset corresponds to the position of the bottom of the indicator.
 */
@Composable
@ExperimentalMaterialApi
fun rememberPullToLoadState(
    key: Any?,
    onLoadPrevious: () -> Unit,
    onLoadNext: () -> Unit,
    onThresholdReached: () -> Unit,
    loadThreshold: Dp = PullRefreshDefaults.RefreshThreshold,
): PullToLoadState {
    require(loadThreshold > 0.dp) { "The refresh trigger must be greater than zero!" }

    val scope = rememberCoroutineScope()
    val onNext = rememberUpdatedState(onLoadNext)
    val onPrevious = rememberUpdatedState(onLoadPrevious)
    val onLoad = rememberUpdatedState(onThresholdReached)
    val thresholdPx: Float

    with(LocalDensity.current) {
        thresholdPx = loadThreshold.toPx()
    }

    val state = remember(key, scope) {
        PullToLoadState(
            animationScope = scope,
            onThresholdReached = onLoad,
            onLoadPrevious = onPrevious,
            onLoadNext = onNext,
            threshold = thresholdPx
        )
    }

    SideEffect {
        state.setThreshold(thresholdPx)
    }

    return state
}

/**
 * A state object that can be used in conjunction with [pullToLoad] to add pull-to-refresh
 * behaviour to a scroll component. Based on Android's SwipeRefreshLayout.
 *
 * Provides [progress], a float representing how far the user has pulled as a percentage of the
 * refreshThreshold. Values of one or less indicate that the user has not yet pulled past the
 * threshold. Values greater than one indicate how far past the threshold the user has pulled.
 *
 * Can be used in conjunction with [pullRefreshIndicatorTransform] to implement Android-like
 * pull-to-refresh behaviour with a custom indicator.
 *
 * Should be created using [rememberPullToLoadState].
 */
class PullToLoadState internal constructor(
    private val animationScope: CoroutineScope,
    private val onThresholdReached: State<() -> Unit>,
    private val onLoadPrevious: State<() -> Unit>,
    private val onLoadNext: State<() -> Unit>,
    threshold: Float
) {
    /**
     * A float representing how far the user has pulled as a percentage of the refreshThreshold.
     *
     * If the component has not been pulled at all, progress is zero. If the pull has reached
     * halfway to the threshold, progress is 0.5f. A value greater than 1 indicates that pull has
     * gone beyond the refreshThreshold - e.g. a value of 2f indicates that the user has pulled to
     * two times the refreshThreshold.
     */
    val progress get() = abs(offsetPulled) / threshold

    val offsetFraction: Float get() = calculateOffsetFraction()

    sealed interface Status {
        data object PullingUp : Status

        data object PullingDown : Status

        data object PulledDown : Status

        data object PulledUp : Status

        data object Idle : Status
    }

    val status: Status
        get() = when {
            offsetPulled < threshold && offsetPulled > 0f -> Status.PullingDown
            offsetPulled > -threshold && offsetPulled < 0f -> Status.PullingUp
            offsetPulled >= threshold -> Status.PulledDown
            offsetPulled <= -threshold -> Status.PulledUp
            else -> Status.Idle
        }

    private val threshold get() = _threshold


    private var offsetPulled by mutableFloatStateOf(0f)
    private var _threshold by mutableFloatStateOf(threshold)

    internal fun onPull(pullDelta: Float): Float {
        val statusBefore = status
        val consumed = if (offsetPulled.signOpposites(offsetPulled + pullDelta)) {
            -offsetPulled
        } else {
            pullDelta
        }
        /*
                Log.d(
                    TAG,
                    "onPull: currentOffset = $offsetPulled, pullDelta = $pullDelta, consumed = $consumed"
                )*/


        offsetPulled += consumed
        val statusAfter = status
        if ((statusAfter is Status.PulledUp || statusAfter is Status.PulledDown) && statusBefore != statusAfter) {
            onThresholdReached.value()
        }
        return consumed
    }

    internal fun onRelease(velocity: Float): Float {
        Log.d(TAG, "onPull: $velocity")
//        val consumed = when {
//            // We are flinging without having dragged the pull refresh (for example a fling inside
//            // a list) - don't consume
//            distancePulled == 0f -> 0f
//            // If the velocity is negative, the fling is upwards, and we don't want to prevent the
//            // the list from scrolling
//            velocity < 0f -> 0f
//            // We are showing the indicator, and the fling is downwards - consume everything
//            else -> velocity
//        }
        when (status) {
            Status.PulledDown -> {
                onLoadPrevious.value()
            }

            Status.PulledUp -> {
                onLoadNext.value()
            }

            else -> {
                animateDistanceTo(0f)
            }
        }
        return 0f
    }

    // Make sure to cancel any existing animations when we launch a new one. We use this instead of
    // Animatable as calling snapTo() on every drag delta has a one frame delay, and some extra
    // overhead of running through the animation pipeline instead of directly mutating the state.
    private val mutatorMutex = MutatorMutex()
    internal fun animateDistanceTo(float: Float, velocity: Float = 0f) {
        animationScope.launch {
            mutatorMutex.mutate {
                animate(
                    initialValue = offsetPulled,
                    targetValue = float,
                    initialVelocity = velocity
                ) { value, _ ->
                    offsetPulled = value
                }
            }
        }
    }

    internal fun flingWithVelocity(initialVelocity: Float) {
        animationScope.launch {
            mutatorMutex.mutate {
                animateDecay(
                    initialValue = offsetPulled,
                    initialVelocity = initialVelocity,
                    animationSpec = FloatExponentialDecaySpec(
                        frictionMultiplier = 3f,
                        absVelocityThreshold = 10f
                    )
                ) { value, _ ->
                    if (abs(value) > threshold) {
                        cancel()
                    } else {
                        onPull(value - offsetPulled)
                    }
                }
            }
        }.invokeOnCompletion { animateDistanceTo(0f) }
    }

    internal fun setThreshold(threshold: Float) {
        _threshold = threshold
    }

    private fun calculateOffsetFraction(): Float = when (status) {
        Status.Idle -> 0f
        Status.PulledDown -> sqrt(progress)
        Status.PulledUp -> -sqrt(progress)
        Status.PullingDown -> progress
        Status.PullingUp -> -progress
    }

}

private fun Float.signOpposites(f: Float): Boolean =
    (this > 0f && f < 0f) || (this < 0f && f > 0f)

/**
 * Default parameter values for [rememberPullToLoadState].
 */
@ExperimentalMaterialApi
object PullRefreshDefaults {
    /**
     * If the indicator is below this threshold offset when it is released, a refresh
     * will be triggered.
     */
    val RefreshThreshold = 120.dp
}