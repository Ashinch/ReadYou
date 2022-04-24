package me.ash.reader.ui.page.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.map
import me.ash.reader.R
import me.ash.reader.data.entity.toVersion
import me.ash.reader.ui.component.Banner
import me.ash.reader.ui.component.DisplayText
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.getCurrentVersion
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.theme.palette.onLight

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    var updateDialogVisible by remember { mutableStateOf(false) }
    val skipVersion = context.dataStore.data
        .map { it[DataStoreKeys.SkipVersionNumber.key] ?: "" }
        .collectAsState(initial = "")
        .value
        .toVersion()
    val latestVersion = context.dataStore.data
        .map { it[DataStoreKeys.NewVersionNumber.key] ?: "" }
        .collectAsState(initial = "")
        .value
        .toVersion()
    val currentVersion by remember { mutableStateOf(context.getCurrentVersion()) }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface),
                title = {},
                navigationIcon = {
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        navController.navigate(RouteName.HOME)
                    }
                },
                actions = {}
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.settings), desc = "")
                }
                item {
                    Box {
                        if (latestVersion.whetherNeedUpdate(currentVersion, skipVersion)) {
                            Banner(
                                modifier = Modifier.zIndex(1f),
                                title = stringResource(R.string.get_new_updates),
                                desc = stringResource(
                                    R.string.get_new_updates_desc,
                                    latestVersion.toString(),
                                ),
                                icon = Icons.Outlined.Lightbulb,
                                action = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.close),
                                    )
                                },
                            ) { updateDialogVisible = true }
                        }
                        Banner(
                            title = stringResource(R.string.in_coding),
                            desc = stringResource(R.string.coming_soon),
                            icon = Icons.Outlined.Lightbulb,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.accounts),
                        desc = stringResource(R.string.accounts_desc),
                        icon = Icons.Outlined.AccountCircle,
                        enable = false,
                    ) {}
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.color_and_style),
                        desc = stringResource(R.string.color_and_style_desc),
                        icon = Icons.Outlined.Palette,
                    ) {
                        navController.navigate(RouteName.COLOR_AND_STYLE)
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.interaction),
                        desc = stringResource(R.string.interaction_desc),
                        icon = Icons.Outlined.TouchApp,
                        enable = false,
                    ) {}
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.languages),
                        desc = stringResource(R.string.languages_desc),
                        icon = Icons.Outlined.Language,
                        enable = false,
                    ) {}
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.tips_and_support),
                        desc = stringResource(R.string.tips_and_support_desc),
                        icon = Icons.Outlined.TipsAndUpdates,
                    ) {
                        navController.navigate(RouteName.TIPS_AND_SUPPORT)
                    }
                }
            }
        }
    )

    UpdateDialog(
        visible = updateDialogVisible,
        onDismissRequest = { updateDialogVisible = false },
    )
}
