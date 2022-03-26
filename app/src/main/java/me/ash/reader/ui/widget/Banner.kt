package me.ash.reader.ui.widget

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.ui.theme.LocalLightThemeColors

@Composable
fun Banner(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val lightThemeColors = LocalLightThemeColors.current
    val lightPrimaryContainer = lightThemeColors.primaryContainer
    val lightOnSurface = lightThemeColors.onSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    maxLines = if (desc == null) 2 else 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = lightOnSurface,
                )
                desc?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        color = lightOnSurface.copy(alpha = 0.7f),
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
