package me.ash.reader.ui.page.home.flow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.ui.page.home.reading.PullToLoadDefaults
import me.ash.reader.ui.page.home.reading.PullToLoadState
import me.ash.reader.ui.page.home.reading.PullToLoadState.Status.PulledUp
import me.ash.reader.ui.page.home.reading.PullToLoadState.Status.PullingUp
import me.ash.reader.ui.theme.palette.LocalFixedColorRoles
import kotlin.math.abs

enum class LoadAction {
    NextFeed, MarkAllAsRead
}

@Composable
fun BoxScope.PullToLoadIndicator(
    modifier: Modifier = Modifier,
    state: PullToLoadState,
    loadAction: LoadAction? = null,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val status = state.status

    val canLoadNext = loadAction != null

    LaunchedEffect(status) {
        when {
            canLoadNext && status == PulledUp -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            else -> {}
        }
    }

    val fraction = state.offsetFraction
    val absFraction = abs(fraction)

    val alignment = if (fraction < 0f) {
        Alignment.BottomCenter
    } else {
        Alignment.TopCenter
    }

    val visible = remember(status, canLoadNext) {
        when (status) {
            PullingUp, PulledUp -> {
                canLoadNext
            }

            else -> {
                false
            }
        }
    }

    if (visible && !state.isSettled) {
        if (loadAction == LoadAction.NextFeed) {
            Surface(
                modifier = modifier
                    .align(alignment)
                    .padding(vertical = 80.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (fraction * PullToLoadDefaults.ContentOffsetMultiple * .5f).dp.roundToPx()
                        )
                    }
                    .width(36.dp),
                color = LocalFixedColorRoles.current.primaryFixed,
                shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    AnimatedContent(
                        targetState = status, modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        ), transitionSpec = {
                            (fadeIn(animationSpec = tween(220, delayMillis = 0))).togetherWith(
                                fadeOut(animationSpec = tween(90))
                            )
                        }, label = "", contentAlignment = Alignment.Center
                    ) { status ->
                        if (status == PulledUp) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = LocalFixedColorRoles.current.onPrimaryFixedVariant,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .padding(vertical = (2 * absFraction).dp)
                                    .size(32.dp)
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height((12 * absFraction).dp)
                            )
                        }
                    }
                }

            }
        } else {
            Box(
                modifier = modifier
                    .align(alignment)
                    .padding(vertical = 80.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (fraction * PullToLoadDefaults.ContentOffsetMultiple * .5f).dp.roundToPx()
                        )
                    }
            ) {
                AnimatedVisibility(
                    visible = status == PulledUp, enter = fadeIn() + scaleIn(),
                    exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + scaleOut()
                ) {
                    Box(
                        modifier = modifier
                            .background(
                                LocalFixedColorRoles.current.tertiaryFixed,
                                shape = MaterialTheme.shapes.extraLarge
                            ), contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = null,
                                tint = LocalFixedColorRoles.current.onTertiaryFixed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.mark_all_as_read),
                                style = MaterialTheme.typography.labelLarge,
                                color = LocalFixedColorRoles.current.onTertiaryFixed
                            )
                        }
                    }
                }
            }
        }
    }

}

@Preview
@Composable
private fun MarkAllAsRead() {
    Row {
        Surface(
            shape = CircleShape,
            color = LocalFixedColorRoles.current.primaryFixed,
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp), contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.DoneAll, null, modifier = Modifier.size(16.dp))
            }
        }
        Surface(
            shape = CircleShape,
            color = LocalFixedColorRoles.current.primaryFixed,
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp), contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.KeyboardArrowDown, null)
            }
        }
    }

}