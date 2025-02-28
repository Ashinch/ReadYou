package me.ash.reader.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.R


class FeedIconLoader(private val context: Context) {

    suspend fun asyncLoadFeedIcon(
        feedName: String? = "",
        iconUrl: String?,
        size: Dp = 20.dp
    ): Bitmap? {
        val (xPx, yPx) = dpToPixels(size)
        val drawable = if (iconUrl.isNullOrEmpty()) {
            fontIcon(size, feedName ?: "")
        } else if ("^image/.*;base64,.*".toRegex().matches(iconUrl)) {
            // e.g. image/gif;base64,R0lGODlh...
            base64Image(
                base64Uri = iconUrl,
                onEmpty = { fontIcon(size, feedName ?: "") },
            )
        } else {
            requestImage(
                url = iconUrl,
                contentDescription = feedName ?: ""
            )
        }
        // Need to set fixed height image or get weird performance issues when scrolling
        return drawable?.toBitmap(height = xPx, width = yPx)
    }

    private fun fontIcon(size: Dp, feedName: String): Drawable {
        TODO()
    }

    private fun base64Image(base64Uri: String, onEmpty: () -> Drawable): Drawable {
        TODO()
    }

    private suspend fun requestImage(url: String, contentDescription: String): Drawable? {
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

