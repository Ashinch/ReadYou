package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import me.ash.reader.DataStoreKeys
import me.ash.reader.dataStore
import me.ash.reader.get
import me.ash.reader.spacerDollar
import me.ash.reader.ui.extension.collectAsStateValue
import me.ash.reader.ui.widget.Dialog
import java.io.InputStream

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SubscribeDialog(
    viewModel: SubscribeViewModel = hiltViewModel(),
    openInputStreamCallback: (InputStream) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                openInputStreamCallback(inputStream)
            }
        }
    }
    val viewState = viewModel.viewState.collectAsStateValue()
    val groupsState = viewState.groups.collectAsState(initial = emptyList())
    var height by remember { mutableStateOf(0) }

    LaunchedEffect(viewState.visible) {
        if (viewState.visible) {
            val defaultGroupId = context.dataStore
                .get(DataStoreKeys.CurrentAccountId)!!
                .spacerDollar("0")
            viewModel.dispatch(SubscribeViewAction.SelectedGroup(defaultGroupId))
            viewModel.dispatch(SubscribeViewAction.Init)
        } else {
            viewModel.dispatch(SubscribeViewAction.Reset)
            viewState.pagerState.scrollToPage(0)
        }
    }

    Dialog(
        visible = viewState.visible,
        onDismissRequest = {
            viewModel.dispatch(SubscribeViewAction.Hide)
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = "Subscribe",
            )
        },
        title = {
            Text(
                when (viewState.pagerState.currentPage) {
                    0 -> "订阅"
                    else -> viewState.feed?.name ?: "未知"
                }
            )
        },
        text = {
            SubscribeViewPager(
//                height = when (viewState.pagerState.currentPage) {
//                    0 -> 84.dp
//                    else -> Dp.Unspecified
//                },
                inputContent = viewState.inputContent,
                errorMessage = viewState.errorMessage,
                onValueChange = {
                    viewModel.dispatch(SubscribeViewAction.Input(it))
                },
                onSearchKeyboardAction = {
                    viewModel.dispatch(SubscribeViewAction.Search(scope))
                },
                link = viewState.inputContent,
                groups = groupsState.value,
                selectedNotificationPreset = viewState.notificationPreset,
                selectedFullContentParsePreset = viewState.fullContentParsePreset,
                selectedGroupId = viewState.selectedGroupId,
                pagerState = viewState.pagerState,
                notificationPresetOnClick = {
                    viewModel.dispatch(SubscribeViewAction.ChangeNotificationPreset)
                },
                fullContentParsePresetOnClick = {
                    viewModel.dispatch(SubscribeViewAction.ChangeFullContentParsePreset)
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
                        enabled = viewState.inputContent.isNotEmpty(),
                        onClick = {
                            viewModel.dispatch(SubscribeViewAction.Search(scope))
                        }
                    ) {
                        Text(
                            text = "搜索",
                            color = if (viewState.inputContent.isNotEmpty()) {
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
                            viewModel.dispatch(SubscribeViewAction.Subscribe)
                        }
                    ) {
                        Text("订阅")
                    }
                }
            }
        },
        dismissButton = {
            when (viewState.pagerState.currentPage) {
                0 -> {
                    TextButton(
                        onClick = {
                            launcher.launch("*/*")
                            viewModel.dispatch(SubscribeViewAction.Hide)
                        }
                    ) {
                        Text("导入OPML文件")
                    }
                }
                1 -> {
                    TextButton(
                        onClick = {
                            viewModel.dispatch(SubscribeViewAction.Hide)
                        }
                    ) {
                        Text("取消")
                    }
                }
            }
        },
    )
}