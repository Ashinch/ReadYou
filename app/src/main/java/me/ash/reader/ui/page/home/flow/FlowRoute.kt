package me.ash.reader.ui.page.home.flow

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.home.HomeUiState
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.theme.palette.core.LocalWidthWindowSizeClass

enum class FlowScreenType {
    FlowWithArticleDetails,
    Flow,
    ArticleDetails,
}

fun getFlowScreenType(isExpanded: Boolean, homeUiState: HomeUiState): FlowScreenType = when (isExpanded) {
    true -> {
        FlowScreenType.FlowWithArticleDetails
    }
    false -> {
        if (homeUiState.isArticleOpen) {
            FlowScreenType.ArticleDetails
        } else {
            FlowScreenType.Flow
        }
    }
}

@Composable
fun FlowRoute(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    flowViewModel: FlowViewModel = hiltViewModel(),
) {
    val homeUiState = homeViewModel.homeUiState.collectAsStateValue()
    val isExpandedScreen = LocalWidthWindowSizeClass.current == WindowWidthSizeClass.Expanded
    val flowScreenType = getFlowScreenType(isExpandedScreen, homeUiState)

    val selectArticle: (String) -> Unit = { articleId ->
        homeViewModel.selectArticle(articleId)
    }

    when (flowScreenType) {
        FlowScreenType.FlowWithArticleDetails -> {
            FlowWithArticleDetailsScreen(
                navController = navController,
                homeViewModel = homeViewModel,
                flowViewModel = flowViewModel,
                onArticleClick = selectArticle,
            )

            BackHandler {
                homeViewModel.backToFeed()
                navController.popBackStack()
            }
        }
        FlowScreenType.Flow -> {
            FlowPage(
                navController = navController,
                homeViewModel = homeViewModel,
                flowViewModel = flowViewModel,
                onArticleClick = selectArticle,
            )
        }
        FlowScreenType.ArticleDetails -> {
            ReadingPage(navController = navController, homeViewModel = homeViewModel)

            BackHandler {
                homeViewModel.backToFeed()
            }
        }
    }
}
