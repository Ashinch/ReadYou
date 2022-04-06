package me.ash.reader.ui.page.common

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.ash.reader.ui.ext.animatedComposable
import me.ash.reader.ui.ext.isFirstLaunch
import me.ash.reader.ui.page.home.HomePage
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.page.startup.StartupPage
import me.ash.reader.ui.theme.AppTheme

@OptIn(ExperimentalAnimationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeEntry() {
    val context = LocalContext.current
    val navController = rememberAnimatedNavController()

    AppTheme {
        ProvideWindowInsets {
            rememberSystemUiController().run {
                setStatusBarColor(Color.Transparent, !isSystemInDarkTheme())
                setSystemBarsColor(Color.Transparent, !isSystemInDarkTheme())
                setNavigationBarColor(MaterialTheme.colorScheme.surface, !isSystemInDarkTheme())
            }
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .statusBarsPadding()
                ) {
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
                    }
                }
                Spacer(
                    modifier = Modifier
                        .navigationBarsHeight()
                        .fillMaxWidth()
                )
            }
        }
    }
}