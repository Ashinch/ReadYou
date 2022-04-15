package me.ash.reader.ui.component

import android.graphics.drawable.PictureDrawable
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.caverock.androidsvg.SVG
import me.ash.reader.ui.svg.parseDynamicColor
import me.ash.reader.ui.theme.LocalUseDarkTheme
import me.ash.reader.ui.theme.palette.LocalTonalPalettes

@Composable
fun DynamicSVGImage(
    modifier: Modifier = Modifier,
    svgImageString: String,
    contentDescription: String,
) {
    val context = LocalContext.current
    val useDarkTheme = LocalUseDarkTheme.current
    val tonalPalettes = LocalTonalPalettes.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    val pic by remember(tonalPalettes, size) {
        mutableStateOf(
            PictureDrawable(
                SVG.getFromString(svgImageString.parseDynamicColor(tonalPalettes, useDarkTheme))
                    .renderToPicture(size.width, size.height)
            )
        )
    }

    Row(
        modifier = modifier
            .aspectRatio(1.38f)
            .onGloballyPositioned {
                if (it.size != IntSize.Zero) {
                    size = it.size
                }
            },
    ) {
        Crossfade(targetState = pic) {
            AsyncImage(
                contentDescription = contentDescription,
                model = ImageRequest.Builder(context)
                    .data(it)
                    .crossfade(true)
                    .build(),
                imageLoader = context.imageLoader,
            )
        }
    }
}