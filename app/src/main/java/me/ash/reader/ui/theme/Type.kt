package me.ash.reader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import java.text.Bidi

// TODO: Rename file to Typography.kt and add @Stable

val SystemTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

internal fun TextStyle.applyTextDirection(textDirection: TextDirection = TextDirection.Content) =
    this.copy(textDirection = textDirection)

/**
 * Resolve the text to Rtl if the text requires BiDirectional
 * @see [android.view.View.TEXT_DIRECTION_ANY_RTL]
 * @see [Bidi.requiresBidi]
 */
fun TextStyle.applyTextDirection(requiresBidi: Boolean) =
    this.applyTextDirection(textDirection = if (requiresBidi) TextDirection.Rtl else TextDirection.Ltr)

internal fun Typography.applyTextDirection() = this.copy(
    displayLarge = displayLarge.applyTextDirection(),
    displayMedium = displayMedium.applyTextDirection(),
    displaySmall = displaySmall.applyTextDirection(),
    headlineLarge = headlineLarge.applyTextDirection(),
    headlineMedium = headlineMedium.applyTextDirection(),
    headlineSmall = headlineSmall.applyTextDirection(),
    titleLarge = titleLarge.applyTextDirection(),
    titleMedium = titleMedium.applyTextDirection(),
    titleSmall = titleSmall.applyTextDirection(),
    bodyLarge = bodyLarge.applyTextDirection(),
    bodyMedium = bodyMedium.applyTextDirection(),
    bodySmall = bodySmall.applyTextDirection(),
    labelLarge = labelLarge.applyTextDirection(),
    labelMedium = labelMedium.applyTextDirection(),
    labelSmall = labelSmall.applyTextDirection(),
)
