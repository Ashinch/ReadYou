package me.ash.reader.ui.widget

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AnimateLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    reference: Any?,
    content: LazyListScope.() -> Unit,
) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(reference) {
        Log.i("RLog", "reference change")
        visible = false
//        delay(50)
        visible = true
    }

    AnimatedVisibility(
        modifier = modifier.fillMaxSize(),
        visible = visible,
        enter = fadeIn() + expandVertically(),
    ) {
        LazyColumn(
            modifier = modifier,
            state = state,
            content = content,
        )
    }
}