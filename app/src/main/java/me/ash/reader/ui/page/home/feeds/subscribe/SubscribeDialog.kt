package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.Dispatchers
import me.ash.reader.*
import me.ash.reader.R
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.widget.Dialog
import java.io.InputStream

@OptIn(ExperimentalPagerApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun SubscribeDialog(
    modifier: Modifier = Modifier,
    viewModel: SubscribeViewModel = hiltViewModel(),
    openInputStreamCallback: (InputStream) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                openInputStreamCallback(inputStream)
            }
        }
    }
    val viewState = viewModel.viewState.collectAsStateValue()
    val groupsState =
        viewState.groups.collectAsState(initial = emptyList(), context = Dispatchers.IO)
    var dialogHeight by remember { mutableStateOf(300.dp) }
    val readYouString = stringResource(R.string.read_you)
    val defaultString = stringResource(R.string.defaults)
    LaunchedEffect(viewState.visible) {
        if (viewState.visible) {
            val defaultGroupId = context.dataStore
                .get(DataStoreKeys.CurrentAccountId)!!
                .spacerDollar(readYouString + defaultString)
            viewModel.dispatch(SubscribeViewAction.SelectedGroup(defaultGroupId))
            viewModel.dispatch(SubscribeViewAction.Init)
        } else {
            viewModel.dispatch(SubscribeViewAction.Reset)
            viewState.pagerState.scrollToPage(0)
        }
    }

    LaunchedEffect(viewState.pagerState.currentPage) {
        focusManager.clearFocus()
        when (viewState.pagerState.currentPage) {
            0 -> dialogHeight = 300.dp
            1 -> dialogHeight = Dp.Unspecified
        }
    }

    Dialog(
        modifier = Modifier
            .padding(horizontal = 44.dp)
            .height(dialogHeight),
        visible = viewState.visible,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            focusManager.clearFocus()
            viewModel.dispatch(SubscribeViewAction.Hide)
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = stringResource(R.string.subscribe),
            )
        },
        title = {
            Text(
                when (viewState.pagerState.currentPage) {
                    0 -> viewState.title
                    else -> viewState.feed?.name ?: stringResource(R.string.unknown)
                }
            )
        },
        text = {
            SubscribeViewPager(
                readOnly = viewState.lockLinkInput,
                inputLink = viewState.linkContent,
                errorMessage = viewState.errorMessage,
                onLinkValueChange = {
                    viewModel.dispatch(SubscribeViewAction.InputLink(it))
                },
                onSearchKeyboardAction = {
                    viewModel.dispatch(SubscribeViewAction.Search(scope))
                },
                link = viewState.linkContent,
                groups = groupsState.value,
                selectedAllowNotificationPreset = viewState.allowNotificationPreset,
                selectedParseFullContentPreset = viewState.parseFullContentPreset,
                selectedGroupId = viewState.selectedGroupId,
                newGroupContent = viewState.newGroupContent,
                onNewGroupValueChange = {
                    viewModel.dispatch(SubscribeViewAction.InputNewGroup(it))
                },
                newGroupSelected = viewState.newGroupSelected,
                changeNewGroupSelected = {
                    viewModel.dispatch(SubscribeViewAction.SelectedNewGroup(it))
                },
                pagerState = viewState.pagerState,
                allowNotificationPresetOnClick = {
                    viewModel.dispatch(SubscribeViewAction.ChangeAllowNotificationPreset)
                },
                parseFullContentPresetOnClick = {
                    viewModel.dispatch(SubscribeViewAction.ChangeParseFullContentPreset)
                },
                groupOnClick = {
                    viewModel.dispatch(SubscribeViewAction.SelectedGroup(it))
                },
                onResultKeyboardAction = {
                    viewModel.dispatch(SubscribeViewAction.Subscribe)
                }
            )
        },
        confirmButton = {
            when (viewState.pagerState.currentPage) {
                0 -> {
                    TextButton(
                        enabled = viewState.linkContent.isNotEmpty(),
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.dispatch(SubscribeViewAction.Search(scope))
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.search),
                            color = if (viewState.linkContent.isNotEmpty()) {
                                Color.Unspecified
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
                1 -> {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.dispatch(SubscribeViewAction.Subscribe)
                        }
                    ) {
                        Text(stringResource(R.string.subscribe))
                    }
                }
            }
        },
        dismissButton = {
            when (viewState.pagerState.currentPage) {
                0 -> {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            launcher.launch("*/*")
                            viewModel.dispatch(SubscribeViewAction.Hide)
                        }
                    ) {
                        Text(text = stringResource(R.string.import_from_opml))
                    }
                }
                1 -> {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.dispatch(SubscribeViewAction.Hide)
                        }
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            }
        },
    )
}