package me.ash.reader.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import java.text.Bidi

// TODO: Rename file to Typography.kt and add @Stable

private val LabelSmallEmphasizedFont = FontFamily.SansSerif
private val LabelSmallEmphasizedLineHeight = 16.0.sp
private val LabelSmallEmphasizedSize = 11.sp
private val LabelSmallEmphasizedTracking = 0.5.sp
private val LabelSmallEmphasizedWeight = FontWeight.Bold

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val SystemTypography =
    Typography(
        bodySmallEmphasized =
            TextStyle.Default.copy(
                fontFamily = LabelSmallEmphasizedFont,
                fontSize = LabelSmallEmphasizedSize,
                fontWeight = LabelSmallEmphasizedWeight,
                letterSpacing = LabelSmallEmphasizedTracking,
                lineHeight = LabelSmallEmphasizedLineHeight,
            )
    )

internal fun TextStyle.applyTextDirection(textDirection: TextDirection = TextDirection.Content) =
    this.copy(textDirection = textDirection)

/**
 * Resolve the text to Rtl if the text requires BiDirectional
 *
 * @see [android.view.View.TEXT_DIRECTION_ANY_RTL]
 * @see [Bidi.requiresBidi]
 */
fun TextStyle.applyTextDirection(requiresBidi: Boolean) =
    this.applyTextDirection(
        textDirection = if (requiresBidi) TextDirection.Rtl else TextDirection.Ltr
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun Typography.applyTextDirection() =
    this.copy(
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
        bodyLargeEmphasized = bodyLargeEmphasized.applyTextDirection(),
        bodyMediumEmphasized = bodyMediumEmphasized.applyTextDirection(),
        bodySmallEmphasized = bodySmallEmphasized.applyTextDirection(),
        displayLargeEmphasized = displayLargeEmphasized.applyTextDirection(),
        displayMediumEmphasized = displayMediumEmphasized.applyTextDirection(),
        displaySmallEmphasized = displaySmallEmphasized.applyTextDirection(),
        headlineLargeEmphasized = headlineLargeEmphasized.applyTextDirection(),
        headlineMediumEmphasized = headlineMediumEmphasized.applyTextDirection(),
        headlineSmallEmphasized = headlineSmallEmphasized.applyTextDirection(),
        titleLargeEmphasized = titleLargeEmphasized.applyTextDirection(),
        titleMediumEmphasized = titleMediumEmphasized.applyTextDirection(),
        titleSmallEmphasized = titleSmallEmphasized.applyTextDirection(),
        labelLargeEmphasized = labelLargeEmphasized.applyTextDirection(),
        labelMediumEmphasized = labelMediumEmphasized.applyTextDirection(),
        labelSmallEmphasized = labelSmallEmphasized.applyTextDirection(),
    )
