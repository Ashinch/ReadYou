package me.ash.reader.ui.page.home.reading

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun BoxScope.PullToLoadIndicator(state:PullToLoadState) {
    state.status.run {
        val fraction = state.offsetFraction
        val absFraction = abs(fraction)
        val imageVector = when (this) {
            PullToLoadState.Status.PulledDown -> Icons.Outlined.KeyboardArrowUp
            PullToLoadState.Status.PulledUp -> Icons.Outlined.KeyboardArrowDown
            else -> null
        }

        val alignment = if (fraction < 0f) {
            Alignment.BottomCenter
        } else {
            Alignment.TopCenter
        }
        if (this != PullToLoadState.Status.Idle) {
            Surface(
                modifier = Modifier
                    .align(alignment)
                    .padding(vertical = 80.dp)
                    .offset(y = (fraction * 48).dp)
                    .width(36.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center),
                ) {
                    AnimatedContent(
                        targetState = imageVector, modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        ), transitionSpec = {
                            (fadeIn(animationSpec = tween(220, delayMillis = 0)))
                                .togetherWith(fadeOut(animationSpec = tween(90)))
                        }, label = ""
                    ) {
                        if (it != null) {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
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
        }
    }
}