package me.ash.reader.ui.component.base

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin

val curlyCornerShape = CurlyCornerShape()

class CurlyCornerShape(
    private val amp: Double = 16.0,
    private val count: Int = 12,
) : CornerBasedShape(
    topStart = ZeroCornerSize,
    topEnd = ZeroCornerSize,
    bottomEnd = ZeroCornerSize,
    bottomStart = ZeroCornerSize
) {

    private fun sineCircleXYatAngle(
        d1: Double,
        d2: Double,
        d3: Double,
        d4: Double,
        d5: Double,
        i: Int,
    ): List<Double> = (i.toDouble() * d5).run {
        listOf(
            (sin(this) * d4 + d3) * cos(d5) + d1,
            (sin(this) * d4 + d3) * sin(d5) + d2
        )
    }

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline {
        val d = 2.0
        val r2: Double = size.width / d
        var r13: Double = size.height / d
        val r18 = size.width / 2.0 - amp
        val path = Path()
        path.moveTo((d * r2 - amp).toFloat(), r13.toFloat())
        var i = 0
        while (true) {
            val i2 = i + 1
            val d3 = r13
            val r5: List<Double> = sineCircleXYatAngle(
                r2, r13, r18, amp, Math.toRadians(
                    i.toDouble()
                ), count
            )
            path.lineTo(r5[0].toFloat(), r5[1].toFloat())
            if (i2 >= 360) {
                path.close()
                return Outline.Generic(path)
            }
            i = i2
            r13 = d3
        }
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ) = RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomEnd = bottomEnd,
        bottomStart = bottomStart
    )
}
