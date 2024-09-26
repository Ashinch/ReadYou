package me.ash.reader.ui.page.home.adaptive

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.parcelize.Parcelize
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.flow.FlowViewModel
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.page.home.reading.ReadingViewModel

@Parcelize
data class ArticleData(val id: String) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleListReaderPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    flowViewModel: FlowViewModel = hiltViewModel(),
    readingViewModel: ReadingViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ArticleData>()
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }
    val readerState = readingViewModel.readerStateStateFlow.collectAsStateValue()

    LaunchedEffect(navigator.currentDestination?.content) {
        navigator.currentDestination?.content?.id?.let { readingViewModel.initData(it) }
    }

    ListDetailPaneScaffold(
        modifier = modifier,
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                FlowPage(
                    homeViewModel = homeViewModel,
                    flowViewModel = flowViewModel,
                    readingArticleId = readerState.articleId,
                    onNavigateToFeeds = {
                        if (navController.previousBackStackEntry == null) {
                            navController.navigate(RouteName.FEEDS) {
                                launchSingleTop = true
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }, onOpenArticle = {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, ArticleData(it))
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                ReadingPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    readingViewModel = readingViewModel
                )
            }
        }
    )
}
