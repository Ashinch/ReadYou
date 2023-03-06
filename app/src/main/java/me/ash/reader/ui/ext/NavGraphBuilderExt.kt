/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.ext

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    reduceAnimation: Boolean,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) = if (reduceAnimation) {
    Log.i("RLog", "ReduceAnimation: $reduceAnimation")

    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        content = content,
    )
} else {
    Log.i("RLog", "ReduceAnimation: $reduceAnimation")
    composable(
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
}
