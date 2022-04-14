package me.ash.reader.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.theme.palette.alwaysLight
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun BlockButton(
    modifier: Modifier = Modifier,
    text: String = "",
    selected: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(0.7f) onDark MaterialTheme.colorScheme.inverseOnSurface,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer alwaysLight true,
    contentColor: Color = MaterialTheme.colorScheme.inverseSurface,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSurface alwaysLight true,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) selectedContainerColor else containerColor)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) selectedContentColor else contentColor,
        )
    }
}