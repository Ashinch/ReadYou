/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.ext

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.ash.reader.ui.motion.EmphasizedAccelerate
import me.ash.reader.ui.motion.EmphasizedDecelerate
import me.ash.reader.ui.motion.materialSharedAxisXIn
import me.ash.reader.ui.motion.materialSharedAxisXOut

private const val INITIAL_OFFSET_FACTOR = 0.10f
private const val INITIAL_SCALE_FACTOR = 0.8f


fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    usePredictiveBack: Boolean = Build.VERSION.SDK_INT >= 34,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    if (usePredictiveBack) {
        animatedComposablePredictiveBack(route, arguments, deepLinks, content)
    } else {
        animatedComposableLegacy(route, arguments, deepLinks, content)
    }
}

fun NavGraphBuilder.animatedComposablePredictiveBack(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) =
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = { materialSharedAxisXIn(initialOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() }) },
        exitTransition = {
            materialSharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
        },
        popEnterTransition = {
            scaleIn(
                animationSpec = tween(durationMillis = 350, easing = EmphasizedDecelerate),
                initialScale = INITIAL_SCALE_FACTOR,
            ) + materialSharedAxisXIn(initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
        },
        popExitTransition = {
            materialSharedAxisXOut(targetOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() }) +
                    scaleOut(
                        targetScale = INITIAL_SCALE_FACTOR,
                        animationSpec = tween(durationMillis = 350, easing = EmphasizedAccelerate),
                    )
        },
        content = content,
    )

fun NavGraphBuilder.animatedComposableLegacy(
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
