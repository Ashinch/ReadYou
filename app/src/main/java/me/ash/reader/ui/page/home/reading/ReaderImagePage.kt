package me.ash.reader.ui.page.home.reading

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import coil.compose.rememberAsyncImagePainter

import me.ash.reader.R
import me.ash.reader.ui.ext.showToast
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

data class ImageData(val imageUrl: String = "", val altText: String = "")

@Composable
fun ReaderImageViewer(
    imageData: ImageData, onDownloadImage: (String) -> Unit, onDismissRequest: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            val view = LocalView.current
            val context = LocalContext.current

            val dialogWindowProvider = view.parent as? DialogWindowProvider
            dialogWindowProvider?.window?.setDimAmount(1f)

            val zoomableState = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f))

            val painter = rememberAsyncImagePainter(model = imageData.imageUrl)

            LaunchedEffect(painter.intrinsicSize) {
                zoomableState.setContentLocation(
                    ZoomableContentLocation.scaledInsideAndCenterAligned(painter.intrinsicSize)
                )
            }

            Image(
                painter = painter,
                contentDescription = imageData.altText,
                modifier = Modifier
                    .zoomable(state = zoomableState, clipToBounds = true)
                    .fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Inside
            )

            IconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_TAP)
                    onDismissRequest()
                }, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                ), modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.close),
                )
            }
            var expanded by remember { mutableStateOf(false) }
            IconButton(
                onClick = { expanded = true }, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                ), modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = stringResource(id = R.string.more),
                )
            }

            val launcher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
                    onResult = { result ->
                        if (result) {
                            onDownloadImage(imageData.imageUrl)
                        } else {
                            context.showToast(context.getString(R.string.permission_denied))
                        }
                    })

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text(text = stringResource(id = R.string.save)) },
                        onClick = {
                            val isStoragePermissionGranted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED

                            if (Build.VERSION.SDK_INT > 28 || isStoragePermissionGranted) {
                                onDownloadImage(imageData.imageUrl)
                            } else {
                                launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                            expanded = false
                        })
                }
            }

        }
    }
}