package me.ash.reader.ui.page.common

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.ash.reader.ui.ext.animatedComposable
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.findActivity
import me.ash.reader.ui.ext.isFirstLaunch
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.read.ReadPage
import me.ash.reader.ui.page.settings.ColorAndStyle
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.page.settings.TipsAndSupport
import me.ash.reader.ui.page.startup.StartupPage
import me.ash.reader.ui.theme.AppTheme
import me.ash.reader.ui.theme.LocalUseDarkTheme

@OptIn(ExperimentalAnimationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeEntry(
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val viewState = homeViewModel.viewState.collectAsStateValue()
    val pagingItems = viewState.pagingData.collectAsLazyPagingItems()

    AppTheme {
        val context = LocalContext.current
        val useDarkTheme = LocalUseDarkTheme.current
        val navController = rememberAnimatedNavController()

        val intent by rememberSaveable { mutableStateOf(context.findActivity()?.intent) }
        var openArticleId by rememberSaveable {
            mutableStateOf(intent?.extras?.get(ExtraName.ARTICLE_ID)?.toString() ?: "")
        }.also {
            intent?.replaceExtras(null)
        }

        LaunchedEffect(openArticleId) {
            if (openArticleId.isNotEmpty()) {
                navController.navigate("${RouteName.READING}/${openArticleId}") {
                    popUpTo(RouteName.FEEDS)
                }
                openArticleId = ""
            }
        }

        rememberSystemUiController().run {
            setStatusBarColor(Color.Transparent, !useDarkTheme)
            setSystemBarsColor(Color.Transparent, !useDarkTheme)
            setNavigationBarColor(Color.Transparent, !useDarkTheme)
        }

        AnimatedNavHost(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            navController = navController,
            startDestination = if (context.isFirstLaunch) RouteName.STARTUP else RouteName.FEEDS,
        ) {
            animatedComposable(route = RouteName.STARTUP) {
                StartupPage(navController)
            }
            animatedComposable(route = RouteName.FEEDS) {
                FeedsPage(navController = navController, homeViewModel = homeViewModel)
            }
            animatedComposable(route = RouteName.FLOW) {
                FlowPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    pagingItems = pagingItems
                )
            }
            animatedComposable(route = "${RouteName.READING}/{articleId}") {
                ReadPage(navController = navController)
            }
            animatedComposable(route = RouteName.SETTINGS) {
                SettingsPage(navController)
            }
            animatedComposable(route = RouteName.COLOR_AND_STYLE) {
                ColorAndStyle(navController)
            }
            animatedComposable(route = RouteName.TIPS_AND_SUPPORT) {
                TipsAndSupport(navController)
            }
        }
    }
}