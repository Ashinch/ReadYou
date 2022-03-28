package me.ash.reader.ui.page.common

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.ash.reader.ui.page.home.HomePage
import me.ash.reader.ui.page.settings.SettingsPage
import me.ash.reader.ui.theme.AppTheme

@OptIn(ExperimentalAnimationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun HomeEntry(
    extras: Bundle? = null,
) {
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
                        startDestination = RouteName.HOME,
                    ) {
                        composable(
                            route = RouteName.HOME,
                            enterTransition = {
                                slideInHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    initialOffsetX = { -it }
                                ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    targetOffsetX = { it }
                                ) + fadeOut(animationSpec = tween(220, delayMillis = 90))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    initialOffsetX = { -it }
                                ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    targetOffsetX = { it }
                                ) + fadeOut(animationSpec = tween(220, delayMillis = 90))
                            },
                        ) {
                            HomePage(navController, extras?.get(ExtraName.ARTICLE_ID))
                        }
                        composable(
                            route = RouteName.SETTINGS,
                            enterTransition = {
                                slideInHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    initialOffsetX = { -it }
                                ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    targetOffsetX = { -it }
                                ) + fadeOut(animationSpec = tween(220, delayMillis = 90))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    initialOffsetX = { -it }
                                ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioNoBouncy
                                    ),
                                    targetOffsetX = { -it }
                                ) + fadeOut(animationSpec = tween(220, delayMillis = 90))
                            },
                        ) {
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