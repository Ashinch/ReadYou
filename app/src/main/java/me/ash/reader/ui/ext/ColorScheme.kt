package me.ash.reader.ui.ext

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.domain.model.constant.ElevationTokens
import kotlin.math.ln
import kotlin.math.roundToInt

@Composable
@Deprecated(
    message = "Migrate to tone-based surfaces", level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
        "surfaceColorAtElevation(elevation: Dp)",
        "androidx.compose.runtime.remember"
    )
)
fun ColorScheme.surfaceColorAtElevation(
    elevation: Dp,
    color: Color = surface,
): Color = surfaceColorAtElevation(elevation = elevation)

@Composable
fun ColorScheme.surfaceColorAtElevation(
    elevation: Dp,
): Color = remember(this, elevation) {
    when (elevation.value.roundToInt()) {
        ElevationTokens.Level0 -> surface
        ElevationTokens.Level1 -> surfaceContainerLow
        ElevationTokens.Level2 -> surfaceContainer
        ElevationTokens.Level3 -> surfaceContainerHigh
        ElevationTokens.Level4, ElevationTokens.Level5 -> surfaceContainerHighest
        else -> surface
    }
}

fun Color.atElevation(
    sourceColor: Color,
    elevation: Dp,
): Color {
    if (elevation == 0.dp) return this
    return sourceColor.copy(alpha = elevation.alphaLN(constant = 4.5f)).compositeOver(this)
}

fun Dp.alphaLN(constant: Float = 1f, weight: Float = 0f): Float =
    ((constant * ln(value + 1) + weight) + 2f) / 100f
