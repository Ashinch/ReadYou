package me.ash.reader.ui.page.settings.tips

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.source.Download
import me.ash.reader.ui.component.Dialog
import me.ash.reader.ui.ext.*

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun UpdateDialog(
    updateViewModel: UpdateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState = updateViewModel.viewState.collectAsStateValue()
    val downloadState = viewState.downloadFlow.collectAsState(initial = Download.NotYet).value
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
    val newVersionSize = " " + context.dataStore.data
        .map { it[DataStoreKeys.NewVersionSize.key] ?: 0 }
        .map { it / 1024f / 1024f }
        .map { if (it > 0f) " ${String.format("%.2f", it)} MB" else "" }
        .collectAsState(initial = 0)
        .value

    val settings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        context.installLatestApk()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            context.installLatestApk()
        } else {
            settings.launch(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ),
            )
        }
    }

    Dialog(
        modifier = Modifier.heightIn(max = 400.dp),
        visible = viewState.updateDialogVisible,
        onDismissRequest = { updateViewModel.dispatch(UpdateViewAction.Hide) },
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
                    text = "$newVersionPublishDate$newVersionSize",
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
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(context.getString(R.string.github_link)),
                        )
                    )
                    // Disable automatic updates in F-Droid
//                    if (downloadState !is Download.Progress) {
//                        updateViewModel.dispatch(
//                            UpdateViewAction.DownloadUpdate(
//                                url = context.newVersionDownloadUrl,
//                            )
//                        )
//                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.update) + when (downloadState) {
                        is Download.NotYet -> ""
                        is Download.Progress -> " ${downloadState.percent}%"
                        is Download.Finished -> {
                            if (context.packageManager.canRequestPackageInstalls()) {
                                Log.i(
                                    "RLog",
                                    "Download.Finished: ${downloadState.file.absolutePath}"
                                )
                                context.installLatestApk()
                            } else {
                                launcher.launch(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                            }
                            ""
                        }
                    }
                )
            }
        },
        dismissButton = {
            if (downloadState !is Download.Progress) {
                TextButton(
                    onClick = {
                        scope.launch {
                            context.dataStore.put(DataStoreKeys.SkipVersionNumber, newVersionNumber)
                            updateViewModel.dispatch(UpdateViewAction.Hide)
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.skip_this_version))
                }
            }
        },
    )
}