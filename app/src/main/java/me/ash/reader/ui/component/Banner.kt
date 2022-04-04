/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Banner(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val lightThemeColors = MaterialTheme.colorScheme
    val lightPrimaryContainer = lightThemeColors.primaryContainer
    val lightOnSurface = lightThemeColors.onSurface

    Surface(
        modifier = modifier.fillMaxWidth().height(88.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(lightPrimaryContainer)
                .clickable { onClick() }
                .padding(16.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { icon ->
                Crossfade(targetState = icon) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp),
                        tint = lightOnSurface,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    maxLines = if (desc == null) 2 else 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = lightOnSurface,
                    overflow = TextOverflow.Ellipsis,
                )
                desc?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = lightOnSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            action?.let {
                Box(Modifier.padding(start = 16.dp)) {
                    CompositionLocalProvider(LocalContentColor provides lightOnSurface) {
                        it()
                    }
                }
            }
        }
    }
}
