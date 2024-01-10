package co.softov.morestuff.android.ui.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.SwipeToDismissDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.OnRemeasuredModifier
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The sole purpose for this file is to change swipe-to-dismiss velocity. Google... really?
 */
private val DismissThreshold = Dp.Infinity //125.dp

/**
 * A composable that can be dismissed by swiping left or right.
 *
 * @sample androidx.compose.material3.samples.SwipeToDismissListItems
 *
 * @param state The state of this component.
 * @param background A composable that is stacked behind the content and is exposed when the
 * content is swiped. You can/should use the [state] to have different backgrounds on each side.
 * @param dismissContent The content that can be dismissed.
 * @param modifier Optional [Modifier] for this component.
 * @param directions The set of directions in which the component can be dismissed.
 */
@Composable
@ExperimentalMaterial3Api
fun NoFlingSwipeToDismiss(
    state: NoFlingDismissState,
    background: @Composable RowScope.() -> Unit,
    dismissContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    directions: Set<DismissDirection> = setOf(
        DismissDirection.EndToStart,
        DismissDirection.StartToEnd
    ),
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(
        modifier
            .swipeableV2(
                state = state.swipeableState,
                orientation = Orientation.Horizontal,
                enabled = state.currentValue == DismissValue.Default,
                reverseDirection = isRtl,
            )
            .swipeAnchors(
                state = state.swipeableState,
                possibleValues = setOf(DismissValue.Default, DismissValue.DismissedToEnd, DismissValue.DismissedToStart)
            ) { value, layoutSize ->
                val width = layoutSize.width.toFloat()
                when (value) {
                    DismissValue.DismissedToEnd -> if (DismissDirection.StartToEnd in directions) width else null
                    DismissValue.DismissedToStart -> if (DismissDirection.EndToStart in directions) -width else null
                    DismissValue.Default -> 0f
                }
            }
    ) {
        Row(
            content = background,
            modifier = Modifier.matchParentSize()
        )
        Row(
            content = dismissContent,
            modifier = Modifier.offset { IntOffset(state.requireOffset().roundToInt(), 0) }
        )
    }
}

/**
 * Create and [remember] a [DismissState].
 *
 * @param initialValue The initial value of the state.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 * @param positionalThreshold The positional threshold to be used when calculating the target state
 * while a swipe is in progress and when settling after the swipe ends. This is the distance from
 * the start of a transition. It will be, depending on the direction of the interaction, added or
 * subtracted from/to the origin offset. It should always be a positive value.
 */
@Composable
@ExperimentalMaterial3Api
fun rememberNoFlingDismissState(
    initialValue: DismissValue = DismissValue.Default,
    confirmValueChange: (DismissValue) -> Boolean = { true },
    positionalThreshold: Density.(totalDistance: Float) -> Float =
        SwipeToDismissDefaults.FixedPositionalThreshold,
): NoFlingDismissState {
    return rememberSaveable(
        saver = NoFlingDismissState.Saver(confirmValueChange, positionalThreshold)) {
        NoFlingDismissState(initialValue, confirmValueChange, positionalThreshold)
    }
}

/**
 * Enable swipe gestures between a set of predefined values.
 *
 * When a swipe is detected, the offset of the [SwipeableV2State] will be updated with the swipe
 * delta. You should use this offset to move your content accordingly (see [Modifier.offset]).
 * When the swipe ends, the offset will be animated to one of the anchors and when that anchor is
 * reached, the value of the [SwipeableV2State] will also be updated to the value corresponding to
 * the new anchor.
 *
 * Swiping is constrained between the minimum and maximum anchors.
 *
 * @param state The associated [SwipeableV2State].
 * @param orientation The orientation in which the swipeable can be swiped.
 * @param enabled Whether this [swipeableV2] is enabled and should react to the user's input.
 * @param reverseDirection Whether to reverse the direction of the swipe, so a top to bottom
 * swipe will behave like bottom to top, and a left to right swipe will behave like right to left.
 * @param interactionSource Optional [MutableInteractionSource] that will passed on to
 * the internal [Modifier.draggable].
 */
