package me.ash.reader.ui.page.settings.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.data.preference.DarkThemePreference
import me.ash.reader.data.preference.LocalAmoledDarkTheme
import me.ash.reader.data.preference.LocalDarkTheme
import me.ash.reader.data.preference.not
import me.ash.reader.ui.component.DisplayText
import me.ash.reader.ui.component.FeedbackIconButton
import me.ash.reader.ui.component.Subtitle
import me.ash.reader.ui.component.Switch
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkTheme(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val darkTheme = LocalDarkTheme.current
    val amoledDarkTheme = LocalAmoledDarkTheme.current
    val scope = rememberCoroutineScope()

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
                    DisplayText(text = stringResource(R.string.dark_theme), desc = "")
                }
                item {
                    DarkThemePreference.values.map {
                        SettingItem(
                            title = it.getDesc(context),
                            onClick = {
                                it.put(context, scope)
                            },
                        ) {
                            RadioButton(selected = it == darkTheme, onClick = {
                                it.put(context, scope)
                            })
                        }
                    }
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = "其他",
                    )
                    SettingItem(
                        title = "Amoled 的深色主题",
                        onClick = {
                            (!amoledDarkTheme).put(context, scope)
                        },
                    ) {
                        Switch(activated = amoledDarkTheme.value) {
                            (!amoledDarkTheme).put(context, scope)
                        }
                    }
                }
            }
        }
    )
}
