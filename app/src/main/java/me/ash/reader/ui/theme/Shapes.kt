package me.ash.reader.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.0.dp),
    small = RoundedCornerShape(8.0.dp),
    medium = RoundedCornerShape(12.0.dp),
    large = RoundedCornerShape(16.0.dp),
    extraLarge = RoundedCornerShape(28.0.dp)
)

@Stable
val Shape20 = RoundedCornerShape(20.0.dp)

@Stable
val Shape32 = RoundedCornerShape(32.0.dp)

@Stable
val ShapeTop32 = RoundedCornerShape(32.0.dp, 32.0.dp, 0.0.dp, 0.0.dp)

@Stable
val ShapeBottom32 = RoundedCornerShape(0.0.dp, 0.0.dp, 32.0.dp, 32.0.dp)
