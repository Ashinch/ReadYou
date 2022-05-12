/*
 * Feeder: Android RSS reader app
 * https://gitlab.com/spacecowboy/Feeder
 *
 * Copyright (C) 2022  Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ash.reader.ui.component.reader

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.ext.alphaLN

const val PADDING_HORIZONTAL = 24.0
const val MAX_CONTENT_WIDTH = 840.0

@Composable
fun bodyForeground(): Color =
    MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun bodyStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        color = bodyForeground()
    )

@Composable
fun h1Style(): TextStyle =
    MaterialTheme.typography.displayMedium.copy(
        color = bodyForeground()
    )

@Composable
fun h2Style(): TextStyle =
    MaterialTheme.typography.displaySmall.copy(
        color = bodyForeground()
    )

@Composable
fun h3Style(): TextStyle =
    MaterialTheme.typography.headlineLarge.copy(
        color = bodyForeground()
    )

@Composable
fun h4Style(): TextStyle =
    MaterialTheme.typography.headlineMedium.copy(
        color = bodyForeground()
    )

@Composable
fun h5Style(): TextStyle =
    MaterialTheme.typography.headlineSmall.copy(
        color = bodyForeground()
    )

@Composable
fun h6Style(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(
        color = bodyForeground()
    )

@Composable
fun captionStyle(): TextStyle =
    MaterialTheme.typography.bodySmall.copy(
        color = bodyForeground().copy(alpha = 0.6f)
    )

@Composable
fun linkTextStyle(): TextStyle =
    TextStyle(
        color = MaterialTheme.colorScheme.secondary,
        textDecoration = TextDecoration.Underline
    )

@Composable
fun codeBlockStyle(): TextStyle =
    MaterialTheme.typography.titleSmall.merge(
        SpanStyle(
            color = bodyForeground(),
            fontFamily = FontFamily.Monospace
        )
    )

@Composable
fun codeBlockBackground(): Color =
    MaterialTheme.colorScheme.secondary.copy(alpha = (0.dp).alphaLN(weight = 3.2f))

@Composable
fun blockQuoteStyle(): SpanStyle =
    MaterialTheme.typography.titleSmall.toSpanStyle().merge(
        SpanStyle(
            fontWeight = FontWeight.Light
        )
    )

@Composable
fun codeInlineStyle(): SpanStyle =
    MaterialTheme.typography.titleSmall.toSpanStyle().copy(
        color = bodyForeground(),
        fontStyle = FontStyle.Italic,
        fontFamily = FontFamily.Monospace,
    )
