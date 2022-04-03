package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.ClipboardTextField
import me.ash.reader.ui.component.Dialog
import me.ash.reader.ui.component.TextFieldDialog
import me.ash.reader.ui.ext.collectAsStateValue

@OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SubscribeDialog(
    modifier: Modifier = Modifier,
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val viewState = subscribeViewModel.viewState.collectAsStateValue()
    val groupsState = viewState.groups.collectAsState(initial = emptyList())
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                subscribeViewModel.dispatch(SubscribeViewAction.ImportFromInputStream(inputStream))
            }
        }
    }

    LaunchedEffect(viewState.visible) {
        if (viewState.visible) {
            subscribeViewModel.dispatch(SubscribeViewAction.Init)
        } else {
            subscribeViewModel.dispatch(SubscribeViewAction.Reset)
            subscribeViewModel.dispatch(SubscribeViewAction.SwitchPage(true))
        }
    }

    Dialog(
        modifier = Modifier.padding(horizontal = 44.dp),
        visible = viewState.visible,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            focusManager.clearFocus()
            subscribeViewModel.dispatch(SubscribeViewAction.Hide)
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = stringResource(R.string.subscribe),
            )
        },
        title = {
            Text(
                text = if (viewState.isSearchPage) {
                    viewState.title
                } else {
                    viewState.feed?.name ?: stringResource(R.string.unknown)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            AnimatedContent(
                targetState = viewState.isSearchPage,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                }
            ) { targetExpanded ->
                if (targetExpanded) {
                    ClipboardTextField(
                        readOnly = viewState.lockLinkInput,
                        value = viewState.linkContent,
                        onValueChange = {
                            subscribeViewModel.dispatch(SubscribeViewAction.InputLink(it))
                        },
                        placeholder = stringResource(R.string.feed_or_site_url),
                        errorText = viewState.errorMessage,
                        imeAction = ImeAction.Search,
                        focusManager = focusManager,
                        onConfirm = {
                            subscribeViewModel.dispatch(SubscribeViewAction.Search)
                        },
                    )
                } else {
                    ResultView(
                        link = viewState.linkContent,
                        groups = groupsState.value,
                        selectedAllowNotificationPreset = viewState.allowNotificationPreset,
                        selectedParseFullContentPreset = viewState.parseFullContentPreset,
                        selectedGroupId = viewState.selectedGroupId,
                        allowNotificationPresetOnClick = {
                            subscribeViewModel.dispatch(SubscribeViewAction.ChangeAllowNotificationPreset)
                        },
                        parseFullContentPresetOnClick = {
                            subscribeViewModel.dispatch(SubscribeViewAction.ChangeParseFullContentPreset)
                        },
                        onGroupClick = {
                            subscribeViewModel.dispatch(SubscribeViewAction.SelectedGroup(it))
                        },
                        onAddNewGroup = {
                            subscribeViewModel.dispatch(SubscribeViewAction.ShowNewGroupDialog)
                        },
                    )
                }
            }
        },
        confirmButton = {
            if (viewState.isSearchPage) {
                TextButton(
                    enabled = viewState.linkContent.isNotBlank()
                            && viewState.title != stringResource(R.string.searching),
                    onClick = {
                        focusManager.clearFocus()
                        subscribeViewModel.dispatch(SubscribeViewAction.Search)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.search),
                        color = if (viewState.linkContent.isNotBlank()) {
                            Color.Unspecified
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        }
                    )
                }
            } else {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        subscribeViewModel.dispatch(SubscribeViewAction.Subscribe)
                    }
                ) {
                    Text(stringResource(R.string.subscribe))
                }
            }
        },
        dismissButton = {
            if (viewState.isSearchPage) {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        launcher.launch("*/*")
                        subscribeViewModel.dispatch(SubscribeViewAction.Hide)
                    }
                ) {
                    Text(text = stringResource(R.string.import_from_opml))
                }
            } else {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        subscribeViewModel.dispatch(SubscribeViewAction.Hide)
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        },
    )

    TextFieldDialog(
        visible = viewState.newGroupDialogVisible,
        title = stringResource(R.string.create_new_group),
        icon = Icons.Outlined.CreateNewFolder,
        value = viewState.newGroupContent,
        placeholder = stringResource(R.string.name),
        onValueChange = {
            subscribeViewModel.dispatch(SubscribeViewAction.InputNewGroup(it))
        },
        onDismissRequest = {
            subscribeViewModel.dispatch(SubscribeViewAction.HideNewGroupDialog)
        },
        onConfirm = {
            subscribeViewModel.dispatch(SubscribeViewAction.AddNewGroup)
        }
    )
}