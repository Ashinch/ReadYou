package me.ash.reader.ui.page.settings.accounts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.ash.reader.R
import me.ash.reader.data.model.preference.*
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AccountDetailsPage(
    navController: NavHostController = rememberAnimatedNavController(),
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val uiState = viewModel.accountUiState.collectAsStateValue()
    val context = LocalContext.current
    val syncInterval = LocalSyncInterval.current
    val syncOnStart = LocalSyncOnStart.current
    val syncOnlyOnWiFi = LocalSyncOnlyOnWiFi.current
    val syncOnlyWhenCharging = LocalSyncOnlyWhenCharging.current
    val keepArchived = LocalKeepArchived.current
    val syncBlockList = LocalSyncBlockList.current

    var nameValue by remember { mutableStateOf(uiState.account?.name) }
    var nameDialogVisible by remember { mutableStateOf(false) }
    var syncIntervalDialogVisible by remember { mutableStateOf(false) }
    var keepArchivedDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchAccount()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { result ->
        viewModel.exportAsOPML { string ->
            result?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(string.toByteArray())
                }
            }
        }
    }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                navController.popBackStack()
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = uiState.account?.type?.toDesc(context) ?: "", desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.display),
                    )
                    SettingItem(
                        title = stringResource(R.string.name),
                        desc = uiState.account?.name ?: "",
                        onClick = { nameDialogVisible = true },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.synchronous),
                    )
                    SettingItem(
                        title = stringResource(R.string.sync_interval),
                        desc = syncInterval.toDesc(context),
                        onClick = { syncIntervalDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.sync_once_on_start),
                        onClick = { (!syncOnStart).put(context.currentAccountId, viewModel) },
                    ) {
                        RYSwitch(activated = syncOnStart.value) {
                            (!syncOnStart).put(context.currentAccountId, viewModel)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.only_on_wifi),
                        onClick = { (!syncOnlyOnWiFi).put(context.currentAccountId, viewModel) },
                    ) {
                        RYSwitch(activated = syncOnlyOnWiFi.value) {
                            (!syncOnlyOnWiFi).put(context.currentAccountId, viewModel)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.only_when_charging),
                        onClick = { (!syncOnlyWhenCharging).put(context.currentAccountId, viewModel) },
                    ) {
                        RYSwitch(activated = syncOnlyWhenCharging.value) {
                            (!syncOnlyWhenCharging).put(context.currentAccountId, viewModel)
                        }
                    }
                    SettingItem(
                        title = stringResource(R.string.keep_archived_articles),
                        desc = keepArchived.toDesc(context),
                        onClick = { keepArchivedDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.block_list),
                        onClick = {

                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.advanced),
                    )
                    SettingItem(
                        title = stringResource(R.string.export_as_opml),
                        onClick = {
                            launcher.launch("ReadYou.opml")
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.clear_all_articles),
                        onClick = { viewModel.showClearDialog() },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.delete_account),
                        onClick = { viewModel.showDeleteDialog() },
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

    RadioDialog(
        visible = syncIntervalDialogVisible,
        title = stringResource(R.string.sync_interval),
        options = SyncIntervalPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == syncInterval,
            ) {
                it.put(context.currentAccountId, viewModel)
            }
        }
    ) {
        syncIntervalDialogVisible = false
    }

    RadioDialog(
        visible = keepArchivedDialogVisible,
        title = stringResource(R.string.keep_archived_articles),
        options = KeepArchivedPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(context),
                selected = it == keepArchived,
            ) {
                it.put(context.currentAccountId, viewModel)
            }
        }
    ) {
        keepArchivedDialogVisible = false
    }

    RYDialog(
        visible = uiState.clearDialogVisible,
        onDismissRequest = {
            viewModel.hideClearDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = stringResource(R.string.clear_all_articles),
            )
        },
        title = {
            Text(text = stringResource(R.string.clear_all_articles))
        },
        text = {
            Text(text = stringResource(R.string.clear_all_articles_toast))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    uiState.account?.id?.let {
                        viewModel.clear(it) {
                            viewModel.hideClearDialog()
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.clear),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.hideClearDialog()
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )

    TextFieldDialog(
        visible = nameDialogVisible,
        title = stringResource(R.string.name),
        value = nameValue ?: "",
        placeholder = stringResource(R.string.value),
        onValueChange = {
            nameValue = it
        },
        onDismissRequest = {
            nameDialogVisible = false
        },
        onConfirm = {
            if (nameValue?.isNotBlank() == true) {
                viewModel.update(context.currentAccountId) {
                    name = nameValue ?: ""
                }
                nameDialogVisible = false
            }
        }
    )

    RYDialog(
        visible = uiState.deleteDialogVisible,
        onDismissRequest = {
            viewModel.hideDeleteDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.PersonOff,
                contentDescription = stringResource(R.string.delete_account),
            )
        },
        title = {
            Text(text = stringResource(R.string.delete_account))
        },
        text = {
            Text(text = stringResource(R.string.delete_account_toast))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    uiState.account?.id?.let {
                        viewModel.delete(it) {
                            viewModel.hideDeleteDialog()
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.delete),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.hideDeleteDialog()
                }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                )
            }
        },
    )
}
