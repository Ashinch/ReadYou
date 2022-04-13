/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.ui.theme.palette.onDark
import me.ash.reader.ui.theme.palette.tonalPalettes

// TODO: ripple & swipe
@Composable
fun Switch(
    modifier: Modifier = Modifier,
    activated: Boolean,
    enable: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val tonalPalettes = MaterialTheme.colorScheme.tonalPalettes()

    Surface(
        modifier = modifier.size(56.dp, 28.dp).alpha(if (enable) 1f else 0.5f),
        shape = CircleShape,
        color = animateColorAsState(
            if (activated) (tonalPalettes primary 40) onDark (tonalPalettes neutralVariant 50)
            else (tonalPalettes neutralVariant 50) onDark (tonalPalettes neutral 60)
        ).value
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                    then if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ) {
            Surface(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = animateDpAsState(if (activated) 32.dp else 4.dp).value),
                shape = CircleShape,
                color = animateColorAsState(
                    if (activated) tonalPalettes primary 90
                    else (tonalPalettes neutralVariant 70) onDark (tonalPalettes neutral 30)
                ).value
            ) {}
        }
    }
}

// TODO: inactivated colors
@Composable
fun SwitchHeadline(
    activated: Boolean,
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    val tonalPalettes = MaterialTheme.colorScheme.tonalPalettes()

    Surface(
        modifier = modifier,
        color = Color.Unspecified,
        contentColor = tonalPalettes neutral 10,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(tonalPalettes primary 90)
                .clickable { onClick() }
                .padding(20.dp, 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                )
            }
            Box(Modifier.padding(start = 20.dp)) {
                Switch(activated = activated)
            }
        }
    }
}
