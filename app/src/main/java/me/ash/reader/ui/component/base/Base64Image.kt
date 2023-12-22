package me.ash.reader.ui.component.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun Base64Image(
    modifier: Modifier = Modifier,
    base64Uri: String
) {
    val bitmap = base64ToBitmap(base64Uri)
    val imageBitmap = bitmap.asImageBitmap()

    Image(
        bitmap = imageBitmap,
        modifier = modifier,
        contentDescription = null
    )
}

fun base64ToBitmap(base64String: String): Bitmap {
    val base64Data = base64String.substringAfter("base64,")
    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}