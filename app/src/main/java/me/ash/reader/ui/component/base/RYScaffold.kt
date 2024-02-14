package me.ash.reader.ui.component.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.palette.onDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RYScaffold(
    containerColor: Color = MaterialTheme.colorScheme.surface,
    topBarTonalElevation: Dp = 0.dp,
    containerTonalElevation: Dp = 0.dp,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    topBarTonalElevation,
                    color = containerColor
                )
            ),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
            containerTonalElevation,
            color = containerColor
        ) onDark MaterialTheme.colorScheme.surface,
        topBar = {
            if (navigationIcon != null || actions != null) {
                TopAppBar(
                    title = {},
                    navigationIcon = { navigationIcon?.invoke() },
                    actions = { actions?.invoke(this) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            topBarTonalElevation
                        ),
                    )
                )
            }
        },
        content = {
            Column {
                Spacer(modifier = Modifier.height(it.calculateTopPadding()))
                content()
            }
        },
        bottomBar = { bottomBar?.invoke() },
        floatingActionButton = { floatingActionButton?.invoke() },
    )
}
