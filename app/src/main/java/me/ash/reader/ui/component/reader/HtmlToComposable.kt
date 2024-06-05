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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.size.Precision
import coil.size.Size
import coil.size.pxOrElse
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalReadingImageMaximize
import me.ash.reader.ui.component.base.RYAsyncImage
import me.ash.reader.ui.ext.requiresBidi
import me.ash.reader.ui.theme.applyTextDirection
import org.jsoup.Jsoup
import org.jsoup.helper.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.roundToInt

fun LazyListScope.htmlFormattedText(
    inputStream: InputStream,
    subheadUpperCase: Boolean = false,
    baseUrl: String,
    @DrawableRes imagePlaceholder: Int,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
    onLinkClick: (String) -> Unit,
) {
    Jsoup.parse(inputStream, null, baseUrl)
        ?.body()
        ?.let { body ->
            formatBody(
                element = body,
                subheadUpperCase = subheadUpperCase,
                imagePlaceholder = imagePlaceholder,
                onImageClick = onImageClick,
                onLinkClick = onLinkClick,
                baseUrl = baseUrl,
            )
        }
}

private fun LazyListScope.formatBody(
    element: Element,
    subheadUpperCase: Boolean = false,
    @DrawableRes imagePlaceholder: Int,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val paragraph = paragraphBuilder.toAnnotatedString()
            val requiresBidi = paragraph.toString().requiresBidi()
            val textStyle = bodyStyle().applyTextDirection(requiresBidi = requiresBidi)

            // ClickableText prevents taps from deselecting selected text
            // So use regular Text if possible
            if (paragraph.getStringAnnotations("URL", 0, paragraph.length)
                    .isNotEmpty()
            ) {
                ClickableText(
                    text = paragraph,
                    style = textStyle,
                    modifier = Modifier
                        .padding(horizontal = textHorizontalPadding().dp)
                        .width(MAX_CONTENT_WIDTH.dp)
                ) { offset ->
                    paragraph.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()
                        ?.let {
                            onLinkClick(it.item)
                        }
                }
            } else {
                Text(
                    text = paragraph,
                    style = textStyle,
                    modifier = Modifier
                        .padding(horizontal = textHorizontalPadding().dp)
                        .width(MAX_CONTENT_WIDTH.dp)
                )
            }
        }
    }

    composer.appendTextChildren(
        element.childNodes(),
        subheadUpperCase = subheadUpperCase,
        lazyListScope = this,
        imagePlaceholder = imagePlaceholder,
        onImageClick = onImageClick,
        onLinkClick = onLinkClick,
        baseUrl = baseUrl,
    )

    composer.terminateCurrentText()
}

