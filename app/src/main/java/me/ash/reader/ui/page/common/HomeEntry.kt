package me.ash.reader.ui.page.common

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.ash.reader.ui.ext.animatedComposable
import me.ash.reader.ui.ext.isFirstLaunch
import me.ash.reader.ui.page.home.HomePage
import me.ash.reader.ui.page.settings.ColorAndStyle
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.page.startup.StartupPage
import me.ash.reader.ui.theme.AppTheme
import me.ash.reader.ui.theme.LocalUseDarkTheme

@OptIn(ExperimentalAnimationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeEntry() {
    AppTheme {
        val context = LocalContext.current
        val useDarkTheme = LocalUseDarkTheme.current
        val navController = rememberAnimatedNavController()

        rememberSystemUiController().run {
            setStatusBarColor(Color.Transparent, !useDarkTheme)
            setSystemBarsColor(Color.Transparent, !useDarkTheme)
            setNavigationBarColor(Color.Transparent, !useDarkTheme)
        }

        AnimatedNavHost(
            navController = navController,
            startDestination = if (context.isFirstLaunch) RouteName.STARTUP else RouteName.HOME,
        ) {
            animatedComposable(route = RouteName.STARTUP) {
                StartupPage(navController)
            }
            animatedComposable(route = RouteName.HOME) {
                HomePage(navController)
            }
            animatedComposable(route = RouteName.SETTINGS) {
                SettingsPage(navController)
            }
            animatedComposable(route = RouteName.COLOR_AND_STYLE) {
                ColorAndStyle(navController)
            }
        }
    }
}