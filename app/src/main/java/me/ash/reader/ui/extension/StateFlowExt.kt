package me.ash.reader.ui.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun <T> StateFlow<T>.collectAsStateValue(
    context: CoroutineContext = EmptyCoroutineContext
): T = collectAsState(context).value
