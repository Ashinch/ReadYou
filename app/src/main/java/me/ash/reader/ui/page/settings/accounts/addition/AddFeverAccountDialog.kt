package me.ash.reader.ui.page.settings.accounts.addition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import me.ash.reader.data.model.account.security.FeverSecurityKey
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYOutlineTextField
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun AddFeverAccountDialog(
    navController: NavHostController,
    viewModel: AdditionViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState = viewModel.additionUiState.collectAsStateValue()

    var serverUrl by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    RYDialog(
        modifier = Modifier.padding(horizontal = 44.dp),
        visible = uiState.addFeverAccountDialogVisible,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            focusManager.clearFocus()
            // subscribeViewModel.hideDrawer()
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = stringResource(R.string.fever),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.fever),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                RYOutlineTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = stringResource(R.string.server_url),
                    placeholder = "https://demo.freshrss.org/api/fever.php",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Spacer(modifier = Modifier.height(10.dp))
                RYOutlineTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = stringResource(R.string.username),
                    placeholder = "demo",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(modifier = Modifier.height(10.dp))
                RYOutlineTextField(
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true,
                    label = stringResource(R.string.password),
                    placeholder = "demodemo",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        },
        confirmButton = {
            TextButton(
                enabled = serverUrl.isNotBlank() && username.isNotEmpty() && password.isNotEmpty(),
                onClick = {
                    focusManager.clearFocus()
                    accountViewModel.addAccount(Account(
                        type = AccountType.Fever,
                        name = context.getString(R.string.fever),
                        securityKey = FeverSecurityKey(
                            serverUrl = serverUrl
                                .takeIf { !it.endsWith("/") }
                                ?: serverUrl.trimEnd('/'),
                            username = username,
                            password = password,
                        ).toString(),
                    )) {
                        if (it == null) {
                            context.showToast("Not valid credentials")
                        } else {
                            viewModel.hideAddFeverAccountDialog()
                            navController.popBackStack()
                            navController.navigate("${RouteName.ACCOUNT_DETAILS}/${it.id}") {
                                launchSingleTop = true
                            }
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
                    viewModel.hideAddFeverAccountDialog()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
