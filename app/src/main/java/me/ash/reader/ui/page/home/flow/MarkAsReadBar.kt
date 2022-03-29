package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.R

@Composable
fun MarkAsReadBar() {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MarkAsReadBarItem(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.seven_days),
        )
        MarkAsReadBarItem(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.three_days),
        )
        MarkAsReadBarItem(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.one_day),
        )
        MarkAsReadBarItem(
            modifier = Modifier.weight(2.5f),
            text = stringResource(R.string.mark_all_as_read),
            isPrimary = true,
        )
    }
}

@Composable
fun MarkAsReadBarItem(
    modifier: Modifier = Modifier,
    text: String,
    isPrimary: Boolean = false,
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { },
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        color = if (isPrimary) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
        }
    }
    if (!isPrimary) {
        Spacer(modifier = Modifier.width(8.dp))
    }
}