package me.ash.reader.ui.page.home.reading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import me.ash.reader.R
import me.ash.reader.ui.component.base.RYAsyncImage
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

data class ImageData(val imageUrl: String = "", val altText: String = "")

@Composable
fun ReaderImageViewer(imageData: ImageData, onDismissRequest: () -> Unit = {}) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .background(Color.Black)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
            dialogWindowProvider?.window?.setDimAmount(1f)

            val zoomableState = rememberZoomableState().apply {
                contentAlignment = Alignment.Center
            }

            RYAsyncImage(
                data = imageData.imageUrl,
                contentDescription = imageData.altText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .zoomable(zoomableState)
                    .fillMaxSize(),
            )

            IconButton(
                onClick = onDismissRequest,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Gray.copy(alpha = 0.5f),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.close)
                )
            }
        }
    }
}