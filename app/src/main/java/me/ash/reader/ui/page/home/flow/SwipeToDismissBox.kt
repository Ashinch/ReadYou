/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalFoundationApi::class)

package me.ash.reader.ui.page.home.flow

import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import me.ash.reader.ui.page.home.flow.SwipeToDismissBoxState.Companion.Saver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

/** The directions in which a [SwipeToDismissBox] can be dismissed. */
enum class SwipeToDismissBoxValue {
    /** Can be dismissed by swiping in the reading direction. */
    StartToEnd,

    /** Can be dismissed by swiping in the reverse of the reading direction. */
    EndToStart,

    /** Cannot currently be dismissed. */
    Settled
}

/**
 * State of the [SwipeToDismissBox] composable.
 *
 * @param initialValue The initial value of the state.
 * @param density The density that this state can use to convert values to and from dp.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 * @param positionalThreshold The positional threshold to be used when calculating the target state
 *   while a swipe is in progress and when settling after the swipe ends. This is the distance from
 *   the start of a transition. It will be, depending on the direction of the interaction, added or
 *   subtracted from/to the origin offset. It should always be a positive value.
 */
@OptIn(ExperimentalFoundationApi::class)
class SwipeToDismissBoxState(
    initialValue: SwipeToDismissBoxValue,
    internal val density: Density,
    snapAnimationSpec: AnimationSpec<Float> = spring(),
    decayAnimationSpec: DecayAnimationSpec<Float> = exponentialDecay(),
    confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true },
    velocityThreshold: () -> Float = { with(density) { DismissVelocityThreshold.toPx() } },
    positionalThreshold: (totalDistance: Float) -> Float
) {
    internal val anchoredDraggableState =
        AnchoredDraggableState(
            initialValue = initialValue,
            snapAnimationSpec = snapAnimationSpec,
            decayAnimationSpec = decayAnimationSpec,
            confirmValueChange = confirmValueChange,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold
        )

    internal val offset: Float
        get() = anchoredDraggableState.offset

    /**
     * Require the current offset.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float = anchoredDraggableState.requireOffset()

    /** The current state value of the [SwipeToDismissBoxState]. */
    val currentValue: SwipeToDismissBoxValue
        get() = anchoredDraggableState.currentValue

    /**
     * The target state. This is the closest state to the current offset (taking into account
     * positional thresholds). If no interactions like animations or drags are in progress, this
     * will be the current state.
     */
    val targetValue: SwipeToDismissBoxValue
        get() = anchoredDraggableState.targetValue

    fun progress(from: SwipeToDismissBoxValue, to: SwipeToDismissBoxValue): Float =
        anchoredDraggableState.progress(from = from, to = to)

    /**
     * The direction (if any) in which the composable has been or is being dismissed.
     *
     * Use this to change the background of the [SwipeToDismissBox] if you want different actions on
     * each side.
     */
    val dismissDirection: SwipeToDismissBoxValue
        get() =
            when {
                offset == 0f || offset.isNaN() -> SwipeToDismissBoxValue.Settled
                offset > 0f -> SwipeToDismissBoxValue.StartToEnd
                else -> SwipeToDismissBoxValue.EndToStart
            }

    /**
     * Set the state without any animation and suspend until it's set
     *
     * @param targetValue The new target value
     */
    suspend fun snapTo(targetValue: SwipeToDismissBoxValue) {
        anchoredDraggableState.snapTo(targetValue)
    }

    /**
     * Reset the component to the default position with animation and suspend until it if fully
     * reset or animation has been cancelled. This method will throw [CancellationException] if the
     * animation is interrupted
     *
     * @return the reason the reset animation ended
     */
    suspend fun reset() =
        anchoredDraggableState.animateTo(targetValue = SwipeToDismissBoxValue.Settled)

    /**
     * Dismiss the component in the given [direction], with an animation and suspend. This method
     * will throw [CancellationException] if the animation is interrupted
     *
     * @param direction The dismiss direction.
     */
    suspend fun dismiss(direction: SwipeToDismissBoxValue) {
        anchoredDraggableState.animateTo(targetValue = direction)
    }

    companion object {

        /**
         * The default [Saver] implementation for [SwipeToDismissBoxState].
         */
        fun Saver(
            confirmValueChange: (SwipeToDismissBoxValue) -> Boolean,
            positionalThreshold: (totalDistance: Float) -> Float,
            velocityThreshold: () -> Float,
            snapAnimationSpec: AnimationSpec<Float>,
            decayAnimationSpec: DecayAnimationSpec<Float>,
            density: Density
        ) = Saver<SwipeToDismissBoxState, SwipeToDismissBoxValue>(
            save = { it.currentValue },
            restore = {
                SwipeToDismissBoxState(
                    it,
                    density,
                    snapAnimationSpec,
                    decayAnimationSpec,
                    confirmValueChange,
                    velocityThreshold,
                    positionalThreshold
                )
            }
        )
    }
}

