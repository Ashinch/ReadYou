package me.ash.reader.ui.page.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.component.Dialog
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun UpdateDialog(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val newVersionNumber = context.dataStore.data
        .map { it[DataStoreKeys.NewVersionNumber.key] ?: "" }
        .collectAsState(initial = "")
        .value
    val newVersionPublishDate = context.dataStore.data
        .map { it[DataStoreKeys.NewVersionPublishDate.key] ?: "" }
        .collectAsState(initial = "")
        .value
    val newVersionLog = context.dataStore.data
        .map { it[DataStoreKeys.NewVersionLog.key] ?: "" }
        .collectAsState(initial = "")
        .value
    val newVersionSize = context.dataStore.data
        .map { it[DataStoreKeys.NewVersionSize.key] ?: 0 }
        .map { it / 1024f / 1024f }
        .map { if (it > 0f) " ${String.format("%.2f", it)} MB" else "" }
        .collectAsState(initial = 0)
        .value

    Dialog(
        modifier = modifier.heightIn(max = 400.dp),
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Update,
                contentDescription = stringResource(R.string.change_log),
            )
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(R.string.change_log))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = newVersionPublishDate,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        text = {
            SelectionContainer {
                Text(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    text = newVersionLog,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                }
            ) {
                Text(text = stringResource(R.string.update) + newVersionSize)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                scope.launch {
                    context.dataStore.put(DataStoreKeys.SkipVersionNumber, newVersionNumber)
                    onDismissRequest()
                }
            }) {
                Text(text = stringResource(R.string.skip_this_version))
            }
        },
    )
}