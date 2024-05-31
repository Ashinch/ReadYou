package me.ash.reader.ui.page.settings.troubleshooting

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReportGmailerrorred
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.OpenLinkPreference
import me.ash.reader.ui.component.base.Banner
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.DateFormat
import me.ash.reader.ui.ext.MimeType
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.getCurrentVersion
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.ext.toString
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight
import java.util.Date

@Composable
fun TroubleshootingPage(
    navController: NavHostController,
    viewModel: TroubleshootingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState = viewModel.troubleshootingUiState.collectAsStateValue()
    var byteArray by remember { mutableStateOf(ByteArray(0)) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(MimeType.JSON)
    ) { result ->
        viewModel.exportPreferencesAsJSON(context) { byteArray ->
            result?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(byteArray)
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                byteArray = inputStream.readBytes()
                viewModel.tryImport(context, byteArray)
            }
        }
    }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                navController.popBackStack()
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.troubleshooting), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                    Banner(
                        title = stringResource(R.string.bug_report),
                        icon = Icons.Outlined.Info,
                        action = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.go_to),
                            )
                        },
                    ) {
                        context.openURL(
                            context.getString(R.string.issue_tracer_url),
                            OpenLinkPreference.AutoPreferCustomTabs
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.app_preferences),
                    )
                    SettingItem(
                        title = stringResource(R.string.import_from_json),
                        onClick = {
                            importLauncher.launch(arrayOf(MimeType.ANY))
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.export_as_json),
                        onClick = {
                            preferenceFileLauncher(context, exportLauncher)
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    RYDialog(
        visible = uiState.warningDialogVisible,
        onDismissRequest = { viewModel.hideWarningDialog() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.ReportGmailerrorred,
                contentDescription = stringResource(R.string.import_from_json),
            )
        },
        title = {
            Text(text = stringResource(R.string.import_from_json))
        },
        text = {
            Text(text = stringResource(R.string.invalid_json_file_warning))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.hideWarningDialog()
                    viewModel.importPreferencesFromJSON(context, byteArray)
                }
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideWarningDialog() }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

private fun preferenceFileLauncher(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Uri?>,
) {
    launcher.launch("Read-You-" +
            "${context.getCurrentVersion()}-settings-" +
            "${Date().toString(DateFormat.YYYY_MM_DD_DASH_HH_MM_SS_DASH)}.json")
}
