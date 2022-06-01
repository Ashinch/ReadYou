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

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle

class TextComposer(
    val paragraphEmitter: (AnnotatedParagraphStringBuilder) -> Unit
) {
    val spanStack: MutableList<Span> = mutableListOf()

    // The identity of this will change - do not reference it in blocks
    private var builder: AnnotatedParagraphStringBuilder = AnnotatedParagraphStringBuilder()

    fun terminateCurrentText() {
        if (builder.isEmpty()) {
            // Nothing to emit, and nothing to reset
            return
        }

        paragraphEmitter(builder)

        builder = AnnotatedParagraphStringBuilder()

        for (span in spanStack) {
            when (span) {
                is SpanWithStyle -> builder.pushStyle(span.spanStyle)
                is SpanWithAnnotation -> builder.pushStringAnnotation(
                    tag = span.tag,
                    annotation = span.annotation
                )
                is SpanWithComposableStyle -> builder.pushComposableStyle(span.spanStyle)
            }
        }
    }

    val endsWithWhitespace: Boolean
        get() = builder.endsWithWhitespace

    fun ensureDoubleNewline() =
        builder.ensureDoubleNewline()

    fun append(text: String) =
        builder.append(text)

    fun append(char: Char) =
        builder.append(char)

    fun <R> appendTable(block: () -> R): R {
        builder.ensureDoubleNewline()
        terminateCurrentText()
        return block()
    }

    fun <R> appendImage(
        link: String? = null,
        onLinkClick: (String) -> Unit,
        block: (
            onClick: (() -> Unit)?
        ) -> R
    ): R {
        val url = link ?: findClosestLink()
        //builder.ensureDoubleNewline()
        terminateCurrentText()
        val onClick: (() -> Unit)? = if (url?.isNotBlank() == true) {
            {
                onLinkClick(url)
            }
        } else {
            null
        }
        return block(onClick)
    }

    fun pop(index: Int) =
        builder.pop(index)

    fun pushStyle(style: SpanStyle): Int =
        builder.pushStyle(style)

    fun pushStringAnnotation(tag: String, annotation: String): Int =
        builder.pushStringAnnotation(tag = tag, annotation = annotation)

    fun pushComposableStyle(style: @Composable () -> SpanStyle): Int =
        builder.pushComposableStyle(style)

    fun popComposableStyle(index: Int) =
        builder.popComposableStyle(index)

    private fun findClosestLink(): String? {
        for (span in spanStack.reversed()) {
            if (span is SpanWithAnnotation && span.tag == "URL") {
                return span.annotation
            }
        }
        return null
    }
}

inline fun <R : Any> TextComposer.withParagraph(
    crossinline block: TextComposer.() -> R
): R {
    ensureDoubleNewline()
    return block(this)
}

inline fun <R : Any> TextComposer.withStyle(
    style: SpanStyle,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithStyle(style))
    val index = pushStyle(style)
    return try {
        block()
    } finally {
        pop(index)
        spanStack.removeLast()
    }
}

inline fun <R : Any> TextComposer.withComposableStyle(
    noinline style: @Composable () -> SpanStyle,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithComposableStyle(style))
    val index = pushComposableStyle(style)
    return try {
        block()
    } finally {
        popComposableStyle(index)
        spanStack.removeLast()
    }
}

inline fun <R : Any> TextComposer.withAnnotation(
    tag: String,
    annotation: String,
    crossinline block: TextComposer.() -> R
): R {
    spanStack.add(SpanWithAnnotation(tag = tag, annotation = annotation))
    val index = pushStringAnnotation(tag = tag, annotation = annotation)
    return try {
        block()
    } finally {
        pop(index)
        spanStack.removeLast()
    }
}

sealed class Span

data class SpanWithStyle(
    val spanStyle: SpanStyle
) : Span()

data class SpanWithAnnotation(
    val tag: String,
    val annotation: String
) : Span()

data class SpanWithComposableStyle(
    val spanStyle: @Composable () -> SpanStyle
) : Span()
