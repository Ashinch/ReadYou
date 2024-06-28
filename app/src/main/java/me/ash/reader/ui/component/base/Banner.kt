/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.component.base

import android.view.SoundEffectConstants
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.ui.theme.palette.alwaysLight

@Composable
fun Banner(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val view = LocalView.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp),
        color = Color.Unspecified,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(backgroundColor alwaysLight true)
                .clickable {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onClick()
                }
                .padding(16.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { icon ->
                Crossfade(targetState = icon) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.onSurface alwaysLight true,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    maxLines = if (desc == null) 2 else 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface alwaysLight true,
                    overflow = TextOverflow.Ellipsis,
                )
                desc?.let {
                    Text(
//                        modifier = Modifier.animateContentSize(tween()),
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = (MaterialTheme.colorScheme.onSurface alwaysLight true).copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            action?.let {
                Box(Modifier.padding(start = 16.dp)) {
                    CompositionLocalProvider(
                        LocalContentColor provides (MaterialTheme.colorScheme.onSurface alwaysLight true)
                    ) { it() }
                }
            }
        }
    }
}
