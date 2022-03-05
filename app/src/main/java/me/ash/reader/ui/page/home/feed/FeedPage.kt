package me.ash.reader.ui.page.home.feed

import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.flow.collect
import me.ash.reader.DateTimeExt
import me.ash.reader.DateTimeExt.toString
import me.ash.reader.data.constant.Filter
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupWithFeed
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.HomeViewAction
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.util.collectAsStateValue
import me.ash.reader.ui.widget.*
import java.io.InputStream


@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun FeedPage(
    navController: NavHostController,
    modifier: Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    filter: Filter,
    groupAndFeedOnClick: (currentGroup: Group?, currentFeed: Feed?) -> Unit = { _, _ -> },
) {
    val viewState = viewModel.viewState.collectAsStateValue()
    val syncState = RssRepository.syncState.collectAsStateValue()
    var addFeedDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(homeViewModel.filterState) {
        homeViewModel.filterState.collect { state ->
            viewModel.dispatch(
                FeedViewAction.FetchData(
                    isStarred = state.filter.let { it != Filter.All && it == Filter.Starred },
                    isUnread = state.filter.let { it != Filter.All && it == Filter.Unread },
                )
            )
        }
    }

    DisposableEffect(Unit) {
        viewModel.dispatch(
            FeedViewAction.FetchAccount()
        )
        onDispose { }
    }

    Box(
        modifier.fillMaxSize()
    ) {
        AddFeedDialog(
            visible = addFeedDialogVisible,
            hiddenFunction = { addFeedDialogVisible = false },
            openInputStreamCallback = {
                viewModel.dispatch(FeedViewAction.AddFromFile(it))
            },
        )
        TopTitleBox(
            title = viewState.account?.name ?: "未知账户",
            description = if (syncState.isSyncing) {
                "Syncing (${syncState.syncedCount}/${syncState.feedCount}) : ${syncState.currentFeedName}"
            } else {
                viewState.account?.updateAt?.toString(DateTimeExt.YYYY_MM_DD_HH_MM, true)
                    ?: "从未同步"
            },
            listState = viewState.listState,
            startOffset = Offset(20f, 80f),
            startHeight = 72f,
            startTitleFontSize = 38f,
            startDescriptionFontSize = 16f,
        ) {
            viewModel.dispatch(FeedViewAction.ScrollToItem(0))
        }
        Column {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(route = RouteName.SETTINGS)
                    }) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (syncState.isSyncing) return@IconButton
                        homeViewModel.dispatch(HomeViewAction.Sync())
                    }) {
                        Icon(
                            modifier = Modifier.size(26.dp),
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = {
                        addFeedDialogVisible = true
                    }) {
                        Icon(
                            modifier = Modifier.size(26.dp),
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Subscribe",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
            LazyColumn(
                state = viewState.listState,
                modifier = Modifier.weight(1f),
            ) {
                item {
                    Spacer(modifier = Modifier.height(114.dp))
                    BarButton(
                        barButtonType = ButtonType(
                            content = filter.title,
                            important = viewState.filterImportant
                        )
                    ) {
                        groupAndFeedOnClick(null, null)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    BarButton(
                        barButtonType = FirstExpandType(
                            content = "Feeds",
                            icon = Icons.Rounded.ExpandMore
                        )
                    ) {
                        viewModel.dispatch(FeedViewAction.ChangeGroupVisible)
                    }
                }
                itemsIndexed(viewState.groupWithFeedList) { index, groupWithFeed ->
                    GroupList(
                        modifier = modifier,
                        groupVisible = viewState.groupsVisible,
                        feedVisible = viewState.feedsVisible[index],
                        groupWithFeed = groupWithFeed,
                        groupAndFeedOnClick = groupAndFeedOnClick,
                        expandOnClick = {
                            viewModel.dispatch(FeedViewAction.ChangeFeedVisible(index))
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun AddFeedDialog(
    visible: Boolean,
    hiddenFunction: () -> Unit,
    openInputStreamCallback: (InputStream) -> Unit,
) {
    val context = LocalContext.current
    var inputString by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                openInputStreamCallback(inputStream)
            }
        }
    }
    val focusRequester = remember { FocusRequester() }
    val localFocusManager = LocalFocusManager.current
    val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        visible = visible,
        onDismissRequest = hiddenFunction,
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = "Subscribe",
            )
        },
        title = { Text("订阅") },
        text = {
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { if(it.isFocused) localSoftwareKeyboardController?.hide() }
                    .focusable(),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                ),
                value = inputString,
                onValueChange = {
                    inputString = it
                },
                placeholder = {
                    Text(
                        text = "订阅源或站点链接",
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    )
                },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = "Paste",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        hiddenFunction()
                    }
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
        },
        confirmButton = {
            TextButton(
                enabled = inputString.isNotEmpty(),
                onClick = {
                    hiddenFunction()
                }
            ) {
                Text(
                    text = "搜索",
                    color = if (inputString.isNotEmpty()) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    launcher.launch("*/*")
                    hiddenFunction()
                }
            ) {
                Text("导入OPML文件")
            }
        },
    )
}

@ExperimentalAnimationApi
@Composable
private fun ColumnScope.GroupList(
    modifier: Modifier = Modifier,
    groupVisible: Boolean,
    feedVisible: Boolean,
    groupWithFeed: GroupWithFeed,
    groupAndFeedOnClick: (currentGroup: Group?, currentFeed: Feed?) -> Unit = { _, _ -> },
    expandOnClick: () -> Unit
) {
    AnimatedVisibility(
        visible = groupVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = modifier) {
            BarButton(
                barButtonType = SecondExpandType(
                    content = groupWithFeed.group.name,
                    icon = Icons.Rounded.ExpandMore,
                    important = groupWithFeed.group.important ?: 0,
                ),
                iconOnClickListener = expandOnClick
            ) {
                groupAndFeedOnClick(groupWithFeed.group, null)
            }
            FeedList(
                visible = feedVisible,
                feeds = groupWithFeed.feeds,
                onClick = { currentFeed ->
                    groupAndFeedOnClick(null, currentFeed)
                }
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun ColumnScope.FeedList(
    visible: Boolean,
    feeds: List<Feed>,
    onClick: (currentFeed: Feed?) -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            feeds.forEach { feed ->
                Log.i("RLog", "FeedList: ${feed.icon}")
                BarButton(
                    barButtonType = ItemType(
//                        icon = feed.icon ?: "",
                        icon = if (feed.icon == null) {
                            null
                        } else {
                            BitmapPainter(
                                BitmapFactory.decodeByteArray(
                                    feed.icon,
                                    0,
                                    feed.icon!!.size
                                ).asImageBitmap()
                            )
                        },
                        content = feed.name,
                        important = feed.important ?: 0
                    )
                ) {
                    onClick(feed)
                }
            }
        }
    }
}