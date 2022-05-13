package me.ash.reader.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import me.ash.reader.R

@Composable
fun AsyncImage(
    modifier: Modifier = Modifier,
    data: Any? = null,
    size: Size = Size.ORIGINAL,
    scale: Scale = Scale.FIT,
    precision: Precision = Precision.AUTOMATIC,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String = "",
    @DrawableRes placeholder: Int? = R.drawable.ic_hourglass_empty_black_24dp,
    @DrawableRes error: Int? = R.drawable.ic_broken_image_black_24dp,
) {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val placeholderPainterResource = placeholder?.run { painterResource(this) }
    val errorPainterResource = error?.run { painterResource(this) }
    val placeholderPainter by remember {
        mutableStateOf(
            placeholderPainterResource?.run {
                forwardingPainter(
                    painter = this,
                    colorFilter = ColorFilter.tint(color),
                    alpha = 0.1f,
                )
            }
        )
    }
    val errorPainter by remember {
        mutableStateOf(
            errorPainterResource?.run {
                forwardingPainter(
                    painter = this,
                    colorFilter = ColorFilter.tint(color),
                    alpha = 0.1f,
                )
            }
        )
    }

    coil.compose.AsyncImage(
        modifier = modifier,
        model = ImageRequest
            .Builder(context)
            .data(data)
            .crossfade(true)
            .scale(scale)
            .precision(precision)
            .size(size)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = context.imageLoader,
        placeholder = placeholderPainter,
        error = errorPainter,
    )
}

// From: https://gist.github.com/colinrtwhite/c2966e0b8584b4cdf0a5b05786b20ae1

/**
 * Create and return a new [Painter] that wraps [painter] with its [alpha], [colorFilter], or [onDraw] overwritten.
 */
fun forwardingPainter(
    painter: Painter,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    onDraw: DrawScope.(ForwardingDrawInfo) -> Unit = DefaultOnDraw,
): Painter = ForwardingPainter(painter, alpha, colorFilter, onDraw)

data class ForwardingDrawInfo(
    val painter: Painter,
    val alpha: Float,
    val colorFilter: ColorFilter?,
)

private class ForwardingPainter(
    private val painter: Painter,
    private var alpha: Float,
    private var colorFilter: ColorFilter?,
    private val onDraw: DrawScope.(ForwardingDrawInfo) -> Unit,
) : Painter() {

    private var info = newInfo()

    override val intrinsicSize get() = painter.intrinsicSize

    override fun applyAlpha(alpha: Float): Boolean {
        if (alpha == DefaultAlpha) {
            this.alpha = alpha
            this.info = newInfo()
        }
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        if (colorFilter == null) {
            this.colorFilter = colorFilter
            this.info = newInfo()
        }
        return true
    }

    override fun DrawScope.onDraw() = onDraw(info)

    private fun newInfo() = ForwardingDrawInfo(painter, alpha, colorFilter)
}

private val DefaultOnDraw: DrawScope.(ForwardingDrawInfo) -> Unit = { info ->
    with(info.painter) {
        draw(
            androidx.compose.ui.geometry.Size(size.width, size.height),
            info.alpha,
            info.colorFilter
        )
    }
}