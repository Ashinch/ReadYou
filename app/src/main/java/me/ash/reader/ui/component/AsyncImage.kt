package me.ash.reader.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
) {
    val context = LocalContext.current

    coil.compose.AsyncImage(
        modifier = modifier,
        model = ImageRequest
            .Builder(context)
            .data(data)
            .crossfade(true)
            .error(R.drawable.ic_launcher_foreground)
            .scale(scale)
            .precision(precision)
            .size(size)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        imageLoader = context.imageLoader,
    )
}