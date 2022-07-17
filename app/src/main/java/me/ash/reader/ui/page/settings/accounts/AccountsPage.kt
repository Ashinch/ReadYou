package me.ash.reader.ui.page.settings.accounts

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.ash.reader.R
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AccountsPage(
    navController: NavHostController = rememberAnimatedNavController(),
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.accountUiState.collectAsStateValue()

    LaunchedEffect(Unit) {
        viewModel.fetchAccount()
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
                    DisplayText(text = stringResource(R.string.accounts), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.list),
                    )
                    SettingItem(
                        title = uiState.account?.name ?: "",
                        desc = uiState.account?.type?.toDesc(context) ?: "",
                        icon = uiState.account?.type?.toIcon()?.takeIf { it is ImageVector }?.let { it as ImageVector },
                        iconPainter = uiState.account?.type?.toIcon()?.takeIf { it is Painter }?.let { it as Painter },
                        onClick = {
                            navController.navigate(RouteName.ACCOUNT_DETAILS) {
                                launchSingleTop = true
                            }
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.more),
                    )
                    SettingItem(
                        title = stringResource(R.string.add_accounts),
                        desc = stringResource(R.string.add_accounts_desc),
                        icon = Icons.Outlined.PersonAdd,
                        onClick = {
                            navController.navigate(RouteName.ADD_ACCOUNTS) {
                                launchSingleTop = true
                            }
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
}

@Preview
@Composable
fun AccountsPreview() {
    AccountsPage()
}
