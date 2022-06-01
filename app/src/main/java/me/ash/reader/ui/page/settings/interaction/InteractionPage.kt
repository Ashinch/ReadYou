package me.ash.reader.ui.page.settings.interaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.data.preference.InitialFilterPreference
import me.ash.reader.data.preference.InitialPagePreference
import me.ash.reader.data.preference.LocalInitialFilter
import me.ash.reader.data.preference.LocalInitialPage
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun InteractionPage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val initialPage = LocalInitialPage.current
    val initialFilter = LocalInitialFilter.current
    val scope = rememberCoroutineScope()
    var initialPageDialogVisible by remember { mutableStateOf(false) }
    var initialFilterDialogVisible by remember { mutableStateOf(false) }

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
                        desc = initialPage.getDesc(context),
                        onClick = {
                            initialPageDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(R.string.initial_filter),
                        desc = initialFilter.getDesc(context),
                        onClick = {
                            initialFilterDialogVisible = true
                        },
                    ) {}
                }
                item {
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    RadioDialog(
        visible = initialPageDialogVisible,
        title = stringResource(R.string.initial_page),
        options = InitialPagePreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = it == initialPage,
            ) {
                it.put(context, scope)
            }
        },
    ) {
        initialPageDialogVisible = false
    }

    RadioDialog(
        visible = initialFilterDialogVisible,
        title = stringResource(R.string.initial_filter),
        options = InitialFilterPreference.values.map {
            RadioDialogOption(
                text = it.getDesc(context),
                selected = it == initialFilter,
            ) {
                it.put(context, scope)
            }
        },
    ) {
        initialFilterDialogVisible = false
    }
}
