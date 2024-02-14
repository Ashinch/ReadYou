package me.ash.reader.ui.page.common

import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.LocalReadingDarkTheme
import me.ash.reader.ui.ext.*
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.page.settings.accounts.AccountDetailsPage
import me.ash.reader.ui.page.settings.accounts.AccountsPage
import me.ash.reader.ui.page.settings.accounts.AddAccountsPage
import me.ash.reader.ui.page.settings.color.ColorAndStylePage
import me.ash.reader.ui.page.settings.color.DarkThemePage
import me.ash.reader.ui.page.settings.color.feeds.FeedsPageStylePage
import me.ash.reader.ui.page.settings.color.flow.FlowPageStylePage
import me.ash.reader.ui.page.settings.color.reading.*
import me.ash.reader.ui.page.settings.interaction.InteractionPage
import me.ash.reader.ui.page.settings.languages.LanguagesPage
import me.ash.reader.ui.page.settings.tips.TipsAndSupportPage
import me.ash.reader.ui.page.startup.StartupPage
import me.ash.reader.ui.theme.AppTheme

@OptIn(ExperimentalAnimationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeEntry(
    homeViewModel: HomeViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var isReadingPage by rememberSaveable { mutableStateOf(false) }
    val filterUiState = homeViewModel.filterUiState.collectAsStateValue()
    val subscribeUiState = subscribeViewModel.subscribeUiState.collectAsStateValue()
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

        // This is finally
        navController.currentBackStackEntryFlow.collectLatest {
            Log.i("RLog", "currentBackStackEntry: ${navController.currentDestination?.route}")
            // Animation duration takes 310 ms
            delay(310L)
            isReadingPage =
                navController.currentDestination?.route == "${RouteName.READING}/{articleId}"
        }
    }

    DisposableEffect(subscribeUiState.shouldNavigateToFeedPage) {
        if (subscribeUiState.shouldNavigateToFeedPage) {
            if (navController.currentDestination?.route != RouteName.FEEDS) {
                navController.popBackStack(
                    route = RouteName.FEEDS,
                    inclusive = false,
                    saveState = true
                )
            }
        }
        onDispose {
            subscribeViewModel.onIntentConsumed()
        }
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

    val useDarkTheme = if (isReadingPage) {
        LocalReadingDarkTheme.current.isDarkTheme()
    } else {
        LocalDarkTheme.current.isDarkTheme()
    }

    AppTheme(
        useDarkTheme = if (isReadingPage) LocalReadingDarkTheme.current.isDarkTheme()
        else LocalDarkTheme.current.isDarkTheme()
    ) {

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
            forwardAndBackwardComposable(route = RouteName.STARTUP) {
                StartupPage(navController)
            }

            // Home
            forwardAndBackwardComposable(route = RouteName.FEEDS) {
                FeedsPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    subscribeViewModel = subscribeViewModel
                )
            }
            forwardAndBackwardComposable(route = RouteName.FLOW) {
                FlowPage(
                    navController = navController,
                    homeViewModel = homeViewModel,
                )
            }
            forwardAndBackwardComposable(route = "${RouteName.READING}/{articleId}") {
                ReadingPage(navController = navController, homeViewModel = homeViewModel)
            }

            // Settings
            forwardAndBackwardComposable(route = RouteName.SETTINGS) {
                SettingsPage(navController)
            }

            // Accounts
            forwardAndBackwardComposable(route = RouteName.ACCOUNTS) {
                AccountsPage(navController)
            }

            forwardAndBackwardComposable(route = "${RouteName.ACCOUNT_DETAILS}/{accountId}") {
                AccountDetailsPage(navController)
            }

            forwardAndBackwardComposable(route = RouteName.ADD_ACCOUNTS) {
                AddAccountsPage(navController)
            }

            // Color & Style
            forwardAndBackwardComposable(route = RouteName.COLOR_AND_STYLE) {
                ColorAndStylePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.DARK_THEME) {
                DarkThemePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.FEEDS_PAGE_STYLE) {
                FeedsPageStylePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.FLOW_PAGE_STYLE) {
                FlowPageStylePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.READING_PAGE_STYLE) {
                ReadingStylePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.READING_DARK_THEME) {
                ReadingDarkThemePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.READING_PAGE_TITLE) {
                ReadingTitlePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.READING_PAGE_TEXT) {
                ReadingTextPage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.READING_PAGE_IMAGE) {
                ReadingImagePage(navController)
            }
            forwardAndBackwardComposable(route = RouteName.READING_PAGE_VIDEO) {
                ReadingVideoPage(navController)
            }

            // Interaction
            forwardAndBackwardComposable(route = RouteName.INTERACTION) {
                InteractionPage(navController)
            }

            // Languages
            forwardAndBackwardComposable(route = RouteName.LANGUAGES) {
                LanguagesPage(navController = navController)
            }

            // Tips & Support
            forwardAndBackwardComposable(route = RouteName.TIPS_AND_SUPPORT) {
                TipsAndSupportPage(navController)
            }
        }
    }
}
