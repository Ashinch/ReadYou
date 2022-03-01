import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalAnimationApi
@Suppress("UpdateTransitionLabel", "TransitionPropertiesLabel")
@SuppressLint("ComposableNaming", "UnusedTransitionTargetStateParameter")
/**
 * @param state Use [updateAnimatedItemsState].
 */
inline fun <T> LazyListScope.animatedItemsIndexed(
    state: List<AnimatedItem<T>>,
    enterTransition: EnterTransition = expandVertically(),
    exitTransition: ExitTransition = shrinkVertically(),
    noinline key: ((item: T) -> Any)? = null,
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit
) {
    items(
        state.size,
        if (key != null) { keyIndex: Int -> key(state[keyIndex].item) } else null
    ) { index ->

        val item = state[index]
        val visibility = item.visibility

        key(key?.invoke(item.item)) {
            AnimatedVisibility(
                visibleState = visibility,
                enter = enterTransition,
                exit = exitTransition
            ) {
                itemContent(index, item.item)
            }
        }
    }
}

@Composable
fun <T> updateAnimatedItemsState(
    newList: List<T>
): State<List<AnimatedItem<T>>> {

    val state = remember { mutableStateOf(emptyList<AnimatedItem<T>>()) }
    LaunchedEffect(newList) {
        if (state.value == newList) {
            return@LaunchedEffect
        }
        val oldList = state.value.toList()

        val diffCb = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].item == newList[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].item == newList[newItemPosition]

        }
        val diffResult = calculateDiff(false, diffCb)
        val compositeList = oldList.toMutableList()

        diffResult.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                for (i in 0 until count) {
                    val newItem = AnimatedItem(
                        visibility = MutableTransitionState(false),
                        newList[position + i]
                    )
                    newItem.visibility.targetState = true
                    compositeList.add(position + i, newItem)
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                for (i in 0 until count) {
                    compositeList[position + i].visibility.targetState = false
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                // not detecting moves.
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                // irrelevant with compose.
            }
        })
        if (state.value != compositeList) {
            state.value = compositeList
        }
        val initialAnimation = Animatable(1.0f)
        initialAnimation.animateTo(0f)
        state.value = state.value.filter { it.visibility.targetState }
    }

    return state
}

data class AnimatedItem<T>(
    val visibility: MutableTransitionState<Boolean>,
    val item: T,
) {

    override fun hashCode(): Int {
        return item?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimatedItem<*>

        if (item != other.item) return false

        return true
    }
}

suspend fun calculateDiff(
    detectMoves: Boolean = true,
    diffCb: DiffUtil.Callback
): DiffUtil.DiffResult {
    return withContext(Dispatchers.Unconfined) {
        DiffUtil.calculateDiff(diffCb, detectMoves)
    }
}
