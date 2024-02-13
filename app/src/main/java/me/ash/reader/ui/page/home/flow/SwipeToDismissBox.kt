package me.ash.reader.ui.page.home.flow

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

import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBoxState.Companion.Saver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

/**
 * The directions in which a [SwipeToDismissBox] can be dismissed.
 */
@ExperimentalMaterial3Api
enum class SwipeToDismissBoxValue {
    /**
     * Can be dismissed by swiping in the reading direction.
     */
    StartToEnd,

    /**
     * Can be dismissed by swiping in the reverse of the reading direction.
     */
    EndToStart,

    /**
     * Cannot currently be dismissed.
     */
    Settled
}

/**
 * State of the [SwipeToDismissBox] composable.
 *
 * @param initialValue The initial value of the state.
 * @param density The density that this state can use to convert values to and from dp.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 * @param positionalThreshold The positional threshold to be used when calculating the target state
 * while a swipe is in progress and when settling after the swipe ends. This is the distance from
 * the start of a transition. It will be, depending on the direction of the interaction, added or
 * subtracted from/to the origin offset. It should always be a positive value.
 */
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterial3Api
class SwipeToDismissBoxState(
    initialValue: SwipeToDismissBoxValue,
    internal val density: Density,
    animationSpec: AnimationSpec<Float> = spring(),
    confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true },
    velocityThreshold: () -> Float = { with(density) { DismissThreshold.toPx() } },
    positionalThreshold: (totalDistance: Float) -> Float
) {
    internal val anchoredDraggableState = AnchoredDraggableState(
        initialValue = initialValue,
        animationSpec = animationSpec,
        confirmValueChange = confirmValueChange,
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold
    )

    internal val offset: Float get() = anchoredDraggableState.offset

    /**
     * Require the current offset.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float = anchoredDraggableState.requireOffset()

    /**
     * The current state value of the [SwipeToDismissBoxState].
     */
    val currentValue: SwipeToDismissBoxValue get() = anchoredDraggableState.currentValue

    /**
     * The target state. This is the closest state to the current offset (taking into account
     * positional thresholds). If no interactions like animations or drags are in progress, this
     * will be the current state.
     */
    val targetValue: SwipeToDismissBoxValue get() = anchoredDraggableState.targetValue

    /**
     * The fraction of the progress going from currentValue to targetValue, within [0f..1f] bounds.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    val progress: Float get() = anchoredDraggableState.progress

    /**
     * The direction (if any) in which the composable has been or is being dismissed.
     *
     * Use this to change the background of the [SwipeToDismissBox] if you want different actions on each
     * side.
     */
    val dismissDirection: SwipeToDismissBoxValue
        get() = if (offset == 0f || offset.isNaN())
            SwipeToDismissBoxValue.Settled
        else if (offset > 0f)
            SwipeToDismissBoxValue.StartToEnd else SwipeToDismissBoxValue.EndToStart

    /**
     * Whether the component has been dismissed in the given [direction].
     *
     * @param direction The dismiss direction.
     */
    @Deprecated(
        message = "DismissDirection is no longer used by SwipeToDismissBoxState. Please compare " +
                "currentValue against SwipeToDismissValue instead.",
        level = DeprecationLevel.HIDDEN
    )
    @Suppress("DEPRECATION")
    fun isDismissed(direction: DismissDirection): Boolean {
        return currentValue == (
                if (direction == DismissDirection.StartToEnd) {
                    SwipeToDismissBoxValue.StartToEnd
                } else {
                    SwipeToDismissBoxValue.EndToStart
                }
                )
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
     * reset or animation has been cancelled. This method will throw [CancellationException] if
     * the animation is interrupted
     *
     * @return the reason the reset animation ended
     */
    suspend fun reset() = anchoredDraggableState.animateTo(
        targetValue = SwipeToDismissBoxValue.Settled
    )

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
            animationSpec: AnimationSpec<Float>,
            density: Density
        ) = Saver<SwipeToDismissBoxState, SwipeToDismissBoxValue>(
            save = { it.currentValue },
            restore = {
                SwipeToDismissBoxState(
                    it,
                    density,
                    animationSpec,
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
 * @param background A composable that is stacked behind the content and is exposed when the
 * content is swiped. You can/should use the [state] to have different backgrounds on each side.
 * @param dismissContent The content that can be dismissed.
 * @param modifier Optional [Modifier] for this component.
 * @param directions The set of directions in which the component can be dismissed.
 */
@Composable
@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use SwipeToDismissBox instead",
    replaceWith =
    ReplaceWith(
        "SwipeToDismissBox(state, background, modifier, " +
                "enableDismissFromStartToEnd, enableDismissFromEndToStart, dismissContent)"
    )
)
@ExperimentalMaterial3Api
fun SwipeToDismiss(
    state: SwipeToDismissBoxState,
    background: @Composable RowScope.() -> Unit,
    dismissContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    directions: Set<SwipeToDismissBoxValue> = setOf(
        SwipeToDismissBoxValue.EndToStart,
        SwipeToDismissBoxValue.StartToEnd
    ),
) = SwipeToDismissBox(
    state = state,
    backgroundContent = background,
    modifier = modifier,
    enableDismissFromStartToEnd = SwipeToDismissBoxValue.StartToEnd in directions,
    enableDismissFromEndToStart = SwipeToDismissBoxValue.EndToStart in directions,
    content = dismissContent
)

/**
 * A composable that can be dismissed by swiping left or right.
 *
 * @sample androidx.compose.material3.samples.SwipeToDismissListItems
 *
 * @param state The state of this component.
 * @param backgroundContent A composable that is stacked behind the [content] and is exposed when the
 * content is swiped. You can/should use the [state] to have different backgrounds on each side.
 * @param modifier Optional [Modifier] for this component.
 * @param enableDismissFromStartToEnd Whether SwipeToDismissBox can be dismissed from start to end.
 * @param enableDismissFromEndToStart Whether SwipeToDismissBox can be dismissed from end to start.
 * @param content The content that can be dismissed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@ExperimentalMaterial3Api
fun SwipeToDismissBox(
    state: SwipeToDismissBoxState,
    backgroundContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
//    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(
        modifier
            .anchoredDraggable(
                state = state.anchoredDraggableState,
                orientation = Orientation.Horizontal,
                enabled = enabled && state.currentValue == SwipeToDismissBoxValue.Settled,
//                reverseDirection = isRtl,
            ),
        propagateMinConstraints = true
    ) {
        Row(
            content = backgroundContent,
            modifier = Modifier.matchParentSize()
        )
        Row(
            content = content,
            modifier = Modifier.swipeToDismissBoxAnchors(
                state,
                enableDismissFromStartToEnd,
                enableDismissFromEndToStart
            )
        )
    }
}

/** Contains default values for [SwipeToDismissBox] and [SwipeToDismissBoxState]. */
@ExperimentalMaterial3Api
object SwipeToDismissBoxDefaults {
    /** Default positional threshold of 56.dp for [SwipeToDismissBoxState]. */
    val positionalThreshold: (totalDistance: Float) -> Float
        @Composable get() = with(LocalDensity.current) {
            { 56.dp.toPx() }
        }
}

/**
 * The directions in which a [SwipeToDismissBox] can be dismissed.
 */
@ExperimentalMaterial3Api
@Deprecated(
    message = "Dismiss direction is no longer used by SwipeToDismissBoxState. Please use " +
            "SwipeToDismissBoxValue instead.",
    level = DeprecationLevel.WARNING
)
enum class DismissDirection {
    /**
     * Can be dismissed by swiping in the reading direction.
     */
    StartToEnd,

    /**
     * Can be dismissed by swiping in the reverse of the reading direction.
     */
    EndToStart,
}

/**
 * Possible values of [SwipeToDismissBoxState].
 */
@ExperimentalMaterial3Api
@Deprecated(
    message = "DismissValue is no longer used by SwipeToDismissBoxState. Please use " +
            "SwipeToDismissBoxValue instead.",
    level = DeprecationLevel.WARNING
)
enum class DismissValue {
    /**
     * Indicates the component has not been dismissed yet.
     */
    Default,

    /**
     * Indicates the component has been dismissed in the reading direction.
     */
    DismissedToEnd,

    /**
     * Indicates the component has been dismissed in the reverse of the reading direction.
     */
    DismissedToStart
}

private val DismissThreshold = 125.dp

@OptIn(ExperimentalMaterial3Api::class)
private fun Modifier.swipeToDismissBoxAnchors(
    state: SwipeToDismissBoxState,
    enableDismissFromStartToEnd: Boolean,
    enableDismissFromEndToStart: Boolean
) = this then SwipeToDismissAnchorsElement(
    state,
    enableDismissFromStartToEnd,
    enableDismissFromEndToStart
)

@OptIn(ExperimentalMaterial3Api::class)
private class SwipeToDismissAnchorsElement(
    private val state: SwipeToDismissBoxState,
    private val enableDismissFromStartToEnd: Boolean,
    private val enableDismissFromEndToStart: Boolean,
) : ModifierNodeElement<SwipeToDismissAnchorsNode>() {

    override fun create() = SwipeToDismissAnchorsNode(
        state,
        enableDismissFromStartToEnd,
        enableDismissFromEndToStart,
    )

    override fun update(node: SwipeToDismissAnchorsNode) {
        node.state = state
        node.enableDismissFromStartToEnd = enableDismissFromStartToEnd
        node.enableDismissFromEndToStart = enableDismissFromEndToStart
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as SwipeToDismissAnchorsElement
        if (state != other.state) return false
        if (enableDismissFromStartToEnd != other.enableDismissFromStartToEnd) return false
        if (enableDismissFromEndToStart != other.enableDismissFromEndToStart) return false
        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + enableDismissFromStartToEnd.hashCode()
        result = 31 * result + enableDismissFromEndToStart.hashCode()
        return result
    }

    override fun InspectorInfo.inspectableProperties() {
        debugInspectorInfo {
            properties["state"] = state
            properties["enableDismissFromStartToEnd"] = enableDismissFromStartToEnd
            properties["enableDismissFromEndToStart"] = enableDismissFromEndToStart
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private class SwipeToDismissAnchorsNode(
    var state: SwipeToDismissBoxState,
    var enableDismissFromStartToEnd: Boolean,
    var enableDismissFromEndToStart: Boolean,
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
        if (isLookingAhead || !didLookahead) {
            val width = placeable.width.toFloat()
            val newAnchors = DraggableAnchors {
                SwipeToDismissBoxValue.Settled at 0f
                if (enableDismissFromStartToEnd) {
                    SwipeToDismissBoxValue.StartToEnd at width
                }
                if (enableDismissFromEndToStart) {
                    SwipeToDismissBoxValue.EndToStart at -width
                }
            }
            state.anchoredDraggableState.updateAnchors(newAnchors)
        }
        didLookahead = isLookingAhead || didLookahead
        return layout(placeable.width, placeable.height) {
            // In a lookahead pass, we use the position of the current target as this is where any
            // ongoing animations would move. If SwipeToDismissBox is in a settled state, lookahead
            // and post-lookahead will converge.
            val xOffset = if (isLookingAhead) {
                state.anchoredDraggableState.anchors.positionOf(state.targetValue)
            } else state.requireOffset()
            placeable.place(xOffset.roundToInt(), 0)
        }
    }
}
