package me.ash.reader.ui.page.settings.interaction

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.component.*
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Interaction(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var initialPageDialogVisible by remember { mutableStateOf(false) }
    var initialFilterDialogVisible by remember { mutableStateOf(false) }

    val initialPage = context.dataStore.data
        .map { it[DataStoreKeys.InitialPage.key] ?: 0 }
        .collectAsState(initial = 0).value

    val initialFilter = context.dataStore.data
        .map { it[DataStoreKeys.InitialFilter.key] ?: 2 }
        .collectAsState(initial = 2).value

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface
                ),
                title = {},
                navigationIcon = {
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) {
                        navController.popBackStack()
                    }
                },
                actions = {}
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.interaction), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.on_start),
                    )
                    SettingItem(
                        title = stringResource(R.string.initial_page),
                        desc = when (initialPage) {
                            0 -> stringResource(R.string.feeds_page)
                            1 -> stringResource(R.string.flow_page)
                            else -> ""
                        },
                        onClick = {
                            initialPageDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.initial_filter),
                        desc = when (initialFilter) {
                            0 -> stringResource(R.string.starred)
                            1 -> stringResource(R.string.unread)
                            2 -> stringResource(R.string.all)
                            else -> ""
                        },
                        onClick = {
                            initialFilterDialogVisible = true
                        },
                    ) {}
                }
            }
        }
    )

    RadioDialog(
        visible = initialPageDialogVisible,
        title = stringResource(R.string.initial_page),
        options = listOf(
            RadioDialogOption(
                text = stringResource(R.string.feeds_page),
                selected = initialPage == 0,
            ) {
                scope.launch {
                    context.dataStore.put(DataStoreKeys.InitialPage, 0)
                }
            },
            RadioDialogOption(
                text = stringResource(R.string.flow_page),
                selected = initialPage == 1,
            ) {
                scope.launch {
                    context.dataStore.put(DataStoreKeys.InitialPage, 1)
                }
            },
        ),
    ) {
        initialPageDialogVisible = false
    }

    RadioDialog(
        visible = initialFilterDialogVisible,
        title = stringResource(R.string.initial_filter),
        options = listOf(
            RadioDialogOption(
                text = stringResource(R.string.starred),
                selected = initialFilter == 0,
            ) {
                scope.launch {
                    context.dataStore.put(DataStoreKeys.InitialFilter, 0)
                }
            },
            RadioDialogOption(
                text = stringResource(R.string.unread),
                selected = initialFilter == 1,
            ) {
                scope.launch {
                    context.dataStore.put(DataStoreKeys.InitialFilter, 1)
                }
            },
            RadioDialogOption(
                text = stringResource(R.string.all),
                selected = initialFilter == 2,
            ) {
                scope.launch {
                    context.dataStore.put(DataStoreKeys.InitialFilter, 2)
                }
            },
        ),
    ) {
        initialFilterDialogVisible = false
    }
}
