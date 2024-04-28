package me.ash.reader.ui.page.home.reading


import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.Drag
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.ash.reader.ui.page.home.reading.PullToLoadDefaults.ContentOffsetMultiple
import kotlin.math.abs
import kotlin.math.sqrt

private const val TAG = "PullToLoad"

/**
 * A [NestedScrollConnection] that provides scroll events to a hoisted [state].
 *
 * Note that this modifier must be added above a scrolling container using [Modifier.nestedScroll],
 * such as a lazy column, in order to receive scroll events.
 *
 * And you should manually handle the offset of components
 * with [PullToLoadState.progress] or [PullToLoadState.offsetFraction]
 *
 * @param enabled If not enabled, all scroll delta and fling velocity will be ignored.
 * @param onScroll Used for detecting if the reader is scrolling down
 */
private class ReaderNestedScrollConnection(
    private val enabled: Boolean,
    private val onPreScroll: (Float) -> Float,
    private val onPostScroll: (Float) -> Float,
    private val onRelease: () -> Unit,
    private val onScroll: ((Float) -> Unit)? = null
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset, source: NestedScrollSource
    ): Offset {
        onScroll?.invoke(available.y)
        return when {
            !enabled || available.y == 0f -> Offset.Zero

            // Scroll down to reduce the progress when the offset is currently pulled up, same for the opposite
            source == Drag -> {
                Offset(0f, onPreScroll(available.y))
            }

            else -> Offset.Zero
        }

    }

    override fun onPostScroll(
        consumed: Offset, available: Offset, source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == Drag -> Offset(0f, onPostScroll(available.y)) // Pull to load
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        onRelease()
        return Velocity.Zero
    }
}


/**
 * Creates a [PullToLoadState] that is remembered across compositions.
 *
 * Changes from [ReaderNestedScrollConnection] will result in this state being updated.
 *
 *
 * @param key Key used for remembering the state
 * @param onLoadNext The function to be called to load the next item when pulled up.
 * @param onLoadPrevious The function to be called to load the previous item when pulled down.
 * @param loadThreshold The threshold below which, if a release
 * occurs, [onLoadNext] or [onLoadPrevious] will be called.
 */
@Composable
@ExperimentalMaterialApi
fun rememberPullToLoadState(
    key: Any?,
    onLoadPrevious: () -> Unit,
    onLoadNext: () -> Unit,
    loadThreshold: Dp = PullToLoadDefaults.LoadThreshold,
): PullToLoadState {
    require(loadThreshold > 0.dp) { "The load trigger must be greater than zero!" }

    val scope = rememberCoroutineScope()
    val onNext = rememberUpdatedState(onLoadNext)
    val onPrevious = rememberUpdatedState(onLoadPrevious)
    val thresholdPx: Float

    with(LocalDensity.current) {
        thresholdPx = loadThreshold.toPx()
    }

    val state = remember(key, scope) {
        PullToLoadState(
            animationScope = scope,
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
 * A state object that can be used in conjunction with [ReaderNestedScrollConnection] to add pull-to-load
 * behaviour to a scroll component. Based on Android's SwipeRefreshLayout.
 *
 * Provides [progress], a float representing how far the user has pulled as a percentage of the
 * [threshold]. Values of one or less indicate that the user has not yet pulled past the
 * threshold. Values greater than one indicate how far past the threshold the user has pulled.
 *
 *
 * Should be created using [rememberPullToLoadState].
 */
class PullToLoadState internal constructor(
    private val animationScope: CoroutineScope,
    private val onLoadPrevious: State<() -> Unit>,
    private val onLoadNext: State<() -> Unit>,
    threshold: Float
) {
    /**
     * A float representing how far the user has pulled as a percentage of the [threshold].
     *
     * If the component has not been pulled at all, progress is zero. If the pull has reached
     * halfway to the threshold, progress is 0.5f. A value greater than 1 indicates that pull has
     * gone beyond the [threshold] - e.g. a value of 2f indicates that the user has pulled to
     * two times the [threshold].
     */
    val progress get() = abs(offsetPulled) / threshold

    /**
     * The offset fraction calculated from [progress] and [status],
     * This fraction grows in linear when the [progress] is no greater than 1,
     * then grows exponentially with the rate 1/2 if the [progress] greater than 1. - e.g. a value
     * of 2f indicates that the user has pulled to **four** times the [threshold].
     *
     * @return The offset fraction currently of this state, could be negative if the content is pulling up
     */
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
        return consumed
    }

    internal fun onPullBack(pullDelta: Float): Float {
        return if (offsetPulled.signOpposites(pullDelta)) onPull(pullDelta) else 0f
    }

    internal fun onRelease(): Float {
        // Snap to 0f and hide the indicator
        animateDistanceTo(0f)

        when (status) {
            Status.PulledDown -> {
                onLoadPrevious.value()
            }

            Status.PulledUp -> {
                animateDistanceTo(0f)
                onLoadNext.value()
            }

            else -> {

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
                    initialVelocity = velocity,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
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
object PullToLoadDefaults {
    /**
     * If the indicator is below this threshold offset when it is released, the load action
     * will be triggered.
     */
    val LoadThreshold = 120.dp

    const val ContentOffsetMultiple = 80
}

fun Modifier.pullToLoad(
    state: PullToLoadState,
    contentOffsetMultiple: Int = ContentOffsetMultiple,
    onScroll: ((Float) -> Unit)? = null,
    enabled: Boolean = true,
): Modifier =
    nestedScroll(
        ReaderNestedScrollConnection(
            enabled = enabled,
            onPreScroll = state::onPullBack,
            onPostScroll = state::onPull,
            onRelease = state::onRelease,
            onScroll = onScroll
        )
    ).run {
        if (enabled) offset(x = 0.dp, y = (state.offsetFraction * contentOffsetMultiple).dp)
        else this
    }
