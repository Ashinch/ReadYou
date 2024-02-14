package me.ash.reader.ui.component.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.ash.reader.ui.theme.palette.alwaysLight
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun BlockButton(
    modifier: Modifier = Modifier,
    text: String = "",
    selected: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer onDark MaterialTheme.colorScheme.inverseOnSurface,
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
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                textAlign = TextAlign.Center,
            ),
            color = if (selected) selectedContentColor else contentColor,
        )
    }
}
