package me.ash.reader.ui.page.home.flow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.domain.data.PagerData
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
import me.ash.reader.infrastructure.preference.LocalSettings
import me.ash.reader.infrastructure.preference.LocalSharedContent
import me.ash.reader.infrastructure.preference.LocalSortUnreadArticles
import me.ash.reader.infrastructure.preference.SortUnreadArticlesPreference
import me.ash.reader.ui.component.FilterBar
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYExtensibleVisibility
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.motion.Direction
import me.ash.reader.ui.motion.SharedXAxisTransitionSlow
import me.ash.reader.ui.motion.SharedYAxisTransitionSlow
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.reading.PullToLoadDefaults
import me.ash.reader.ui.page.home.reading.PullToLoadDefaults.ContentOffsetMultiple
import me.ash.reader.ui.page.home.reading.PullToLoadState
import me.ash.reader.ui.page.home.reading.pullToLoad
import me.ash.reader.ui.page.home.reading.rememberPullToLoadState

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalMaterialApi::class,
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

    val settings = LocalSettings.current
    val pullToSwitchFeed = settings.pullToSwitchFeed

    val flowUiState = flowViewModel.flowUiState.collectAsStateValue()

    val pagerData: PagerData = flowUiState.pagerData

    val filterUiState = pagerData.filterState

    val listState = rememberSaveable(pagerData, saver = LazyListState.Saver) {
        LazyListState(0, 0)
    }

    val isTopBarElevated = topBarTonalElevation.value > 0
    val scrolledTopBarContainerColor = with(MaterialTheme.colorScheme) {
        if (isTopBarElevated) surfaceContainer else surface
    }

    val titleText = when {
        filterUiState.group != null -> filterUiState.group.name
        filterUiState.feed != null -> filterUiState.feed.name
        else -> filterUiState.filter.toName()
    }

    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var markAsRead by remember { mutableStateOf(false) }
    var onSearch by rememberSaveable { mutableStateOf(false) }

    var currentPullToLoadState: PullToLoadState? by remember { mutableStateOf(null) }

    val settleSpec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy)
    }

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
//        lastVisibleIndex.collect {
//            if (it in (pagingItems.itemCount - 25..pagingItems.itemCount - 1)) {
//                pagingItems.get(it)
//            }
//        }
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
                date = it.article.date, isBefore = sortByEarliest
            )
        }
    }

    val onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = remember(sortByEarliest) {
        {
            flowViewModel.markAsReadFromListByDate(
                date = it.article.date, isBefore = !sortByEarliest
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

    BackHandler {
        if (navController.previousBackStackEntry == null) {
            navController.navigate(RouteName.FEEDS) {
                launchSingleTop = true
            }
        } else {
            navController.popBackStack()
        }
    }

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    val isSyncing = flowViewModel.isSyncingFlow.collectAsStateValue()

    Box(modifier = Modifier.fillMaxSize()) {

        RYScaffold(containerTonalElevation = articleListTonalElevation.value.dp, topBar = {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme,
                typography = MaterialTheme.typography.copy(
                    headlineMedium = MaterialTheme.typography.displaySmall,
                    titleLarge = MaterialTheme.typography.titleLarge.merge(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            ) {
                LargeTopAppBar(
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
                        val textStyle = LocalTextStyle.current
                        val color = LocalContentColor.current
                        if (textStyle.fontSize.value > 18f) {
                            BasicText(
                                modifier = Modifier.padding(
                                    start = if (articleListFeedIcon.value) 34.dp else 8.dp,
                                    end = 24.dp
                                ),
                                text = titleText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = textStyle,
                                color = { color }
                            )
                        } else {
                            Text(
                                text = titleText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                    },
                    expandedHeight = 172.dp,
                    scrollBehavior = scrollBehavior, navigationIcon = {
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
                                if (markAsRead) {
                                    markAsRead = false
                                } else {
                                    scope.launch {
                                        if (listState.firstVisibleItemIndex != 0) {
                                            listState.animateScrollToItem(0)
                                        }
                                    }.invokeOnCompletion {
                                        markAsRead = true
                                        onSearch = false
                                    }
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
                            if (onSearch) {
                                onSearch = false
                            } else {
                                scope.launch {
                                    if (listState.firstVisibleItemIndex != 0) {
                                        listState.animateScrollToItem(0)
                                    }
                                }.invokeOnCompletion {
                                    scope.launch {
                                        onSearch = true
                                        markAsRead = false
                                        delay(100)
                                        focusRequester.requestFocus()
                                    }
                                }
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = scrolledTopBarContainerColor
                    )
                )
            }
        }, content = {

            RYExtensibleVisibility(modifier = Modifier.zIndex(1f), visible = onSearch) {
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
            }

            RYExtensibleVisibility(markAsRead) {
                MarkAsReadBar {
                    markAsRead = false
                    flowViewModel.updateReadStatus(
                        groupId = filterUiState.group?.id,
                        feedId = filterUiState.feed?.id,
                        articleId = null,
                        conditions = it,
                        isUnread = false
                    )
                }
            }
            AnimatedContent(
                targetState = flowUiState,
                contentKey = {
                    it.pagerData.filterState.copy(searchContent = null)
                },
                transitionSpec = {
                    val targetFilter = targetState.pagerData.filterState
                    val initialFilter = initialState.pagerData.filterState

                    if (targetFilter.filter.index > initialFilter.filter.index) {
                        SharedXAxisTransitionSlow(direction = Direction.Forward)
                    } else if (targetFilter.filter.index < initialFilter.filter.index) {
                        SharedXAxisTransitionSlow(direction = Direction.Backward)
                    } else if (
                        targetFilter.group != initialFilter.group || targetFilter.feed != initialFilter.feed
                    ) {
                        SharedYAxisTransitionSlow(direction = Direction.Forward)
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                }
            ) { flowUiState ->
                val pager = flowUiState.pagerData.pager
                val filterState = flowUiState.pagerData.filterState
                val pagingItems = pager.collectAsLazyPagingItems()

                if (markAsReadOnScroll && filterState.filter.isUnread()) {
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

                val listState = remember(pager) { listState }
                val loadAction = remember(pager, flowUiState) {
                    when {
                        flowUiState.nextFilterState != null -> LoadAction.NextFeed
                        filterState.filter.isUnread() && markAsReadOnScroll -> LoadAction.MarkAllAsRead
                        else -> null
                    }
                }

                val onLoadNext: (() -> Unit)? = when (loadAction) {
                    LoadAction.NextFeed -> flowViewModel::loadNextFeedOrGroup
                    LoadAction.MarkAllAsRead -> {
                        {
                            flowViewModel.markAllAsRead()
                            currentPullToLoadState?.animateDistanceTo(
                                targetValue = 0f,
                                animationSpec = settleSpec
                            )
                        }
                    }

                    else -> null
                }

                val onPullToSync: (() -> Unit)? = if (isSyncing) null else {
                    {
                        flowViewModel.sync()
                        currentPullToLoadState?.animateDistanceTo(
                            targetValue = 0f,
                            animationSpec = settleSpec
                        )
                    }
                }

                val pullToLoadState = rememberPullToLoadState(
                    key = pager,
                    onLoadNext = onLoadNext,
                    onLoadPrevious = onPullToSync,
                    loadThreshold = PullToLoadDefaults.loadThreshold(.15f)
                ).also { currentPullToLoadState = it }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .pullToLoad(
                                pullToLoadState,
                                enabled = pullToSwitchFeed.value,
                                contentOffsetY = { fraction ->
                                    if (fraction > 0f) {
                                        (fraction * ContentOffsetMultiple * 1.5f).dp.roundToPx()
                                    } else {
                                        (fraction * ContentOffsetMultiple * 2f).dp.roundToPx()
                                    }
                                },
                                onScroll = {
                                    if (it < -10f) {
                                        markAsRead = false
                                    }
                                }
                            )
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .fillMaxSize(),
                        state = listState,
                    ) {
                        ArticleList(
                            pagingItems = pagingItems,
                            diffMap = homeViewModel.diffMapHolder.diffMap,
                            isShowFeedIcon = articleListFeedIcon.value,
                            isShowStickyHeader = articleListDateStickyHeader.value,
                            articleListTonalElevation = articleListTonalElevation.value,
                            isSwipeEnabled = { listState.isScrollInProgress },
                            onClick = { articleWithFeed ->
                                if (articleWithFeed.feed.isBrowser) {
                                    homeViewModel.diffMapHolder.updateDiff(
                                        articleWithFeed = articleWithFeed,
                                        isUnread = false
                                    )
                                    context.openURL(
                                        articleWithFeed.article.link,
                                        openLink,
                                        openLinkSpecificBrowser
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

                    PullToLoadIndicator(
                        state = pullToLoadState,
                        loadAction = loadAction,
                        modifier = Modifier.padding(bottom = 36.dp)
                    )
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
                    containerColor = MaterialTheme.colorScheme.primaryFixedDim,
                    contentColor = MaterialTheme.colorScheme.onPrimaryFixedVariant
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

                if (filterUiState.filter != it) {
                    homeViewModel.changeFilter(filterUiState.copy(filter = it))
                } else {
                    scope.launch {
                        if (listState.firstVisibleItemIndex != 0) {
                            listState.animateScrollToItem(0)
                        }
                    }
                }
            }
        })
        currentPullToLoadState?.let {
            PullToSyncIndicator(
                pullToLoadState = it,
                isSyncing = isSyncing
            )
        }
    }
}
