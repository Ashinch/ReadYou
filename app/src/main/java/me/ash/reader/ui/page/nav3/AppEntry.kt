package me.ash.reader.ui.page.nav3

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import me.ash.reader.ui.motion.materialSharedAxisXIn
import me.ash.reader.ui.motion.materialSharedAxisXOut
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.page.home.reading.ReadingViewModel
import me.ash.reader.ui.page.nav3.key.Route
import me.ash.reader.ui.page.startup.StartupPage

private const val INITIAL_OFFSET_FACTOR = 0.10f

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppEntry() {
    val backStack = remember { mutableStateListOf<Route>(Route.Feeds) }
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val onBack: () -> Unit = { backStack.removeLastOrNull() }

    SharedTransitionLayout {
        NavDisplay<Route>(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            backStack = backStack,
            entryDecorators =
                listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            transitionSpec = {
                materialSharedAxisXIn(
                    initialOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() }
                ) togetherWith
                    materialSharedAxisXOut(
                        targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }
                    )
            },
            popTransitionSpec = {
                materialSharedAxisXIn(
                    initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }
                ) togetherWith
                    materialSharedAxisXOut(targetOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() })
            },
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    Route.Feeds -> {
                        NavEntry(key) {
                            FeedsPage(
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                navigateToSettings = {},
                                navigationToFlow = { backStack.add(Route.Flow) },
                                navigateToAccountList = {},
                                navigateToAccountDetail = {},
                            )
                        }
                    }
                    Route.Flow -> {
                        NavEntry(key) {
                            FlowPage(
                                sharedTransitionScope = this@SharedTransitionLayout,
                                homeViewModel = homeViewModel,
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                onNavigateUp = { backStack.removeLastOrNull() },
                                navigateToArticle = { backStack.add(Route.Reading(it)) },
                            )
                        }
                    }
                    is Route.Reading -> {
                        NavEntry(key) {
                            val articleId = key.articleId

                            val readingViewModel: ReadingViewModel =
                                hiltViewModel<
                                    ReadingViewModel,
                                    ReadingViewModel.ReadingViewModelFactory,
                                > { factory ->
                                    factory.create(articleId.toString(), null)
                                }

                            ReadingPage(
                                readingViewModel = readingViewModel,
                                onBack = onBack,
                                onNavigateToStylePage = {},
                            )
                        }
                    }
                    Route.Startup -> {
                        NavEntry(key) {
                            StartupPage(onNavigateToFeeds = { backStack.add(Route.Feeds) })
                        }
                    }
                }
            },
        )
    }
}