private fun LazyListScope.formatCodeBlock(
    element: Element,
    @DrawableRes imagePlaceholder: Int,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)?,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    val composer = TextComposer { paragraphBuilder ->
        item {
            val scrollState = rememberScrollState()
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = codeBlockBackground(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(horizontal = textHorizontalPadding().dp),
            ) {
                Box(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .horizontalScroll(
                            state = scrollState
                        )
                        .width(MAX_CONTENT_WIDTH.dp)
                ) {
                    Text(
                        text = paragraphBuilder.toAnnotatedString(),
                        style = codeBlockStyle(),
                        softWrap = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    composer.appendTextChildren(
        element.childNodes(), preFormatted = true,
        lazyListScope = this,
        imagePlaceholder = imagePlaceholder,
        onImageClick = onImageClick,
        onLinkClick = onLinkClick,
        baseUrl = baseUrl,
    )

    composer.terminateCurrentText()
}

private fun TextComposer.appendTextChildren(
    nodes: List<Node>,
    preFormatted: Boolean = false,
    subheadUpperCase: Boolean = false,
    lazyListScope: LazyListScope,
    @DrawableRes imagePlaceholder: Int,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)?,
    onLinkClick: (String) -> Unit,
    baseUrl: String,
) {
    var node = nodes.firstOrNull()
    while (node != null) {
        when (node) {
            is TextNode -> {
                if (preFormatted) {
                    append(node.wholeText)
                } else {
                    if (endsWithWhitespace) {
                        node.text().trimStart().let { trimmed ->
                            if (trimmed.isNotEmpty()) {
                                append(trimmed)
                            }
                        }
                    } else {
                        node.text().let { text ->
                            if (text.isNotEmpty()) {
                                append(text)
                            }
                        }
                    }
                }
            }

            is Element -> {
                val element = node
                when (element.tagName()) {
                    "p" -> {
                        // Readability4j inserts p-tags in divs for algorithmic purposes.
                        // They screw up formatting.
                        if (node.hasClass("readability-styled")) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        } else {
                            withParagraph {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onImageClick = onImageClick,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "br" -> append('\n')
                    "h1" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { h1Style().toSpanStyle() }
                            ) {
                                append(
                                    "\n${
                                        if (subheadUpperCase) element.text()
                                            .uppercase() else element.text()
                                    }"
                                )
                            }
                        }
                    }

                    "h2" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { h2Style().toSpanStyle() }
                            ) {
                                append(
                                    "\n${
                                        if (subheadUpperCase) element.text()
                                            .uppercase() else element.text()
                                    }"
                                )
                            }
                        }
                    }

                    "h3" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { h3Style().toSpanStyle() }
                            ) {
                                append(
                                    "\n${
                                        if (subheadUpperCase) element.text()
                                            .uppercase() else element.text()
                                    }"
                                )
                            }
                        }
                    }

                    "h4" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { h4Style().toSpanStyle() }
                            ) {
                                append(
                                    "\n${
                                        if (subheadUpperCase) element.text()
                                            .uppercase() else element.text()
                                    }"
                                )
                            }
                        }
                    }

                    "h5" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { h5Style().toSpanStyle() }
                            ) {
                                append(
                                    "\n${
                                        if (subheadUpperCase) element.text()
                                            .uppercase() else element.text()
                                    }"
                                )
                            }
                        }
                    }

                    "h6" -> {
                        withParagraph {
                            withComposableStyle(
                                style = { h6Style().toSpanStyle() }
                            ) {
                                append(
                                    "\n${
                                        if (subheadUpperCase) element.text()
                                            .uppercase() else element.text()
                                    }"
                                )
                            }
                        }
                    }

                    "strong", "b" -> {
                        withComposableStyle(
                            style = { boldStyle().toSpanStyle() }
                        ) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "i", "em", "cite", "dfn" -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "tt" -> {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "u" -> {
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "sup" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "sub" -> {
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "font" -> {
                        val fontFamily: FontFamily? = element.attr("face")?.asFontFamily()
                        withStyle(SpanStyle(fontFamily = fontFamily)) {
                            appendTextChildren(
                                element.childNodes(),
                                lazyListScope = lazyListScope,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        }
                    }

                    "pre" -> {
                        appendTextChildren(
                            element.childNodes(),
                            preFormatted = true,
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onImageClick = onImageClick,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                    }

                    "code" -> {
                        if (element.parent()?.tagName() == "pre") {
                            terminateCurrentText()
                            lazyListScope.formatCodeBlock(
                                element = element,
                                imagePlaceholder = imagePlaceholder,
                                onImageClick = onImageClick,
                                onLinkClick = onLinkClick,
                                baseUrl = baseUrl,
                            )
                        } else {
                            // inline code
                            withComposableStyle(
                                style = { codeInlineStyle() }
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    preFormatted = preFormatted,
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onImageClick = onImageClick,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "blockquote" -> {
                        withParagraph {
                            withStyle(
                                SpanStyle(
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Light,
                                )
                            ) {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onImageClick = onImageClick,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "a" -> {
                        withComposableStyle(
                            style = { linkTextStyle().toSpanStyle() }
                        ) {
                            withAnnotation("URL", element.attr("abs:href") ?: "") {
                                appendTextChildren(
                                    element.childNodes(),
                                    lazyListScope = lazyListScope,
                                    imagePlaceholder = imagePlaceholder,
                                    onImageClick = onImageClick,
                                    onLinkClick = onLinkClick,
                                    baseUrl = baseUrl,
                                )
                            }
                        }
                    }

                    "img" -> {
                        val imageCandidates = getImageSource(baseUrl, element)
                        if (imageCandidates.hasImage) {
                            val alt = element.attr("alt") ?: ""
                            appendImage(onLinkClick = onLinkClick) { onClick ->
                                lazyListScope.item {
//                                    val scale = remember { mutableStateOf(1f) }
                                    Column(
                                        modifier = Modifier
//                                            .padding(horizontal = horizontalPadding().dp)
                                            .width(MAX_CONTENT_WIDTH.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(textHorizontalPadding().dp))
                                        DisableSelection {
                                            BoxWithConstraints(
                                                modifier = Modifier
                                                    .clip(RectangleShape)
//                                                    .clickable(
//                                                        enabled = onClick != null
//                                                    ) {
//                                                        onClick?.invoke()
//                                                    }
                                                    .fillMaxWidth()
                                                // This makes scrolling a pain, find a way to solve that
//                                            .pointerInput("imgzoom") {
//                                                detectTransformGestures { centroid, pan, zoom, rotation ->
//                                                    val z = zoom * scale.value
//                                                    scale.value = when {
//                                                        z < 1f -> 1f
//                                                        z > 3f -> 3f
//                                                        else -> z
//                                                    }
//                                                }
//                                            }
                                            ) {
                                                val imageSize = maxImageSize()
                                                val imgUrl = imageCandidates.getBestImageForMaxSize(
                                                    pixelDensity = pixelDensity(),
                                                    maxSize = imageSize,
                                                )
                                                RYAsyncImage(
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .fillMaxWidth()
                                                        .padding(horizontal = imageHorizontalPadding().dp)
                                                        .clip(imageShape())
                                                        .run {
                                                            if (onImageClick != null) {
                                                                this.clickable {
                                                                    onImageClick(imgUrl, alt)
                                                                }
                                                            } else {
                                                                this
                                                            }
                                                        },
                                                    data = imgUrl,
                                                    contentDescription = alt,
                                                    size = imageSize,
                                                    precision = Precision.INEXACT,
                                                    contentScale = if (LocalReadingImageMaximize.current.value) ContentScale.FillWidth else ContentScale.Inside,
                                                )
                                            }
                                        }

                                        if (alt.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(textHorizontalPadding().dp / 2))

                                            Text(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = textHorizontalPadding().dp),
                                                text = alt,
                                                style = captionStyle(),
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(textHorizontalPadding().dp))
                                    }
                                }
                            }
                        }
                    }

                    "ul" -> {
                        element.children()
                            .filter { it.tagName() == "li" }
                            .forEach { listItem ->
                                withParagraph {
                                    // no break space
                                    append("  • ")
                                    appendTextChildren(
                                        listItem.childNodes(),
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        onImageClick = onImageClick,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                    }

                    "ol" -> {
                        element.children()
                            .filter { it.tagName() == "li" }
                            .forEachIndexed { i, listItem ->
                                withParagraph {
                                    // no break space
                                    append("${i + 1}. ")
                                    appendTextChildren(
                                        listItem.childNodes(),
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        onImageClick = onImageClick,
                                        baseUrl = baseUrl,
                                    )
                                }
                            }
                    }

                    "table" -> {
                        appendTable {
                            /*
                            In this order:
                            optionally a caption element (containing text children for instance),
                            followed by zero or more colgroup elements,
                            followed optionally by a thead element,
                            followed by either zero or more tbody elements
                            or one or more tr elements,
                            followed optionally by a tfoot element
                             */
                            element.children()
                                .filter { it.tagName() == "caption" }
                                .forEach {
                                    appendTextChildren(
                                        it.childNodes(),
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        onImageClick = onImageClick,
                                        baseUrl = baseUrl,
                                    )
                                    ensureDoubleNewline()
                                    terminateCurrentText()
                                }

                            element.children()
                                .filter { it.tagName() == "thead" || it.tagName() == "tbody" || it.tagName() == "tfoot" }
                                .flatMap {
                                    it.children()
                                        .filter { it.tagName() == "tr" }
                                }
                                .forEach { row ->
                                    appendTextChildren(
                                        row.childNodes(),
                                        lazyListScope = lazyListScope,
                                        imagePlaceholder = imagePlaceholder,
                                        onLinkClick = onLinkClick,
                                        onImageClick = onImageClick,
                                        baseUrl = baseUrl,
                                    )
                                    terminateCurrentText()
                                }

                            append("\n\n")
                        }
                    }

                    "iframe" -> {
                        val video: Video? = getVideo(element.attr("abs:src"))

                        if (video != null) {
                            appendImage(onLinkClick = onLinkClick) {
                                lazyListScope.item {
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = textHorizontalPadding().dp)
                                            .width(MAX_CONTENT_WIDTH.dp)
                                    ) {
                                        DisableSelection {
                                            BoxWithConstraints(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                RYAsyncImage(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = imageHorizontalPadding().dp)
                                                        .clip(imageShape())
                                                        .clickable {
                                                            onLinkClick(video.link)
                                                        },
                                                    data = video.imageUrl,
                                                    size = maxImageSize(),
                                                    contentDescription = stringResource(R.string.touch_to_play_video),
                                                    precision = Precision.INEXACT,
                                                    contentScale = ContentScale.FillWidth,
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(textHorizontalPadding().dp / 2))

                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = textHorizontalPadding().dp),
                                            text = stringResource(R.string.touch_to_play_video),
                                            style = captionStyle(),
                                        )

                                        Spacer(modifier = Modifier.height(textHorizontalPadding().dp))
                                    }
                                }
                            }
                        }
                    }

                    "video" -> {
                        // not implemented yet. remember to disable selection
                    }

                    else -> {
                        appendTextChildren(
                            nodes = element.childNodes(),
                            preFormatted = preFormatted,
                            subheadUpperCase = subheadUpperCase,
                            lazyListScope = lazyListScope,
                            imagePlaceholder = imagePlaceholder,
                            onImageClick = onImageClick,
                            onLinkClick = onLinkClick,
                            baseUrl = baseUrl,
                        )
                    }
                }
            }
        }

        node = node.nextSibling()
    }
}

private fun String.asFontFamily(): FontFamily? = when (this.lowercase()) {
    "monospace" -> FontFamily.Monospace
    "serif" -> FontFamily.Serif
    "sans-serif" -> FontFamily.SansSerif
    else -> null
}

@Preview
@Composable
private fun testIt() {
    val html = """
        <p>In Gimp you go to <em>Image</em> in the top menu bar and select <em>Mode</em> followed by <em>Indexed</em>. Now you see a popup where you can select the number of colors for a generated optimum palette.</p> <p>You&rsquo;ll have to experiment a little because it will depend on your image.</p> <p>I used this approach to shrink the size of the cover image in <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">the_zopfli post</a> from a 37KB (JPG) to just 15KB (PNG, all PNG sizes listed include Zopfli compression btw).</p> <h2 id="straight-jpg-to-png-conversion-124kb">Straight JPG to PNG conversion: 124KB</h2> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things.png" alt="PNG version RGB colors" /></p> <p>First off, I exported the JPG file as a PNG file. This PNG file had a whopping 124KB! Clearly there was some bloat being stored.</p> <h2 id="256-colors-40kb">256 colors: 40KB</h2> <p>Reducing from RGB to only 256 colors has no visible effect to my eyes.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_256.png" alt="256 colors" /></p> <h2 id="128-colors-34kb">128 colors: 34KB</h2> <p>Still no difference.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_128.png" alt="128 colors" /></p> <h2 id="64-colors-25kb">64 colors: 25KB</h2> <p>You can start to see some artifacting in the shadow behind the text.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_64.png" alt="64 colors" /></p> <h2 id="32-colors-15kb">32 colors: 15KB</h2> <p>In my opinion this is the sweet spot. The shadow artifacting is barely noticable but the size is significantly reduced.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_32.png" alt="32 colors" /></p> <h2 id="16-colors-11kb">16 colors: 11KB</h2> <p>Clear artifacting in the text shadow and the yellow (fire?) in the background has developed an outline.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_16.png" alt="16 colors" /></p> <h2 id="8-colors-7-3kb">8 colors: 7.3KB</h2> <p>The broom has shifted in color from a clear brown to almost grey. Text shadow is just a grey blob at this point. Even clearer outline developed on the yellow background.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_8.png" alt="8 colors" /></p> <h2 id="4-colors-4-3kb">4 colors: 4.3KB</h2> <p>Interestingly enough, I think 4 colors looks better than 8 colors. The outline in the background has disappeared because there&rsquo;s not enough color spectrum to render it. The broom is now black and filled areas tend to get a white separator to the outlines.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_4.png" alt="4 colors" /></p> <h2 id="2-colors-2-4kb">2 colors: 2.4KB</h2> <p>Well, at least the silhouette is well defined at this point I guess.</p> <p><img src="https://cowboyprogrammer.org/images/2017/10/zopfli_all_the_things_2.png" alt="2 colors" /></p> <hr/> <p>Other posts in the <b>Migrating from Ghost to Hugo</b> series:</p> <ul class="series"> <li>2016-10-21 &mdash; Reduce the size of images even further by reducing number of colors with Gimp </li> <li>2016-08-26 &mdash; <a href="https://cowboyprogrammer.org/2016/08/zopfli_all_the_things/">Compress all the images!</a> </li> <li>2016-07-25 &mdash; <a href="https://cowboyprogrammer.org/2016/07/migrating_from_ghost_to_hugo/">Migrating from Ghost to Hugo</a> </li> </ul>
    """.trimIndent()

    html.byteInputStream().use { stream ->
        LazyColumn {
            htmlFormattedText(
                inputStream = stream,
                baseUrl = "https://cowboyprogrammer.org",
                imagePlaceholder = R.drawable.ic_telegram,
                onLinkClick = {},
            )
        }
    }
}

@Composable
private fun pixelDensity() = with(LocalDensity.current) {
    density
}

@Composable
private fun BoxWithConstraintsScope.maxImageSize() = with(LocalDensity.current) {
    val maxWidthPx = maxWidth.toPx().roundToInt()

    Size(
        width = maxWidth.toPx().roundToInt().coerceAtLeast(1),
        height = maxHeight
            .toPx()
            .roundToInt()
            .coerceAtLeast(1)
            .coerceAtMost(10 * maxWidthPx),
    )
}

/**
 * Gets the url to the image in the <img> tag - could be from srcset or from src
 */
internal fun getImageSource(baseUrl: String, element: Element) = ImageCandidates(
    baseUrl = baseUrl,
    srcSet = element.attr("srcset") ?: "",
    absSrc = element.attr("abs:src") ?: "",
)

internal class ImageCandidates(
    val baseUrl: String,
    val srcSet: String,
    val absSrc: String,
) {

    val hasImage: Boolean = srcSet.isNotBlank() || absSrc.isNotBlank()

    /**
     * Might throw if hasImage returns false
     */
    fun getBestImageForMaxSize(maxSize: Size, pixelDensity: Float): String {
        val setCandidate = srcSet.splitToSequence(", ")
            .map { it.trim() }
            .map { it.split(SpaceRegex).take(2).map { x -> x.trim() } }
            .fold(100f to "") { acc, candidate ->
                val candidateSize = if (candidate.size == 1) {
                    // Assume it corresponds to 1x pixel density
                    1.0f / pixelDensity
                } else {
                    val descriptor = candidate.last()
                    when {
                        descriptor.endsWith("w", ignoreCase = true) -> {
                            descriptor.substringBefore("w").toFloat() / maxSize.width.pxOrElse { 1 }
                        }

                        descriptor.endsWith("x", ignoreCase = true) -> {
                            descriptor.substringBefore("x").toFloat() / pixelDensity
                        }

                        else -> {
                            return@fold acc
                        }
                    }
                }

                if (abs(candidateSize - 1.0f) < abs(acc.first - 1.0f)) {
                    candidateSize to candidate.first()
                } else {
                    acc
                }
            }
            .second

        return StringUtil.resolve(
            baseUrl,
            setCandidate.takeIf { it.isNotBlank() } ?: absSrc
        )
    }
}

private val SpaceRegex = Regex("\\s+")