@ExperimentalMaterial3Api
internal fun <T> Modifier.swipeableV2(
    state: NoFlingSwipeableV2State<T>,
    orientation: Orientation,
    enabled: Boolean = true,
    reverseDirection: Boolean = false,
    interactionSource: MutableInteractionSource? = null
) = draggable(
    state = state.swipeDraggableState,
    orientation = orientation,
    enabled = enabled,
    interactionSource = interactionSource,
    reverseDirection = reverseDirection,
    startDragImmediately = state.isAnimationRunning,
    onDragStopped = { velocity -> launch { state.settle(velocity) } }
)

/**
 * Define anchor points for a given [SwipeableV2State] based on this node's layout size and update
 * the state with them.
 *
 * @param state The associated [SwipeableV2State]
 * @param possibleValues All possible values the [SwipeableV2State] could be in.
 * @param anchorChangeHandler A callback to be invoked when the anchors have changed,
 * `null` by default. Components with custom reconciliation logic should implement this callback,
 * i.e. to re-target an in-progress animation.
 * @param calculateAnchor This method will be invoked to calculate the position of all
 * [possibleValues], given this node's layout size. Return the anchor's offset from the initial
 * anchor, or `null` to indicate that a value does not have an anchor.
 */
@ExperimentalMaterial3Api
internal fun <T> Modifier.swipeAnchors(
    state: NoFlingSwipeableV2State<T>,
    possibleValues: Set<T>,
    anchorChangeHandler: AnchorChangeHandler<T>? = null,
    calculateAnchor: (value: T, layoutSize: IntSize) -> Float?,
) = this.then(SwipeAnchorsModifier(
    onDensityChanged = { state.density = it },
    onSizeChanged = { layoutSize ->
        val previousAnchors = state.anchors
        val newAnchors = mutableMapOf<T, Float>()
        possibleValues.forEach {
            val anchorValue = calculateAnchor(it, layoutSize)
            if (anchorValue != null) {
                newAnchors[it] = anchorValue
            }
        }
        if (previousAnchors != newAnchors) {
            val previousTarget = state.targetValue
            val stateRequiresCleanup = state.updateAnchors(newAnchors)
            if (stateRequiresCleanup) {
                anchorChangeHandler?.onAnchorsChanged(
                    previousTarget,
                    previousAnchors,
                    newAnchors
                )
            }
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "swipeAnchors"
        properties["state"] = state
        properties["possibleValues"] = possibleValues
        properties["anchorChangeHandler"] = anchorChangeHandler
        properties["calculateAnchor"] = calculateAnchor
    }
))


/**
 * State of the [SwipeToDismiss] composable.
 *
 * @param initialValue The initial value of the state.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 * @param positionalThreshold The positional threshold to be used when calculating the target state
 * while a swipe is in progress and when settling after the swipe ends. This is the distance from
 * the start of a transition. It will be, depending on the direction of the interaction, added or
 * subtracted from/to the origin offset. It should always be a positive value.
 */
@ExperimentalMaterial3Api
class NoFlingDismissState(
    initialValue: DismissValue,
    confirmValueChange: (DismissValue) -> Boolean = { true },
    positionalThreshold: Density.(totalDistance: Float) -> Float =
        SwipeToDismissDefaults.FixedPositionalThreshold,
) {
    internal val swipeableState = NoFlingSwipeableV2State(
        initialValue = initialValue,
        confirmValueChange = confirmValueChange,
        positionalThreshold = positionalThreshold,
        velocityThreshold = DismissThreshold
    )

    internal val offset: Float? get() = swipeableState.offset

    /**
     * Require the current offset.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float = swipeableState.requireOffset()

    /**
     * The current state value of the [DismissState].
     */
    val currentValue: DismissValue get() = swipeableState.currentValue

    /**
     * The target state. This is the closest state to the current offset (taking into account
     * positional thresholds). If no interactions like animations or drags are in progress, this
     * will be the current state.
     */
    val targetValue: DismissValue get() = swipeableState.targetValue

    /**
     * The fraction of the progress going from currentValue to targetValue, within [0f..1f] bounds.
     */
    val progress: Float get() = swipeableState.progress

    /**
     * The direction (if any) in which the composable has been or is being dismissed.
     *
     * If the composable is settled at the default state, then this will be null. Use this to
     * change the background of the [SwipeToDismiss] if you want different actions on each side.
     */
    val dismissDirection: DismissDirection? get() =
        if (offset == 0f || offset == null) null else if (offset!! > 0f) DismissDirection.StartToEnd else DismissDirection.EndToStart

    /**
     * Whether the component has been dismissed in the given [direction].
     *
     * @param direction The dismiss direction.
     */
    fun isDismissed(direction: DismissDirection): Boolean {
        return currentValue == if (direction == DismissDirection.StartToEnd) DismissValue.DismissedToEnd else DismissValue.DismissedToStart
    }

    /**
     * Set the state without any animation and suspend until it's set
     *
     * @param targetValue The new target value
     */
    suspend fun snapTo(targetValue: DismissValue) {
        swipeableState.snapTo(targetValue)
    }

    /**
     * Reset the component to the default position with animation and suspend until it if fully
     * reset or animation has been cancelled. This method will throw [CancellationException] if
     * the animation is interrupted
     *
     * @return the reason the reset animation ended
     */
    suspend fun reset() = swipeableState.animateTo(targetValue = DismissValue.Default)

    /**
     * Dismiss the component in the given [direction], with an animation and suspend. This method
     * will throw [CancellationException] if the animation is interrupted
     *
     * @param direction The dismiss direction.
     */
    suspend fun dismiss(direction: DismissDirection) {
        val targetValue = if (direction == DismissDirection.StartToEnd) DismissValue.DismissedToEnd else DismissValue.DismissedToStart
        swipeableState.animateTo(targetValue = targetValue)
    }

    companion object {
        /**
         * The default [Saver] implementation for [DismissState].
         */
        fun Saver(
            confirmValueChange: (DismissValue) -> Boolean,
            positionalThreshold: Density.(totalDistance: Float) -> Float,
        ) =
            Saver<NoFlingDismissState, DismissValue>(
                save = { it.currentValue },
                restore = {
                    NoFlingDismissState(
                        it, confirmValueChange, positionalThreshold)
                }
            )
    }
}


/**
 * State of the [swipeableV2] modifier.
 *
 * This contains necessary information about any ongoing swipe or animation and provides methods
 * to change the state either immediately or by starting an animation. To create and remember a
 * [SwipeableV2State] use [rememberSwipeableV2State].
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 * @param positionalThreshold The positional threshold to be used when calculating the target state
 * while a swipe is in progress and when settling after the swipe ends. This is the distance from
 * the start of a transition. It will be, depending on the direction of the interaction, added or
 * subtracted from/to the origin offset. It should always be a positive value. See the
 * [fractionalPositionalThreshold] and [fixedPositionalThreshold] methods.
 * @param velocityThreshold The velocity threshold (in dp per second) that the end velocity has to
 * exceed in order to animate to the next state, even if the [positionalThreshold] has not been
 * reached.
 */
@Stable
@ExperimentalMaterial3Api
internal class NoFlingSwipeableV2State<T>(
    initialValue: T,
    internal val animationSpec: AnimationSpec<Float> = NoFlingSwipeableV2Defaults.AnimationSpec,
    internal val confirmValueChange: (newValue: T) -> Boolean = { true },
    internal val positionalThreshold: Density.(totalDistance: Float) -> Float =
        NoFlingSwipeableV2Defaults.PositionalThreshold,
    internal val velocityThreshold: Dp = NoFlingSwipeableV2Defaults.VelocityThreshold,
) {

    private val swipeMutex = InternalMutatorMutex()

    internal val swipeDraggableState = object : DraggableState {
        private val dragScope = object : DragScope {
            override fun dragBy(pixels: Float) {
                this@NoFlingSwipeableV2State.dispatchRawDelta(pixels)
            }
        }

        override suspend fun drag(
            dragPriority: MutatePriority,
            block: suspend DragScope.() -> Unit
        ) {
            swipe(dragPriority) { dragScope.block() }
        }

        override fun dispatchRawDelta(delta: Float) {
            this@NoFlingSwipeableV2State.dispatchRawDelta(delta)
        }
    }

    /**
     * The current value of the [SwipeableV2State].
     */
    var currentValue: T by mutableStateOf(initialValue)
        private set

    /**
     * The target value. This is the closest value to the current offset (taking into account
     * positional thresholds). If no interactions like animations or drags are in progress, this
     * will be the current value.
     */
    val targetValue: T by derivedStateOf {
        animationTarget ?: run {
            val currentOffset = offset
            if (currentOffset != null) {
                computeTarget(currentOffset, currentValue, velocity = 0f)
            } else currentValue
        }
    }

    /**
     * The current offset, or null if it has not been initialized yet.
     *
     * The offset will be initialized during the first measurement phase of the node that the
     * [swipeableV2] modifier is attached to. These are the phases:
     * Composition { -> Effects } -> Layout { Measurement -> Placement } -> Drawing
     * During the first composition, the offset will be null. In subsequent compositions, the offset
     * will be derived from the anchors of the previous pass.
     * Always prefer accessing the offset from a LaunchedEffect as it will be scheduled to be
     * executed the next frame, after layout.
     *
     * To guarantee stricter semantics, consider using [requireOffset].
     */
    @get:Suppress("AutoBoxing")
    var offset: Float? by mutableStateOf(null)
        private set

    /**
     * Require the current offset.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float = checkNotNull(offset) {
        "The offset was read before being initialized. Did you access the offset in a phase " +
                "before layout, like effects or composition?"
    }

    /**
     * Whether an animation is currently in progress.
     */
    val isAnimationRunning: Boolean get() = animationTarget != null

    /**
     * The fraction of the progress going from [currentValue] to [targetValue], within [0f..1f]
     * bounds.
     */
    /*@FloatRange(from = 0f, to = 1f)*/
    val progress: Float by derivedStateOf {
        val a = anchors[currentValue] ?: 0f
        val b = anchors[targetValue] ?: 0f
        val distance = abs(b - a)
        if (distance > 1e-6f) {
            val progress = (this.requireOffset() - a) / (b - a)
            // If we are very close to 0f or 1f, we round to the closest
            if (progress < 1e-6f) 0f else if (progress > 1 - 1e-6f) 1f else progress
        } else 1f
    }

    /**
     * The velocity of the last known animation. Gets reset to 0f when an animation completes
     * successfully, but does not get reset when an animation gets interrupted.
     * You can use this value to provide smooth reconciliation behavior when re-targeting an
     * animation.
     */
    var lastVelocity: Float by mutableStateOf(0f)
        private set

    /**
     * The minimum offset this state can reach. This will be the smallest anchor, or
     * [Float.NEGATIVE_INFINITY] if the anchors are not initialized yet.
     */
    val minOffset by derivedStateOf { anchors.minOrNull() ?: Float.NEGATIVE_INFINITY }

    /**
     * The maximum offset this state can reach. This will be the biggest anchor, or
     * [Float.POSITIVE_INFINITY] if the anchors are not initialized yet.
     */
    val maxOffset by derivedStateOf { anchors.maxOrNull() ?: Float.POSITIVE_INFINITY }

    private var animationTarget: T? by mutableStateOf(null)

    internal var anchors by mutableStateOf(emptyMap<T, Float>())

    internal var density: Density? = null

    /**
     * Update the anchors.
     * If the previous set of anchors was empty, attempt to update the offset to match the initial
     * value's anchor.
     *
     * @return true if the state needs to be adjusted after updating the anchors, e.g. if the
     * initial value is not found in the initial set of anchors. false if no further updates are
     * needed.
     */
    internal fun updateAnchors(newAnchors: Map<T, Float>): Boolean {
        val previousAnchorsEmpty = anchors.isEmpty()
        anchors = newAnchors
        val initialValueHasAnchor = if (previousAnchorsEmpty) {
            val initialValue = currentValue
            val initialValueAnchor = anchors[initialValue]
            val initialValueHasAnchor = initialValueAnchor != null
            if (initialValueHasAnchor) trySnapTo(initialValue)
            initialValueHasAnchor
        } else true
        return !initialValueHasAnchor || !previousAnchorsEmpty
    }

    /**
     * Whether the [value] has an anchor associated with it.
     */
    fun hasAnchorForValue(value: T): Boolean = anchors.containsKey(value)

    /**
     * Snap to a [targetValue] without any animation.
     * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to the
     * [targetValue] without updating the offset.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    suspend fun snapTo(targetValue: T) {
        swipe { snap(targetValue) }
    }

    /**
     * Animate to a [targetValue].
     * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to the
     * [targetValue] without updating the offset.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     * @param velocity The velocity the animation should start with, [lastVelocity] by default
     */
    suspend fun animateTo(
        targetValue: T,
        velocity: Float = lastVelocity,
    ) {
        val targetOffset = anchors[targetValue]
        if (targetOffset != null) {
            try {
                swipe {
                    animationTarget = targetValue
                    var prev = offset ?: 0f
                    animate(prev, targetOffset, velocity, animationSpec) { value, velocity ->
                        // Our onDrag coerces the value within the bounds, but an animation may
                        // overshoot, for example a spring animation or an overshooting interpolator
                        // We respect the user's intention and allow the overshoot, but still use
                        // DraggableState's drag for its mutex.
                        offset = value
                        prev = value
                        lastVelocity = velocity
                    }
                    lastVelocity = 0f
                }
            } finally {
                animationTarget = null
                val endOffset = requireOffset()
                val endState = anchors
                    .entries
                    .firstOrNull { (_, anchorOffset) -> abs(anchorOffset - endOffset) < 0.5f }
                    ?.key
                this.currentValue = endState ?: currentValue
            }
        } else {
            currentValue = targetValue
        }
    }

    /**
     * Find the closest anchor taking into account the velocity and settle at it with an animation.
     */
    suspend fun settle(velocity: Float) {
        val previousValue = this.currentValue
        val targetValue = computeTarget(
            offset = requireOffset(),
            currentValue = previousValue,
            velocity = velocity
        )
        if (confirmValueChange(targetValue)) {
            animateTo(targetValue, velocity)
        } else {
            // If the user vetoed the state change, rollback to the previous state.
            animateTo(previousValue, velocity)
        }
    }

    /**
     * Swipe by the [delta], coerce it in the bounds and dispatch it to the [SwipeableV2State].
     *
     * @return The delta the consumed by the [SwipeableV2State]
     */
    fun dispatchRawDelta(delta: Float): Float {
        val currentDragPosition = offset ?: 0f
        val potentiallyConsumed = currentDragPosition + delta
        val clamped = potentiallyConsumed.coerceIn(minOffset, maxOffset)
        val deltaToConsume = clamped - currentDragPosition
        if (abs(deltaToConsume) >= 0) {
            offset = ((offset ?: 0f) + deltaToConsume).coerceIn(minOffset, maxOffset)
        }
        return deltaToConsume
    }

    private fun computeTarget(
        offset: Float,
        currentValue: T,
        velocity: Float
    ): T {
        val currentAnchors = anchors
        val currentAnchor = currentAnchors[currentValue]
        val currentDensity = requireDensity()
        val velocityThresholdPx = with(currentDensity) { velocityThreshold.toPx() }
        return if (currentAnchor == offset || currentAnchor == null) {
            currentValue
        } else if (currentAnchor < offset) {
            // Swiping from lower to upper (positive).
            if (velocity >= velocityThresholdPx) {
                currentAnchors.closestAnchor(offset, true)
            } else {
                val upper = currentAnchors.closestAnchor(offset, true)
                val distance = abs(currentAnchors.getValue(upper) - currentAnchor)
                val relativeThreshold = abs(positionalThreshold(currentDensity, distance))
                val absoluteThreshold = abs(currentAnchor + relativeThreshold)
                if (offset < absoluteThreshold) currentValue else upper
            }
        } else {
            // Swiping from upper to lower (negative).
            if (velocity <= -velocityThresholdPx) {
                currentAnchors.closestAnchor(offset, false)
            } else {
                val lower = currentAnchors.closestAnchor(offset, false)
                val distance = abs(currentAnchor - currentAnchors.getValue(lower))
                val relativeThreshold = abs(positionalThreshold(currentDensity, distance))
                val absoluteThreshold = abs(currentAnchor - relativeThreshold)
                if (offset < 0) {
                    // For negative offsets, larger absolute thresholds are closer to lower anchors
                    // than smaller ones.
                    if (abs(offset) < absoluteThreshold) currentValue else lower
                } else {
                    if (offset > absoluteThreshold) currentValue else lower
                }
            }
        }
    }

    private fun requireDensity() = requireNotNull(density) {
        "SwipeableState did not have a density attached. Are you using Modifier.swipeable with " +
                "this=$this SwipeableState?"
    }

    private suspend fun swipe(
        swipePriority: MutatePriority = MutatePriority.Default,
        action: suspend () -> Unit
    ): Unit = coroutineScope { swipeMutex.mutate(swipePriority, action) }

    /**
     * Attempt to snap synchronously. Snapping can happen synchronously when there is no other swipe
     * transaction like a drag or an animation is progress. If there is another interaction in
     * progress, the suspending [snapTo] overload needs to be used.
     *
     * @return true if the synchronous snap was successful, or false if we couldn't snap synchronous
     */
    internal fun trySnapTo(targetValue: T): Boolean = swipeMutex.tryMutate { snap(targetValue) }

    private fun snap(targetValue: T) {
        val targetOffset = anchors[targetValue]
        if (targetOffset != null) {
            dispatchRawDelta(targetOffset - (offset ?: 0f))
            currentValue = targetValue
            animationTarget = null
        } else {
            currentValue = targetValue
        }
    }

    companion object {
        /**
         * The default [Saver] implementation for [SwipeableV2State].
         */
        @ExperimentalMaterial3Api
        fun <T : Any> Saver(
            animationSpec: AnimationSpec<Float>,
            confirmValueChange: (T) -> Boolean,
            positionalThreshold: Density.(distance: Float) -> Float,
            velocityThreshold: Dp
        ) = Saver<NoFlingSwipeableV2State<T>, T>(
            save = { it.currentValue },
            restore = {
                NoFlingSwipeableV2State(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmValueChange = confirmValueChange,
                    positionalThreshold = positionalThreshold,
                    velocityThreshold = velocityThreshold
                )
            }
        )
    }
}

/**
 * Expresses a fixed positional threshold of [threshold] dp. This will be the distance from an
 * anchor that needs to be reached for [SwipeableV2State] to settle to the next closest anchor.
 *
 * @see [fractionalPositionalThreshold] for a fractional positional threshold
 */
@ExperimentalMaterial3Api
internal fun fixedPositionalThreshold(threshold: Dp): Density.(distance: Float) -> Float = {
    threshold.toPx()
}


/**
 * Contains useful defaults for [swipeableV2] and [SwipeableV2State].
 */
@Stable
@ExperimentalMaterial3Api
internal object NoFlingSwipeableV2Defaults {
    /**
     * The default animation used by [SwipeableV2State].
     */
    @ExperimentalMaterial3Api
    val AnimationSpec = SpringSpec<Float>()

    /**
     * The default velocity threshold (1.8 dp per millisecond) used by [rememberSwipeableV2State].
     */
    @ExperimentalMaterial3Api
    val VelocityThreshold: Dp = 125.dp

    /**
     * The default positional threshold (56 dp) used by [rememberSwipeableV2State]
     */
    @ExperimentalMaterial3Api
    val PositionalThreshold: Density.(totalDistance: Float) -> Float =
        fixedPositionalThreshold(56.dp)

    /**
     * A [AnchorChangeHandler] implementation that attempts to reconcile an in-progress animation
     * by re-targeting it if necessary or finding the closest new anchor.
     * If the previous anchor is not in the new set of anchors, this implementation will snap to the
     * closest anchor.
     *
     * Consider implementing a custom handler for more complex components like sheets.
     * The [animate] and [snap] lambdas hoist the animation and snap logic. Usually these will just
     * delegate to [SwipeableV2State].
     *
     * @param state The [SwipeableV2State] the change handler will read from
     * @param animate A lambda that gets invoked to start an animation to a new target
     * @param snap A lambda that gets invoked to snap to a new target
     */
    @ExperimentalMaterial3Api
    internal fun <T> ReconcileAnimationOnAnchorChangeHandler(
        state: NoFlingSwipeableV2State<T>,
        animate: (target: T, velocity: Float) -> Unit,
        snap: (target: T) -> Unit
    ) = AnchorChangeHandler { previousTarget, previousAnchors, newAnchors ->
        val previousTargetOffset = previousAnchors[previousTarget]
        val newTargetOffset = newAnchors[previousTarget]
        if (previousTargetOffset != newTargetOffset) {
            if (newTargetOffset != null) {
                animate(previousTarget, state.lastVelocity)
            } else {
                snap(newAnchors.closestAnchor(offset = state.requireOffset()))
            }
        }
    }
}

/**
 * Defines a callback that is invoked when the anchors have changed.
 *
 * Components with custom reconciliation logic should implement this callback, for example to
 * re-target an in-progress animation when the anchors change.
 *
 * @see SwipeableV2Defaults.ReconcileAnimationOnAnchorChangeHandler for a default implementation
 */
@ExperimentalMaterial3Api
internal fun interface AnchorChangeHandler<T> {

    /**
     * Callback that is invoked when the anchors have changed, after the [SwipeableV2State] has been
     * updated with them. Use this hook to re-launch animations or interrupt them if needed.
     *
     * @param previousTargetValue The target value before the anchors were updated
     * @param previousAnchors The previously set anchors
     * @param newAnchors The newly set anchors
     */
    fun onAnchorsChanged(
        previousTargetValue: T,
        previousAnchors: Map<T, Float>,
        newAnchors: Map<T, Float>
    )
}

@Stable
private class SwipeAnchorsModifier(
    private val onDensityChanged: (density: Density) -> Unit,
    private val onSizeChanged: (layoutSize: IntSize) -> Unit,
    inspectorInfo: InspectorInfo.() -> Unit,
) : LayoutModifier, OnRemeasuredModifier, InspectorValueInfo(inspectorInfo) {

    private var lastDensity: Float = -1f
    private var lastFontScale: Float = -1f

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        if (density != lastDensity || fontScale != lastFontScale) {
            onDensityChanged(Density(density, fontScale))
            lastDensity = density
            lastFontScale = fontScale
        }
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

    override fun onRemeasured(size: IntSize) {
        onSizeChanged(size)
    }

    override fun toString() = "SwipeAnchorsModifierImpl(updateDensity=$onDensityChanged, " +
            "onSizeChanged=$onSizeChanged)"
}

private fun <T> Map<T, Float>.closestAnchor(
    offset: Float = 0f,
    searchUpwards: Boolean = false
): T {
    require(isNotEmpty()) { "The anchors were empty when trying to find the closest anchor" }
    return minBy { (_, anchor) ->
        val delta = if (searchUpwards) anchor - offset else offset - anchor
        if (delta < 0) Float.POSITIVE_INFINITY else delta
    }.key
}

private fun <T> Map<T, Float>.minOrNull() = minOfOrNull { (_, offset) -> offset }
private fun <T> Map<T, Float>.maxOrNull() = maxOfOrNull { (_, offset) -> offset }


/**
 * Mutual exclusion for UI state mutation over time.
 *
 * [mutate] permits interruptible state mutation over time using a standard [MutatePriority].
 * A [InternalMutatorMutex] enforces that only a single writer can be active at a time for a particular
 * state resource. Instead of queueing callers that would acquire the lock like a traditional
 * [Mutex], new attempts to [mutate] the guarded state will either cancel the current mutator or
 * if the current mutator has a higher priority, the new caller will throw [CancellationException].
 *
 * [InternalMutatorMutex] should be used for implementing hoisted state objects that many mutators may
 * want to manipulate over time such that those mutators can coordinate with one another. The
 * [InternalMutatorMutex] instance should be hidden as an implementation detail. For example:
 *
 */
@Stable
internal class InternalMutatorMutex {
    private class Mutator(val priority: MutatePriority, val job: Job) {
        fun canInterrupt(other: Mutator) = priority >= other.priority

        fun cancel() = job.cancel()
    }

    private val currentMutator = AtomicReference<Mutator?>(null)
    private val mutex = Mutex()

    private fun tryMutateOrCancel(mutator: Mutator) {
        while (true) {
            val oldMutator = currentMutator.get()
            if (oldMutator == null || mutator.canInterrupt(oldMutator)) {
                if (currentMutator.compareAndSet(oldMutator, mutator)) {
                    oldMutator?.cancel()
                    break
                }
            } else throw CancellationException("Current mutation had a higher priority")
        }
    }

    /**
     * Enforce that only a single caller may be active at a time.
     *
     * If [mutate] is called while another call to [mutate] or [mutateWith] is in progress, their
     * [priority] values are compared. If the new caller has a [priority] equal to or higher than
     * the call in progress, the call in progress will be cancelled, throwing
     * [CancellationException] and the new caller's [block] will be invoked. If the call in
     * progress had a higher [priority] than the new caller, the new caller will throw
     * [CancellationException] without invoking [block].
     *
     * @param priority the priority of this mutation; [MutatePriority.Default] by default.
     * Higher priority mutations will interrupt lower priority mutations.
     * @param block mutation code to run mutually exclusive with any other call to [mutate],
     * [mutateWith] or [tryMutate].
     */
    suspend fun <R> mutate(
        priority: MutatePriority = MutatePriority.Default,
        block: suspend () -> R
    ) = coroutineScope {
        val mutator = Mutator(priority, coroutineContext[Job]!!)

        tryMutateOrCancel(mutator)

        mutex.withLock {
            try {
                block()
            } finally {
                currentMutator.compareAndSet(mutator, null)
            }
        }
    }

    /**
     * Enforce that only a single caller may be active at a time.
     *
     * If [mutateWith] is called while another call to [mutate] or [mutateWith] is in progress,
     * their [priority] values are compared. If the new caller has a [priority] equal to or
     * higher than the call in progress, the call in progress will be cancelled, throwing
     * [CancellationException] and the new caller's [block] will be invoked. If the call in
     * progress had a higher [priority] than the new caller, the new caller will throw
     * [CancellationException] without invoking [block].
     *
     * This variant of [mutate] calls its [block] with a [receiver], removing the need to create
     * an additional capturing lambda to invoke it with a receiver object. This can be used to
     * expose a mutable scope to the provided [block] while leaving the rest of the state object
     * read-only. For example:
     *
     * @param receiver the receiver `this` that [block] will be called with
     * @param priority the priority of this mutation; [MutatePriority.Default] by default.
     * Higher priority mutations will interrupt lower priority mutations.
     * @param block mutation code to run mutually exclusive with any other call to [mutate],
     * [mutateWith] or [tryMutate].
     */
    suspend fun <T, R> mutateWith(
        receiver: T,
        priority: MutatePriority = MutatePriority.Default,
        block: suspend T.() -> R
    ) = coroutineScope {
        val mutator = Mutator(priority, coroutineContext[Job]!!)

        tryMutateOrCancel(mutator)

        mutex.withLock {
            try {
                receiver.block()
            } finally {
                currentMutator.compareAndSet(mutator, null)
            }
        }
    }

    /**
     * Attempt to mutate synchronously if there is no other active caller.
     * If there is no other active caller, the [block] will be executed in a lock. If there is
     * another active caller, this method will return false, indicating that the active caller
     * needs to be cancelled through a [mutate] or [mutateWith] call with an equal or higher
     * mutation priority.
     *
     * Calls to [mutate] and [mutateWith] will suspend until execution of the [block] has finished.
     *
     * @param block mutation code to run mutually exclusive with any other call to [mutate],
     * [mutateWith] or [tryMutate].
     * @return true if the [block] was executed, false if there was another active caller and the
     * [block] was not executed.
     */
    fun tryMutate(block: () -> Unit): Boolean {
        val didLock = mutex.tryLock()
        if (didLock) {
            try {
                block()
            } finally {
                mutex.unlock()
            }
        }
        return didLock
    }
}