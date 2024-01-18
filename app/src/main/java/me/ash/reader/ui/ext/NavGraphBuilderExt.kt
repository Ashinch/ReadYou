/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.ext

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import me.ash.reader.ui.motion.materialSharedAxisXIn
import me.ash.reader.ui.motion.materialSharedAxisXOut

@OptIn(ExperimentalAnimationApi::class)
@Deprecated(message = "Migrate to Forward and backward transition", replaceWith = ReplaceWith("forwardAndBackwardComposable(route = route, arguments = arguments, deepLinks = deepLinks) { content() }")
)
fun NavGraphBuilder.animatedComposable(
        route: String,
        arguments: List<NamedNavArgument> = emptyList(),
        deepLinks: List<NavDeepLink> = emptyList(),
        content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) = composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(220, delayMillis = 90)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(90))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(220, delayMillis = 90)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(90))
        },
        content = content
)

private const val INITIAL_OFFSET_FACTOR = 0.10f

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.forwardAndBackwardComposable(
        route: String,
        arguments: List<NamedNavArgument> = emptyList(),
        deepLinks: List<NavDeepLink> = emptyList(),
        content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            materialSharedAxisXIn(initialOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() })
        },
        exitTransition = {
            materialSharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
        },
        popEnterTransition = {
            materialSharedAxisXIn(initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
        },
        popExitTransition = {
            materialSharedAxisXOut(targetOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() })
        },
        content = content
)
