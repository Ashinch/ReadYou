package me.ash.reader.ui.component.base

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import com.caverock.androidsvg.SVG

@Composable
fun Base64Image(
    modifier: Modifier = Modifier,
    base64Uri: String
) {
    val isSvg = base64Uri.startsWith("image/svg")

    if (isSvg) {
        Image(
            painter = base64ToPainter(base64Uri),
            modifier = modifier,
            contentDescription = null
        )
    } else {
        val bytes = base64ToBytes(base64Uri)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        Image(
            bitmap = bitmap.asImageBitmap(),
            modifier = modifier,
            contentDescription = null
        )
    }
}

private fun base64ToBytes(base64String: String): ByteArray {
    val base64Data = base64String.substringAfter("base64,")
    return Base64.decode(base64Data, Base64.DEFAULT)
}

@Composable
private fun base64ToPainter(base64Str: String): Painter {
    return remember(base64Str) {
        val svg = SVG.getFromString(String(base64ToBytes(base64Str)))
        object : Painter() {
            override val intrinsicSize: Size
                get() {
                    svg.let {
                        return Size(it.documentWidth, it.documentHeight)
                    }
                }

            override fun DrawScope.onDraw() {
                svg.let {
                    val canvas = drawContext.canvas.nativeCanvas
                    // see: https://code.google.com/archive/p/androidsvg/wikis/FAQ.wiki#my-document-has-a-viewbox-but-it-is-still-not-scaling
                    svg.setDocumentHeight("100%")
                    svg.setDocumentWidth("100%")
                    svg.renderToCanvas(canvas)
                }
            }
        }
    }
}
