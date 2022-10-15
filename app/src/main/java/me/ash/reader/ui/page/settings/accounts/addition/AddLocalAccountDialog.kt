package me.ash.reader.ui.page.settings.accounts.addition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.ash.reader.R
import me.ash.reader.data.model.account.Account
import me.ash.reader.data.model.account.AccountType
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYOutlineTextField
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun AddLocalAccountDialog(
    navController: NavHostController,
    viewModel: AdditionViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState = viewModel.additionUiState.collectAsStateValue()

    var name by remember { mutableStateOf("") }

    RYDialog(
        modifier = Modifier.padding(horizontal = 44.dp),
        visible = uiState.addLocalAccountDialogVisible,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            focusManager.clearFocus()
            // subscribeViewModel.hideDrawer()
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = stringResource(R.string.local),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.local),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                RYOutlineTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.name),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.hideAddLocalAccountDialog()

                    accountViewModel.addAccount(Account(
                        type = AccountType.Local,
                        name = name,
                    )) {
                        navController.popBackStack()
                        navController.navigate("${RouteName.ACCOUNT_DETAILS}/${it.id}") {
                            launchSingleTop = true
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.hideAddLocalAccountDialog()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
