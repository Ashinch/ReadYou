package me.ash.reader.ui.page.adaptive

import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.ash.reader.ui.component.reader.ExpandedContentWidth
import me.ash.reader.ui.component.reader.LocalTextContentWidth
import me.ash.reader.ui.component.reader.MediumContentWidth
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.reading.ReadingPage
import timber.log.Timber

@Parcelize data class ArticleData(val articleId: String, val listIndex: Int? = null) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ArticleListReaderPage(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective,
    navigator: ThreePaneScaffoldNavigator<ArticleData>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: ArticleListReaderViewModel,
    onBack: () -> Unit,
    onNavigateToStylePage: () -> Unit,
) {

    val scope = rememberCoroutineScope()

    val backBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange

    val hiddenAnchor = remember(scaffoldDirective) { PaneExpansionAnchor.Offset.fromStart(0.dp) }

    val expandedAnchor =
        remember(scaffoldDirective) {
            PaneExpansionAnchor.Offset.fromStart(scaffoldDirective.defaultPanePreferredWidth)
        }

    val paneExpansionState =
        rememberPaneExpansionState(
            initialAnchoredIndex = 1,
            anchors = listOf(hiddenAnchor, expandedAnchor),
            anchoringAnimationSpec =
                spring(dampingRatio = 1f, stiffness = 380f, visibilityThreshold = 1f),
        )

    val isTwoPane =
        navigator.scaffoldValue.run {
            get(ListDetailPaneScaffoldRole.List) == PaneAdaptedValue.Expanded &&
                get(ListDetailPaneScaffoldRole.Detail) == PaneAdaptedValue.Expanded
        }

    val navigationAction =
        if (isTwoPane) {
            val currentAnchor = paneExpansionState.currentAnchor
            if (currentAnchor == null || currentAnchor == expandedAnchor) {
                NavigationAction.HideList
            } else {
                NavigationAction.ExpandList
            }
        } else {
            NavigationAction.Close
        }

    LaunchedEffect(isTwoPane) { Timber.d("isTwoPane: $isTwoPane") }

    val contentWidth =
        when (navigationAction) {
            NavigationAction.HideList,
            NavigationAction.Close -> MediumContentWidth
            NavigationAction.ExpandList -> ExpandedContentWidth
        }

    var listAlpha by rememberSaveable { mutableFloatStateOf(1f) }

    val animatedContentWidth by animateDpAsState(contentWidth)
    val animatedListAlpha by animateFloatAsState(listAlpha)

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        modifier = modifier,
        defaultBackBehavior = backBehavior,
        paneExpansionDragHandle = { Spacer(modifier = Modifier.width(2.dp)) },
        paneExpansionState = paneExpansionState,
        listPane = {
            AnimatedPane(
                enterTransition = motionDataProvider.calculateEnterTransition(paneRole),
                exitTransition = motionDataProvider.calculateExitTransition(paneRole),
            ) {
                Box(modifier = Modifier.alpha(animatedListAlpha)) {
                    FlowPage(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        viewModel = viewModel,
                        onNavigateUp = onBack,
                        navigateToArticle = { id, index ->
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ArticleData(articleId = id, listIndex = index),
                                )
                            }
                        },
                    )
                }
            }
        },
        detailPane = {
            AnimatedPane(
                enterTransition = motionDataProvider.calculateEnterTransition(paneRole),
                exitTransition = motionDataProvider.calculateExitTransition(paneRole),
            ) {
                val contentKey = navigator.currentDestination?.contentKey
                LaunchedEffect(contentKey) {
                    contentKey?.let {
                        viewModel.initData(articleId = it.articleId, listIndex = it.listIndex)
                    } ?: viewModel.clearData()
                }

                CompositionLocalProvider(LocalTextContentWidth provides animatedContentWidth) {
                    ReadingPage(
                        viewModel = viewModel,
                        navigationAction = navigationAction,
                        onLoadArticle = { id, index ->
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = ArticleData(articleId = id, listIndex = index),
                                )
                            }
                        },
                        onNavAction = {
                            when (it) {
                                NavigationAction.Close -> {
                                    if (navigator.canNavigateBack(backBehavior)) {
                                        scope.launch { navigator.navigateBack(backBehavior) }
                                    } else {
                                        onBack()
                                    }
                                }
                                NavigationAction.HideList -> {
                                    scope.launch {
                                        listAlpha = 0f
                                        paneExpansionState.animateTo(hiddenAnchor)
                                    }
                                }
                                NavigationAction.ExpandList -> {
                                    listAlpha = 1f
                                    scope.launch { paneExpansionState.animateTo(expandedAnchor) }
                                }
                            }
                        },
                        onNavigateToStylePage = onNavigateToStylePage,
                    )
                }
            }
        },
    )
}
