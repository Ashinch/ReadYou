package me.ash.reader.ui.page.home.feed.subscribe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import me.ash.reader.ui.widget.Dialog
import java.io.InputStream

@Composable
fun SubscribeDialog(
    visible: Boolean,
    hiddenFunction: () -> Unit,
    inputContent: String = "",
    onValueChange: (String) -> Unit = {},
    onKeyboardAction: () -> Unit = {},
    openInputStreamCallback: (InputStream) -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                openInputStreamCallback(inputStream)
            }
        }
    }

    Dialog(
        visible = visible,
        onDismissRequest = hiddenFunction,
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = "Subscribe",
            )
        },
        title = { Text("订阅") },
        text = {
            SubscribeViewPager(
                inputContent = inputContent,
                onValueChange = onValueChange,
                onKeyboardAction = onKeyboardAction,
            )
        },
        confirmButton = {
            TextButton(
                enabled = inputContent.isNotEmpty(),
                onClick = {
                    hiddenFunction()
                }
            ) {
                Text(
                    text = "搜索",
                    color = if (inputContent.isNotEmpty()) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    launcher.launch("*/*")
                    hiddenFunction()
                }
            ) {
                Text("导入OPML文件")
            }
        },
    )
}