package me.ash.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

fun Int.positive() = if (this < 0) 0 else this
fun Int.finitelyLarge(value: Int) = if (this > value) value else this
fun Int.finitelySmall(value: Int) = if (this < value) value else this

fun Float.positive() = if (this < 0) 0f else this
fun Float.finitelyLarge(value: Float) = if (this > value) value else this
fun Float.finitelySmall(value: Float) = if (this < value) value else this

@Composable
fun <T : Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        elements.toMutableList().toMutableStateList()
    }
}