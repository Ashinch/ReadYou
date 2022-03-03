package me.ash.reader.ui.page.common

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.ash.reader.ui.page.home.HomePage
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.theme.AppTheme

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun HomeEntry() {
    val navController = rememberNavController()

    AppTheme {
        ProvideWindowInsets {
            rememberSystemUiController().run {
                setStatusBarColor(MaterialTheme.colorScheme.surface, !isSystemInDarkTheme())
                setSystemBarsColor(MaterialTheme.colorScheme.surface, !isSystemInDarkTheme())
                setNavigationBarColor(MaterialTheme.colorScheme.surface, !isSystemInDarkTheme())
            }
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .statusBarsPadding()
                ) {
                    NavHost(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        navController = navController,
                        startDestination = RouteName.HOME,
                    ) {
                        composable(route = RouteName.HOME) {
                            HomePage(navController)
                        }
                        composable(route = RouteName.SETTINGS) {
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