package me.ash.reader.ui.page.home.feeds.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.theme.palette.alwaysLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsTab(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    accounts: List<Account>,
    onAccountSwitch: (Account) -> Unit = {},
    onClickSettings: () -> Unit = {},
    onClickManage: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    val context = LocalContext.current

    RYDialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Outlined.People,
                contentDescription = stringResource(R.string.switch_account),
            )
        },
        title = {
            Text(text = stringResource(R.string.switch_account))
        },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            ) {
                accounts.forEach { account ->
                    Column(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                onAccountSwitch(account)
                            }
                            .padding(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    if (account.id == context.currentAccountId) {
                                        MaterialTheme.colorScheme.primaryContainer alwaysLight true
                                    } else {
                                        MaterialTheme.colorScheme.surfaceDim alwaysLight true
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            AccountTypeIcon(account = account)
                        }
                        Text(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .width(52.dp),
                            textAlign = TextAlign.Center,
                            text = account.name,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClickSettings) {
                Text(
                    text = stringResource(R.string.settings),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onClickManage) {
                Text(
                    text = stringResource(R.string.list),
                )
            }
        },
    )
}

@Composable
fun AccountTypeIcon(
    account: Account,
) {
    val icon = account.type.toIcon().takeIf { it is ImageVector }?.let { it as ImageVector }
    val iconPainter = account.type.toIcon().takeIf { it is Painter }?.let { it as Painter }
    if (icon != null) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = account.name,
            tint = MaterialTheme.colorScheme.onSurface alwaysLight true
        )
    } else {
        iconPainter?.let {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = it,
                contentDescription = account.name,
                tint = MaterialTheme.colorScheme.onSurface alwaysLight true
            )
        }
    }
}
