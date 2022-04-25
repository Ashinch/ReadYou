package me.ash.reader.ui.theme.palette.util

import kotlin.math.PI

internal fun square(x: Double): Double = x * x

internal fun Double.toRadians(): Double = this * PI / 180.0
internal fun Double.toDegrees(): Double = this * 180.0 / PI

operator fun DoubleArray.times(x: Double): DoubleArray = map { it * x }.toDoubleArray()
operator fun DoubleArray.div(x: Double): DoubleArray = map { it / x }.toDoubleArray()

class Matrix3(
    private val x: DoubleArray,
    private val y: DoubleArray,
    private val z: DoubleArray,
) {
    fun inverse(): Matrix3 {
        val det = determinant()
        return Matrix3(
            doubleArrayOf(
                (y[1] * z[2] - y[2] * z[1]) / det,
                (y[2] * z[0] - y[0] * z[2]) / det,
                (y[0] * z[1] - y[1] * z[0]) / det,
            ),
            doubleArrayOf(
                (x[2] * z[1] - x[1] * z[2]) / det,
                (x[0] * z[2] - x[2] * z[0]) / det,
                (x[1] * z[0] - x[0] * z[1]) / det,
            ),
            doubleArrayOf(
                (x[1] * y[2] - x[2] * y[1]) / det,
                (x[2] * y[0] - x[0] * y[2]) / det,
                (x[0] * y[1] - x[1] * y[0]) / det,
            ),
        ).transpose()
    }

    private fun determinant(): Double =
        x[0] * (y[1] * z[2] - y[2] * z[1]) -
                x[1] * (y[0] * z[2] - y[2] * z[0]) +
                x[2] * (y[0] * z[1] - y[1] * z[0])

    private fun transpose() = Matrix3(
        doubleArrayOf(x[0], y[0], z[0]),
        doubleArrayOf(x[1], y[1], z[1]),
        doubleArrayOf(x[2], y[2], z[2]),
    )

    operator fun get(i: Int): DoubleArray = when (i) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("Index must be 0, 1 or 2")
    }

    operator fun times(vec: DoubleArray): DoubleArray = doubleArrayOf(
        x[0] * vec[0] + x[1] * vec[1] + x[2] * vec[2],
        y[0] * vec[0] + y[1] * vec[1] + y[2] * vec[2],
        z[0] * vec[0] + z[1] * vec[1] + z[2] * vec[2],
    )

    override fun toString(): String =
        "{" + arrayOf(x, y, z).joinToString { "[" + it.joinToString() + "]" } + "}"
}
