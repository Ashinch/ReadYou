package me.ash.reader.ui.motion

import androidx.compose.animation.core.CubicBezierEasing

val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

const val EnterDuration = 400
const val ExitDuration = 200

