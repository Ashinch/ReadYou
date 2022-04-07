package me.ash.reader.ui.component

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.google.accompanist.insets.LocalWindowInsets

@Composable
fun AnimatedPopup(
    visible: Boolean = false,
    absoluteY: Dp = Dp.Hairline,
    absoluteX: Dp = Dp.Hairline,
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val density = LocalDensity.current
    val insets = LocalWindowInsets.current

    Popup(
        properties = PopupProperties(focusable = visible),
        onDismissRequest = onDismissRequest,
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                return IntOffset(
                    x = with(density) { (absoluteX).roundToPx() },
                    y = with(density) { (absoluteY).roundToPx() + insets.statusBars.top }
                )
            }
        },
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            content()
        }
    }
}