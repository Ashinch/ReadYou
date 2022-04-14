package me.ash.reader.ui.page.home.flow

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.ui.component.AnimatedPopup
import me.ash.reader.ui.theme.palette.alwaysLight

@Composable
fun MarkAsReadBar(
    visible: Boolean = false,
    absoluteY: Dp = Dp.Hairline,
    onDismissRequest: () -> Unit = {},
    onItemClick: (MarkAsReadBefore) -> Unit = {},
) {
    val animated = remember { Animatable(absoluteY.value) }

    LaunchedEffect(absoluteY) {
        snapshotFlow { absoluteY }.collect {
            animated.animateTo(it.value, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    AnimatedPopup(
        visible = visible,
        absoluteY = animated.value.dp,
        onDismissRequest = onDismissRequest,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MarkAsReadBarItem(
                modifier = Modifier.width(56.dp),
                text = stringResource(R.string.seven_days),
            ) {
                onItemClick(MarkAsReadBefore.SevenDays)
            }
            MarkAsReadBarItem(
                modifier = Modifier.width(56.dp),
                text = stringResource(R.string.three_days),
            ) {
                onItemClick(MarkAsReadBefore.ThreeDays)
            }
            MarkAsReadBarItem(
                modifier = Modifier.width(56.dp),
                text = stringResource(R.string.one_day),
            ) {
                onItemClick(MarkAsReadBefore.OneDay)
            }
            MarkAsReadBarItem(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.mark_all_as_read),
                isPrimary = true,
            ) {
                onItemClick(MarkAsReadBefore.All)
            }
        }
    }
}

@Composable
fun MarkAsReadBarItem(
    modifier: Modifier = Modifier,
    text: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit = {},
) {
    val view = LocalView.current

    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onClick()
            },
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        color = if (isPrimary) {
            MaterialTheme.colorScheme.primaryContainer alwaysLight true
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
                    MaterialTheme.colorScheme.onSurface alwaysLight true
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