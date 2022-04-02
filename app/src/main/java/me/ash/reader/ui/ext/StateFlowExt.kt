package me.ash.reader.ui.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

@Composable
fun <T> StateFlow<T>.collectAsStateValue(
    context: CoroutineContext = Dispatchers.Default
): T = collectAsState(context).value