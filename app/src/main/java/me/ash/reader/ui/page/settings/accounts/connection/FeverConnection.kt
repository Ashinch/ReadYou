package me.ash.reader.ui.page.settings.accounts.connection

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.security.FeverSecurityKey
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.mask
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@Composable
fun LazyItemScope.FeverConnection(
    account: Account,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val securityKey by remember {
        derivedStateOf { FeverSecurityKey(account.securityKey) }
    }

    var passwordMask by remember { mutableStateOf(securityKey.password?.mask()) }

    var serverUrlValue by remember { mutableStateOf(securityKey.serverUrl) }
    var usernameValue by remember { mutableStateOf(securityKey.username) }
    var passwordValue by remember { mutableStateOf(securityKey.password) }

    var serverUrlDialogVisible by remember { mutableStateOf(false) }
    var usernameDialogVisible by remember { mutableStateOf(false) }
    var passwordDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(securityKey.password) {
        passwordMask = securityKey.password?.mask()
    }

    SettingItem(
        title = stringResource(R.string.server_url),
        desc = securityKey.serverUrl ?: "",
        onClick = {
            serverUrlDialogVisible = true
        },
    ) {}
    SettingItem(
        title = stringResource(R.string.username),
        desc = securityKey.username ?: "",
        onClick = {
            usernameDialogVisible = true
        },
    ) {}
    SettingItem(
        title = stringResource(R.string.password),
        desc = passwordMask,
        onClick = {
            passwordDialogVisible = true
        },
    ) {}

    TextFieldDialog(
        visible = serverUrlDialogVisible,
        title = stringResource(R.string.server_url),
        value = serverUrlValue ?: "",
        placeholder = "https://demo.freshrss.org/api/fever.php",
        onValueChange = {
            serverUrlValue = it
        },
        onDismissRequest = {
            serverUrlDialogVisible = false
        },
        onConfirm = {
            if (securityKey.serverUrl?.isNotBlank() == true) {
                securityKey.serverUrl = serverUrlValue
                save(account, viewModel, securityKey)
                serverUrlDialogVisible = false
            }
        }
    )

    TextFieldDialog(
        visible = usernameDialogVisible,
        title = stringResource(R.string.username),
        value = usernameValue ?: "",
        placeholder = "demo",
        onValueChange = {
            usernameValue = it
        },
        onDismissRequest = {
            usernameDialogVisible = false
        },
        onConfirm = {
            if (securityKey.username?.isNotEmpty() == true) {
                securityKey.username = usernameValue
                save(account, viewModel, securityKey)
                usernameDialogVisible = false
            }
        }
    )

    TextFieldDialog(
        visible = passwordDialogVisible,
        title = stringResource(R.string.password),
        value = passwordValue ?: "",
        placeholder = "demodemo",
        isPassword = true,
        onValueChange = {
            passwordValue = it
        },
        onDismissRequest = {
            passwordDialogVisible = false
        },
        onConfirm = {
            if (securityKey.password?.isNotEmpty() == true) {
                securityKey.password = passwordValue
                save(account, viewModel, securityKey)
                passwordDialogVisible = false
            }
        }
    )
}

private fun save(
    account: Account,
    viewModel: AccountViewModel,
    securityKey: FeverSecurityKey,
) {
    account.id?.let {
        viewModel.update(it) {
            this.securityKey = securityKey.toString()
        }
    }
}
