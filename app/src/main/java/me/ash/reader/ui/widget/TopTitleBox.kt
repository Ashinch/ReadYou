package me.ash.reader.ui.widget

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.ash.reader.ui.util.calculateTopBarAnimateValue

@ExperimentalAnimationApi
@Composable
fun BoxScope.TopTitleBox(
    title: String,
    description: String,
    listState: LazyListState,
    SpacerHeight: Float = Float.NaN,
    startOffset: Offset,
    startHeight: Float,
    startTitleFontSize: Float,
    startDescriptionFontSize: Float,
    clickable: () -> Unit = {},
) {
    val transition = updateTransition(targetState = listState, label = "")
    val offset by transition.animateOffset(
        label = "",
        transitionSpec = { spring() }
    ) {
        Offset(
            x = it.calculateTopBarAnimateValue(startOffset.x, 56f),
            y = it.calculateTopBarAnimateValue(startOffset.y, 0f)
        )
    }

    val height by transition.animateFloat(
        label = "",
        transitionSpec = { spring() }
    ) {
        it.calculateTopBarAnimateValue(startHeight, 64f)
    }

    val titleFontSize by transition.animateFloat(
        label = "",
        transitionSpec = { spring(stiffness = Spring.StiffnessHigh) }
    ) {
        it.calculateTopBarAnimateValue(startTitleFontSize, 16f)
    }

    val descriptionFontSize by transition.animateFloat(
        label = "",
        transitionSpec = { spring(stiffness = Spring.StiffnessHigh) }
    ) {
        it.calculateTopBarAnimateValue(startDescriptionFontSize, 12f)
    }

    Box(
        modifier = Modifier
            .zIndex(1f)
            .height(height.dp)
            .offset(offset.x.dp, offset.y.dp)
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClickLabel = "回到顶部",
                onClick = clickable ?: {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column {
            AnimatedText(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = titleFontSize.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(SpacerHeight.dp))
            AnimatedText(
                text = description,
                fontWeight = FontWeight.SemiBold,
                fontSize = descriptionFontSize.sp,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}