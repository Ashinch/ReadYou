package me.ash.reader.ui.page.adaptive

import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.flow.FlowViewModel
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.page.home.reading.ReadingViewModel

@Parcelize data class ArticleData(val articleId: String, val listIndex: Int) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ArticleListReaderPage(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit,
    onNavigateToStylePage: () -> Unit,
    flowViewModel: FlowViewModel = hiltViewModel(),
    readingViewModel: ReadingViewModel = hiltViewModel(),
) {
    val scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val navigator =
        rememberListDetailPaneScaffoldNavigator<ArticleData>(
            scaffoldDirective = scaffoldDirective,
            isDestinationHistoryAware = false,
        )

    val scope = rememberCoroutineScope()

    val readerState = readingViewModel.readerStateStateFlow.collectAsStateValue()

    LaunchedEffect(readerState.articleId) {
        flowViewModel.updateReadingArticleId(readerState.articleId)
    }

    val backBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange

    val paneExpansionState =
        rememberPaneExpansionState(
            initialAnchoredIndex = 1,
            anchors =
                listOf(
                    PaneExpansionAnchor.Proportion(0f),
                    PaneExpansionAnchor.Offset.fromStart(
                        scaffoldDirective.defaultPanePreferredWidth
                    ),
                ),
        )

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        modifier = modifier,
        defaultBackBehavior = backBehavior,
        paneExpansionDragHandle = {
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier =
                    Modifier.paneExpansionDraggable(
                        it,
                        LocalMinimumInteractiveComponentSize.current,
                        interactionSource,
                    ),
                interactionSource = interactionSource,
            )
        },
        paneExpansionState = paneExpansionState,
        listPane = {
            AnimatedPane {
                BoxWithConstraints {
                    if (this.maxWidth > scaffoldDirective.defaultPanePreferredWidth / 2)
                        FlowPage(
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            homeViewModel = homeViewModel,
                            flowViewModel = flowViewModel,
                            onNavigateUp = onBack,
                            navigateToArticle = { id, index ->
                                scope
                                    .launch {
                                        navigator.navigateTo(
                                            pane = ListDetailPaneScaffoldRole.Detail,
                                            contentKey =
                                                ArticleData(articleId = id, listIndex = index),
                                        )
                                    }
                                    .invokeOnCompletion {
                                        readingViewModel.initData(articleId = id, listIndex = index)
                                    }
                            },
                        )
                }
            }
        },
        detailPane = {
            AnimatedPane {
                if (navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) {
                    ReadingPage(
                        readingViewModel = readingViewModel,
                        onBack = {
                            if (navigator.canNavigateBack(backBehavior)) {
                                scope.launch { navigator.navigateBack(backBehavior) }
                            } else {
                                onBack()
                            }
                        },
                        onNavigateToStylePage = onNavigateToStylePage,
                    )
                }
            }
        },
    )
}
