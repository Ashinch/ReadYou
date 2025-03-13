package me.ash.reader.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.infrastructure.preference.widget.WidgetPreferencesManager


class FeedIconLoader(private val context: Context) {

    suspend fun asyncLoadFeedIcon(
        feedName: String? = "",
        iconUrl: String?,
        size: Dp = 20.dp,
        widgetId: Int
    ): Bitmap? {
        val widgetPreferenceManager = WidgetPreferencesManager.getInstance(context)
        val (xPx, yPx) = dpToPixels(size)
        val bitmap = if (iconUrl.isNullOrEmpty()) {
            fontIcon(
                xPx,
                feedName ?: "",
                widgetPreferenceManager.primaryColor.get(widgetId),
                widgetPreferenceManager.onPrimaryColor.get(widgetId)
            )
        } else if ("^image/.*;base64,.*".toRegex().matches(iconUrl)) {
            // e.g. image/gif;base64,R0lGODlh...
            base64Image(
                base64Uri = iconUrl,
                xPx,
                yPx
            ) ?: fontIcon(
                xPx,
                feedName ?: "",
                widgetPreferenceManager.primaryColor.get(widgetId),
                widgetPreferenceManager.onPrimaryColor.get(widgetId)
            )
        } else {
            requestImage(url = iconUrl)?.toBitmap(height = xPx, width = yPx)
        }
        // Need to set fixed height image or get weird performance issues when scrolling
        return bitmap
    }

    private fun fontIcon(size: Int, feedName: String, bgColor: Int, textColor: Int): Bitmap {
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)

        // Paint for the circular background
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        // Paint for the letter
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor  // Dynamic text color
            textSize = size * 0.5f  // Adjust text size based on icon size
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        // Draw the circular background
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        // Calculate text position (centered)
        val textX = size / 2f
        val textY = (size / 2f - (textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(feedName.firstOrNull()?.uppercase() ?: "?", textX, textY, textPaint)

        return bitmap
    }

    private fun base64Image(base64Uri: String, sizeX: Int, sizeY: Int): Bitmap? {
        TODO()
    }

    private suspend fun requestImage(url: String): Drawable? {
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()
        val loader = ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
        val result = withContext(Dispatchers.IO) {
            loader.execute(request)
        }
        return result.drawable
    }

    private fun dpToPixels(xdp: Dp, ydp: Dp? = null): Pair<Int, Int> {
        var ydp = ydp ?: xdp
        val displayMetrics = context.resources.displayMetrics
        return Pair(
            (xdp * (displayMetrics.xdpi / 160)).value.toInt(),
            (ydp * (displayMetrics.ydpi / 160)).value.toInt()
        )
    }
}

