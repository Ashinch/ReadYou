package me.ash.reader.ui.page.home.read

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.R

@Composable
fun ReadPageTopBar(
    btnBackOnClickListener: () -> Unit = {},
) {
    val view = LocalView.current
    SmallTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                btnBackOnClickListener()
            }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }) {
                Icon(
//                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }) {
                Icon(
//                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}