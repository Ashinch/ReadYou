package me.ash.reader.ui.page.home.flow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.preference.LocalFlowArticleListDateStickyHeader
import me.ash.reader.infrastructure.preference.LocalFlowArticleListFeedIcon
import me.ash.reader.infrastructure.preference.LocalFlowArticleListTonalElevation
import me.ash.reader.infrastructure.preference.LocalFlowFilterBarFilled
import me.ash.reader.infrastructure.preference.LocalFlowFilterBarPadding
import me.ash.reader.infrastructure.preference.LocalFlowFilterBarStyle
import me.ash.reader.infrastructure.preference.LocalFlowFilterBarTonalElevation
import me.ash.reader.infrastructure.preference.LocalFlowTopBarTonalElevation
import me.ash.reader.infrastructure.preference.LocalMarkAsReadOnScroll
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.LocalSharedContent
import me.ash.reader.infrastructure.preference.LocalSortUnreadArticles
import me.ash.reader.infrastructure.preference.SortUnreadArticlesPreference
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYExtensibleVisibility
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.motion.materialSharedAxisYIn
import me.ash.reader.ui.motion.materialSharedAxisYOut
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.theme.palette.LocalFixedColorRoles

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
)
@Composable
fun FlowPage(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    flowViewModel: FlowViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val articleListTonalElevation = LocalFlowArticleListTonalElevation.current
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListDateStickyHeader = LocalFlowArticleListDateStickyHeader.current
    val topBarTonalElevation = LocalFlowTopBarTonalElevation.current
    val filterBarStyle = LocalFlowFilterBarStyle.current
    val filterBarFilled = LocalFlowFilterBarFilled.current
    val filterBarPadding = LocalFlowFilterBarPadding.current
    val filterBarTonalElevation = LocalFlowFilterBarTonalElevation.current
    val sharedContent = LocalSharedContent.current
    val markAsReadOnScroll = LocalMarkAsReadOnScroll.current.value
    val context = LocalContext.current

    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current

    val flowUiState = flowViewModel.flowUiState.collectAsStateValue()
    val filterUiState = homeViewModel.filterStateFlow.collectAsStateValue()
    val pagingItems = homeViewModel.pagerFlow.collectAsStateValue().collectAsLazyPagingItems()
    val listState = rememberSaveable(filterUiState, saver = LazyListState.Saver) {
        LazyListState(0, 0)
    }

    val isTopBarElevated = topBarTonalElevation.value > 0
    val isScrolled by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex != 0 } }
    val topBarContainerColor by animateColorAsState(with(MaterialTheme.colorScheme) {
        if (isScrolled && isTopBarElevated) surfaceContainer else surface
    }, label = "")

    val titleText = when {
        filterUiState.group != null -> filterUiState.group.name
        filterUiState.feed != null -> filterUiState.feed.name
        else -> filterUiState.filter.toName()
    }

    if (markAsReadOnScroll) {
        LaunchedEffect(listState.isScrollInProgress) {
            if (!listState.isScrollInProgress) {
                val firstItemIndex = listState.firstVisibleItemIndex
                if (firstItemIndex < pagingItems.itemCount) for (index in 0 until firstItemIndex) {
                    val item = pagingItems.peek(index)
                    with(item) {
                        when (this) {
                            is ArticleFlowItem.Article -> {
                                homeViewModel.diffMapHolder.updateDiff(
                                    articleWithFeed = articleWithFeed, isUnread = false
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var markAsRead by remember { mutableStateOf(false) }
    var onSearch by rememberSaveable { mutableStateOf(false) }

    val owner = LocalLifecycleOwner.current

    val lastVisibleIndex = remember(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }.filterNotNull()
    }

    var showFab by remember(listState) { mutableStateOf(false) }

    val lastReadIndex = flowUiState.lastReadIndex

    LaunchedEffect(lastVisibleIndex, lastReadIndex) {
        if (lastReadIndex != null) {
            lastVisibleIndex.collect { index ->
                if (index < lastReadIndex) {
                    showFab = true
                } else {
                    showFab = false
                    flowViewModel.updateLastReadIndex(null)
                }
            }
        }
        lastVisibleIndex.collect {
            if (it in (pagingItems.itemCount - 25..pagingItems.itemCount - 1)) {
                pagingItems.get(it)
            }
        }
    }




    DisposableEffect(owner) {
        onDispose { homeViewModel.syncWorkLiveData.removeObservers(owner) }
    }

    val onToggleStarred: (ArticleWithFeed) -> Unit = remember {
        { article ->
            flowViewModel.updateStarredStatus(
                articleId = article.article.id,
                isStarred = !article.article.isStarred,
            )
        }
    }

    val onToggleRead: (ArticleWithFeed) -> Unit = remember {
        { articleWithFeed ->
            homeViewModel.diffMapHolder.updateDiff(articleWithFeed)
        }
    }

    val sortByEarliest =
        filterUiState.filter.isUnread() && LocalSortUnreadArticles.current == SortUnreadArticlesPreference.Earliest

    val onMarkAboveAsRead: ((ArticleWithFeed) -> Unit)? = remember(sortByEarliest) {
        {
            flowViewModel.markAsReadFromListByDate(
                date = it.article.date, isBefore = sortByEarliest, lazyPagingItems = pagingItems
            )
        }
    }

    val onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = remember(sortByEarliest) {
        {
            flowViewModel.markAsReadFromListByDate(
                date = it.article.date, isBefore = !sortByEarliest, lazyPagingItems = pagingItems
            )
        }
    }

    val onShare: ((ArticleWithFeed) -> Unit)? = remember {
        { articleWithFeed ->
            with(articleWithFeed.article) {
                sharedContent.share(context, title, link)
            }
        }
    }

    LaunchedEffect(onSearch) {
        if (!onSearch) {
            keyboardController?.hide()
            homeViewModel.inputSearchContent(null)
        }
    }

    BackHandler(onSearch) {
        onSearch = false
    }

    RYScaffold(containerTonalElevation = articleListTonalElevation.value.dp, topBar = {
        TopAppBar(
            modifier = Modifier.clickable(
                onClick = {
                    scope.launch {
                        if (listState.firstVisibleItemIndex != 0) {
                            listState.animateScrollToItem(0)
                        }
                    }
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }), title = {
                AnimatedVisibility(
                    isScrolled,
                    enter = materialSharedAxisYIn(initialOffsetY = { it / 4 }),
                    exit = materialSharedAxisYOut(targetOffsetY = { it / 4 })
                ) {
                    Text(
                        text = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    )
                }
            }, navigationIcon = {
                FeedbackIconButton(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                ) {
                    onSearch = false
                    if (navController.previousBackStackEntry == null) {
                        navController.navigate(RouteName.FEEDS) {
                            launchSingleTop = true
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            }, actions = {
                RYExtensibleVisibility(visible = !filterUiState.filter.isStarred()) {
                    FeedbackIconButton(
                        imageVector = Icons.Rounded.DoneAll,
                        contentDescription = stringResource(R.string.mark_all_as_read),
                        tint = if (markAsRead) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    ) {
                        scope.launch {
                            if (listState.firstVisibleItemIndex != 0) {
                                listState.animateScrollToItem(0)
                            }
                            markAsRead = !markAsRead
                            onSearch = false
                        }
                    }
                }
                FeedbackIconButton(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = if (onSearch) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ) {
                    scope.launch {
                        if (listState.firstVisibleItemIndex != 0) {
                            listState.animateScrollToItem(0)
                        }
                        onSearch = !onSearch
                        if (onSearch) {
                            delay(100)
                            focusRequester.requestFocus()
                        }
                    }
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = topBarContainerColor
            )
        )
    }, content = {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
        ) {
            item {
                DisplayText(
                    modifier = Modifier.padding(start = if (articleListFeedIcon.value) 30.dp else 0.dp),
                    text = titleText,
                    desc = "",
                )
                RYExtensibleVisibility(visible = markAsRead) {
                    Spacer(modifier = Modifier.height((56 + 24 + 10).dp))
                }
                MarkAsReadBar(
                    visible = markAsRead,
                    absoluteY = 180.dp,
                    onDismissRequest = {
                        markAsRead = false
                    },
                ) {
                    markAsRead = false
                    flowViewModel.updateReadStatus(
                        groupId = filterUiState.group?.id,
                        feedId = filterUiState.feed?.id,
                        articleId = null,
                        conditions = it,
                        isUnread = false
                    )
                }
                RYExtensibleVisibility(visible = onSearch) {
                    SearchBar(
                        value = filterUiState.searchContent ?: "", placeholder = when {
                            filterUiState.group != null -> stringResource(
                                R.string.search_for_in,
                                filterUiState.filter.toName(),
                                filterUiState.group.name
                            )

                            filterUiState.feed != null -> stringResource(
                                R.string.search_for_in,
                                filterUiState.filter.toName(),
                                filterUiState.feed.name
                            )

                            else -> stringResource(
                                R.string.search_for, filterUiState.filter.toName()
                            )
                        }, focusRequester = focusRequester, onValueChange = {
                            homeViewModel.inputSearchContent(it)
                        }, onClose = {
                            onSearch = false
                            homeViewModel.inputSearchContent(null)
                        })
                    Spacer(modifier = Modifier.height((56 + 24 + 10).dp))
                }
            }
            ArticleList(
                pagingItems = pagingItems,
                diffMap = homeViewModel.diffMapHolder.diffMap,
                isShowFeedIcon = articleListFeedIcon.value,
                isShowStickyHeader = articleListDateStickyHeader.value,
                articleListTonalElevation = articleListTonalElevation.value,
                isSwipeEnabled = { listState.isScrollInProgress },
                onClick = { articleWithFeed ->
                    if (articleWithFeed.feed.isBrowser) {
                        if (articleWithFeed.article.isUnread) {
                            onToggleRead(articleWithFeed)
                        }
                        context.openURL(
                            articleWithFeed.article.link, openLink, openLinkSpecificBrowser
                        )
                    } else {
                        navController.navigate("${RouteName.READING}/${articleWithFeed.article.id}") {
                            launchSingleTop = true
                        }
                    }
                },
                onToggleStarred = onToggleStarred,
                onToggleRead = onToggleRead,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
                onShare = onShare,
            )
            item {
                Spacer(modifier = Modifier.height(128.dp))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }, floatingActionButton = {
        AnimatedVisibility(
            visible = showFab,
            enter = scaleIn(transformOrigin = TransformOrigin(.5f, 1f)),
            exit = scaleOut(transformOrigin = TransformOrigin(.5f, 1f)) + fadeOut(),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        lastReadIndex?.let {
                            listState.animateScrollToItem(index = it)
                        }
                    }
                    flowViewModel.updateLastReadIndex(null)
                    showFab = false
                },
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.loweredElevation(),
                containerColor = LocalFixedColorRoles.current.primaryFixedDim,
                contentColor = LocalFixedColorRoles.current.onPrimaryFixedVariant
            ) {
                Icon(Icons.Rounded.ArrowDownward, null)
            }
        }
    }, floatingActionButtonPosition = FabPosition.Center, bottomBar = {
        FilterBar(
            modifier = with(sharedTransitionScope) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        "filterBar"
                    ), animatedVisibilityScope = animatedVisibilityScope
                )
            },
            filter = filterUiState.filter,
            filterBarStyle = filterBarStyle.value,
            filterBarFilled = filterBarFilled.value,
            filterBarPadding = filterBarPadding.dp,
            filterBarTonalElevation = filterBarTonalElevation.value.dp,
        ) {
            scope.launch {
                if (listState.firstVisibleItemIndex != 0) {
                    listState.animateScrollToItem(0)
                }
            }
            if (filterUiState.filter != it) {
                homeViewModel.changeFilter(filterUiState.copy(filter = it))
            }
        }
    })
}
