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
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.ash.reader.data.model.Filter
import me.ash.reader.data.preference.LocalDarkTheme
import me.ash.reader.ui.ext.*
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.page.settings.color.ColorAndStylePage
import me.ash.reader.ui.page.settings.color.DarkThemePage
import me.ash.reader.ui.page.settings.color.feeds.FeedsPageStylePage
import me.ash.reader.ui.page.settings.color.flow.FlowPageStylePage
import me.ash.reader.ui.page.settings.interaction.InteractionPage
import me.ash.reader.ui.page.settings.languages.LanguagesPage
import me.ash.reader.ui.page.settings.tips.TipsAndSupportPage
import me.ash.reader.ui.page.startup.StartupPage
import me.ash.reader.ui.theme.AppTheme

@OptIn(ExperimentalAnimationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeEntry(
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val filterUiState = homeViewModel.filterUiState.collectAsStateValue()
    val navController = rememberAnimatedNavController()

    val intent by rememberSaveable { mutableStateOf(context.findActivity()?.intent) }
    var openArticleId by rememberSaveable {
        mutableStateOf(intent?.extras?.get(ExtraName.ARTICLE_ID)?.toString() ?: "")
    }.also {
        intent?.replaceExtras(null)
    }

    LaunchedEffect(Unit) {
        when (context.initialPage) {
            1 -> {
                navController.navigate(RouteName.FLOW) {
                    launchSingleTop = true
                }
            }
            // Other initial pages
        }

        homeViewModel.changeFilter(
            filterUiState.copy(
                filter = when (context.initialFilter) {
                    0 -> Filter.Starred
                    1 -> Filter.Unread
                    2 -> Filter.All
                    else -> Filter.All
                }
            )
        )
    }

    LaunchedEffect(openArticleId) {
        if (openArticleId.isNotEmpty()) {
            navController.navigate(RouteName.FLOW) {
                launchSingleTop = true
            }
            navController.navigate("${RouteName.READING}/${openArticleId}") {
                launchSingleTop = true
            }
            openArticleId = ""
        }
    }

    val useDarkTheme = LocalDarkTheme.current.isDarkTheme()

    AppTheme(useDarkTheme = useDarkTheme) {

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
            // Startup
            animatedComposable(route = RouteName.STARTUP) {
                StartupPage(navController)
            }

            // Home
            animatedComposable(route = RouteName.FEEDS) {
                FeedsPage(navController = navController, homeViewModel = homeViewModel)
            }
            animatedComposable(route = RouteName.FLOW) {
                FlowPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                )
            }
            animatedComposable(route = "${RouteName.READING}/{articleId}") {
                ReadingPage(navController = navController)
            }

            // Settings
            animatedComposable(route = RouteName.SETTINGS) {
                SettingsPage(navController)
            }

            // Color & Style
            animatedComposable(route = RouteName.COLOR_AND_STYLE) {
                ColorAndStylePage(navController)
            }
            animatedComposable(route = RouteName.DARK_THEME) {
                DarkThemePage(navController)
            }
            animatedComposable(route = RouteName.FEEDS_PAGE_STYLE) {
                FeedsPageStylePage(navController)
            }
            animatedComposable(route = RouteName.FLOW_PAGE_STYLE) {
                FlowPageStylePage(navController)
            }

            // Interaction
            animatedComposable(route = RouteName.INTERACTION) {
                InteractionPage(navController)
            }

            // Languages
            animatedComposable(route = RouteName.LANGUAGES) {
                LanguagesPage(navController = navController)
            }

            // Tips & Support
            animatedComposable(route = RouteName.TIPS_AND_SUPPORT) {
                TipsAndSupportPage(navController)
            }
        }
    }
}