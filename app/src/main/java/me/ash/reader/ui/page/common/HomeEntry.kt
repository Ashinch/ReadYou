package me.ash.reader.ui.page.common

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import me.ash.reader.infrastructure.preference.InitialPagePreference
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.LocalSettings
import me.ash.reader.ui.ext.animatedComposable
import me.ash.reader.ui.ext.isFirstLaunch
import me.ash.reader.ui.page.home.HomeViewModel
import me.ash.reader.ui.page.home.feeds.FeedsPage
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.page.home.flow.FlowPage
import me.ash.reader.ui.page.home.flow.FlowViewModel
import me.ash.reader.ui.page.home.reading.ReadingPage
import me.ash.reader.ui.page.home.reading.ReadingViewModel
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.page.settings.accounts.AccountDetailsPage
import me.ash.reader.ui.page.settings.accounts.AccountsPage
import me.ash.reader.ui.page.settings.accounts.AddAccountsPage
import me.ash.reader.ui.page.settings.color.ColorAndStylePage
import me.ash.reader.ui.page.settings.color.DarkThemePage
import me.ash.reader.ui.page.settings.color.feeds.FeedsPageStylePage
import me.ash.reader.ui.page.settings.color.flow.FlowPageStylePage
import me.ash.reader.ui.page.settings.color.reading.BoldCharactersPage
import me.ash.reader.ui.page.settings.color.reading.ReadingImagePage
import me.ash.reader.ui.page.settings.color.reading.ReadingStylePage
import me.ash.reader.ui.page.settings.color.reading.ReadingTextPage
import me.ash.reader.ui.page.settings.color.reading.ReadingTitlePage
import me.ash.reader.ui.page.settings.color.reading.ReadingVideoPage
import me.ash.reader.ui.page.settings.interaction.InteractionPage
import me.ash.reader.ui.page.settings.languages.LanguagesPage
import me.ash.reader.ui.page.settings.tips.LicenseListPage
import me.ash.reader.ui.page.settings.tips.TipsAndSupportPage
import me.ash.reader.ui.page.settings.troubleshooting.TroubleshootingPage
import me.ash.reader.ui.page.startup.StartupPage
import me.ash.reader.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeEntry(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    subscribeViewModel: SubscribeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current


    val settings = LocalSettings.current
    val isFirstLaunch = remember { context.isFirstLaunch }
    val initialPage = remember { settings.initialPage }
    val startDestination =
        if (isFirstLaunch) RouteName.STARTUP else if (initialPage == InitialPagePreference.FlowPage) {
            RouteName.FLOW
        } else RouteName.FEEDS

    AppTheme(
        useDarkTheme = LocalDarkTheme.current.isDarkTheme()
    ) {
        SharedTransitionScope {
            NavHost(
                modifier = Modifier
                    .then(it)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                navController = navController,
                startDestination = startDestination,
            ) {
                // Startup
                animatedComposable(route = RouteName.STARTUP) {
                    StartupPage(navController)
                }

                // Home
                animatedComposable(route = RouteName.FEEDS) {
                    FeedsPage(
                        navController = navController,
                        sharedTransitionScope = this@SharedTransitionScope,
                        animatedVisibilityScope = this,
                        subscribeViewModel = subscribeViewModel
                    )
                }
                animatedComposable(route = RouteName.FLOW) { entry ->
                    val flowViewModel = hiltViewModel<FlowViewModel>()

                    LaunchedEffect(navController) {
                        val lastReadIndexFlow =
                            navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Int?>(
                                "articleIndex",
                                null
                            )
                        val index = lastReadIndexFlow?.first { it != null }
                        if (index != null) {
                            flowViewModel.updateLastReadIndex(index)
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.remove<Int>("articleIndex")
                        }
                    }

                    FlowPage(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        flowViewModel = flowViewModel,
                        sharedTransitionScope = this@SharedTransitionScope,
                        animatedVisibilityScope = this,
                    )
                }
                animatedComposable(route = "${RouteName.READING}/{articleId}") { entry ->
                    val articleId = entry.arguments?.getString("articleId")?.also {
                        entry.arguments?.remove("articleId")
                    }

                    val readingViewModel: ReadingViewModel =
                        hiltViewModel<ReadingViewModel, ReadingViewModel.ReadingViewModelFactory> { factory ->
                            factory.create(articleId.toString(), null)
                        }

                    ReadingPage(
                        navController = navController,
                        readingViewModel = readingViewModel
                    )
                }

                // Settings
                animatedComposable(route = RouteName.SETTINGS) {
                    SettingsPage(navController)
                }

                // Accounts
                animatedComposable(route = RouteName.ACCOUNTS) {
                    AccountsPage(navController)
                }

                animatedComposable(route = "${RouteName.ACCOUNT_DETAILS}/{accountId}") {
                    AccountDetailsPage(navController)
                }

                animatedComposable(route = RouteName.ADD_ACCOUNTS) {
                    AddAccountsPage(navController)
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
                animatedComposable(route = RouteName.READING_PAGE_STYLE) {
                    ReadingStylePage(navController)
                }
                animatedComposable(route = RouteName.READING_BOLD_CHARACTERS) {
                    BoldCharactersPage(navController)
                }
                animatedComposable(route = RouteName.READING_PAGE_TITLE) {
                    ReadingTitlePage(navController)
                }
                animatedComposable(route = RouteName.READING_PAGE_TEXT) {
                    ReadingTextPage(navController)
                }
                animatedComposable(route = RouteName.READING_PAGE_IMAGE) {
                    ReadingImagePage(navController)
                }
                animatedComposable(route = RouteName.READING_PAGE_VIDEO) {
                    ReadingVideoPage(navController)
                }

                // Interaction
                animatedComposable(route = RouteName.INTERACTION) {
                    InteractionPage(navController)
                }

                // Languages
                animatedComposable(route = RouteName.LANGUAGES) {
                    LanguagesPage(navController = navController)
                }

                // Troubleshooting
                animatedComposable(route = RouteName.TROUBLESHOOTING) {
                    TroubleshootingPage(navController = navController)
                }

                // Tips & Support
                animatedComposable(route = RouteName.TIPS_AND_SUPPORT) {
                    TipsAndSupportPage(navController)
                }
                animatedComposable(route = RouteName.LICENSE_LIST) {
                    LicenseListPage(navController)
                }
            }
        }
    }
}