/**
 * A composable that can be dismissed by swiping left or right.
 *
 * @sample androidx.compose.material3.samples.SwipeToDismissListItems
 *
 * @param state The state of this component.
 * @param backgroundContent A composable that is stacked behind the [content] and is exposed when
 *   the content is swiped. You can/should use the [state] to have different backgrounds on each
 *   side.
 * @param modifier Optional [Modifier] for this component.
 * @param enableDismissFromStartToEnd Whether SwipeToDismissBox can be dismissed from start to end.
 * @param enableDismissFromEndToStart Whether SwipeToDismissBox can be dismissed from end to start.
 * @param gesturesEnabled Whether swipe-to-dismiss can be interacted by gestures.
 * @param content The content that can be dismissed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDismissBox(
    state: SwipeToDismissBoxState,
    backgroundContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    gesturesEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(
        modifier.anchoredDraggable(
            state = state.anchoredDraggableState,
            orientation = Orientation.Horizontal,
            enabled = gesturesEnabled && state.currentValue == SwipeToDismissBoxValue.Settled,
            startDragImmediately = false
        ),
        propagateMinConstraints = true
    ) {
        Row(content = backgroundContent, modifier = Modifier.matchParentSize())
        Row(
            content = content,
            modifier =
            Modifier.draggableAnchors(state.anchoredDraggableState, Orientation.Horizontal) { size,
                                                                                              _ ->
                val width = size.width.toFloat()
                return@draggableAnchors DraggableAnchors {
                    SwipeToDismissBoxValue.Settled at 0f
                    if (enableDismissFromStartToEnd) {
                        SwipeToDismissBoxValue.StartToEnd at (if (isRtl) -width else width)
                    }
                    if (enableDismissFromEndToStart) {
                        SwipeToDismissBoxValue.EndToStart at (if (isRtl) width else -width)
                    }
                } to state.targetValue
            }
        )
    }
}

/** Contains default values for [SwipeToDismissBox] and [SwipeToDismissBoxState]. */
object SwipeToDismissBoxDefaults {
    /** Default positional threshold of 56.dp for [SwipeToDismissBoxState]. */
    val positionalThreshold: (totalDistance: Float) -> Float
        @Composable get() = with(LocalDensity.current) { { 56.dp.toPx() } }
}

private val DismissVelocityThreshold = 125.dp


/**
 * This Modifier allows configuring an [AnchoredDraggableState]'s anchors based on this layout
 * node's size and offsetting it. It considers lookahead and reports the appropriate size and
 * measurement for the appropriate phase.
 *
 * @param state The state the anchors should be attached to
 * @param orientation The orientation the component should be offset in
 * @param anchors Lambda to calculate the anchors based on this layout's size and the incoming
 *   constraints. These can be useful to avoid subcomposition.
 */
internal fun <T> Modifier.draggableAnchors(
    state: AnchoredDraggableState<T>,
    orientation: Orientation,
    anchors: (size: IntSize, constraints: Constraints) -> Pair<DraggableAnchors<T>, T>,
) = this then DraggableAnchorsElement(state, anchors, orientation)

private class DraggableAnchorsElement<T>(
    private val state: AnchoredDraggableState<T>,
    private val anchors: (size: IntSize, constraints: Constraints) -> Pair<DraggableAnchors<T>, T>,
    private val orientation: Orientation
) : ModifierNodeElement<DraggableAnchorsNode<T>>() {

    override fun create() = DraggableAnchorsNode(state, anchors, orientation)

    override fun update(node: DraggableAnchorsNode<T>) {
        node.state = state
        node.anchors = anchors
        node.orientation = orientation
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is DraggableAnchorsElement<*>) return false

        if (state != other.state) return false
        if (anchors !== other.anchors) return false
        if (orientation != other.orientation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + anchors.hashCode()
        result = 31 * result + orientation.hashCode()
        return result
    }

    override fun InspectorInfo.inspectableProperties() {
        debugInspectorInfo {
            properties["state"] = state
            properties["anchors"] = anchors
            properties["orientation"] = orientation
        }
    }
}

private class DraggableAnchorsNode<T> constructor(
    var state: AnchoredDraggableState<T>,
    var anchors: (size: IntSize, constraints: Constraints) -> Pair<DraggableAnchors<T>, T>,
    var orientation: Orientation
) : Modifier.Node(), LayoutModifierNode {
    private var didLookahead: Boolean = false

    override fun onDetach() {
        didLookahead = false
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        // If we are in a lookahead pass, we only want to update the anchors here and not in
        // post-lookahead. If there is no lookahead happening (!isLookingAhead && !didLookahead),
        // update the anchors in the main pass.
        if (!isLookingAhead || !didLookahead) {
            val size = IntSize(placeable.width, placeable.height)
            val newAnchorResult = anchors(size, constraints)
            state.updateAnchors(newAnchorResult.first, newAnchorResult.second)
        }
        didLookahead = isLookingAhead || didLookahead
        return layout(placeable.width, placeable.height) {
            // In a lookahead pass, we use the position of the current target as this is where any
            // ongoing animations would move. If the component is in a settled state, lookahead
            // and post-lookahead will converge.
            val offset =
                if (isLookingAhead) {
                    state.anchors.positionOf(state.targetValue)
                } else state.requireOffset()
            val xOffset = if (orientation == Orientation.Horizontal) offset else 0f
            val yOffset = if (orientation == Orientation.Vertical) offset else 0f
            placeable.place(xOffset.roundToInt(), yOffset.roundToInt())
        }
    }
}