package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.ui.component.Dialog
import me.ash.reader.ui.ext.*

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
    val readYouString = stringResource(R.string.read_you)
    val defaultString = stringResource(R.string.defaults)

    LaunchedEffect(viewState.visible) {
        if (viewState.visible) {
            val defaultGroupId = context.dataStore
                .get(DataStoreKeys.CurrentAccountId)!!
                .spacerDollar(readYouString + defaultString)
            subscribeViewModel.dispatch(SubscribeViewAction.SelectedGroup(defaultGroupId))
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
                if (viewState.isSearchPage) {
                    viewState.title
                } else {
                    viewState.feed?.name ?: stringResource(R.string.unknown)
                }
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
                    SearchView(
                        readOnly = viewState.lockLinkInput,
                        inputLink = viewState.linkContent,
                        errorMessage = viewState.errorMessage,
                        onLinkValueChange = {
                            subscribeViewModel.dispatch(SubscribeViewAction.InputLink(it))
                        },
                        onKeyboardAction = {
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
                        newGroupContent = viewState.newGroupContent,
                        onNewGroupValueChange = {
                            subscribeViewModel.dispatch(SubscribeViewAction.InputNewGroup(it))
                        },
                        newGroupSelected = viewState.newGroupSelected,
                        changeNewGroupSelected = {
                            subscribeViewModel.dispatch(SubscribeViewAction.SelectedNewGroup(it))
                        },
                        allowNotificationPresetOnClick = {
                            subscribeViewModel.dispatch(SubscribeViewAction.ChangeAllowNotificationPreset)
                        },
                        parseFullContentPresetOnClick = {
                            subscribeViewModel.dispatch(SubscribeViewAction.ChangeParseFullContentPreset)
                        },
                        onGroupClick = {
                            subscribeViewModel.dispatch(SubscribeViewAction.SelectedGroup(it))
                        },
                        onKeyboardAction = {
                            subscribeViewModel.dispatch(SubscribeViewAction.Subscribe)
                        },
                    )
                }
            }
        },
        confirmButton = {
            if (viewState.isSearchPage) {
                TextButton(
                    enabled = viewState.linkContent.isNotEmpty()
                            && viewState.title != stringResource(R.string.searching),
                    onClick = {
                        focusManager.clearFocus()
                        subscribeViewModel.dispatch(SubscribeViewAction.Search)
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
}