package me.ash.reader.ui.page.home.feed

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.ui.widget.AnimatedText
import me.ash.reader.ui.widget.Menu

@Composable
fun ButtonBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    buttonBarType: ButtonBarType,
) {
    var expanded by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(DpOffset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .indication(interactionSource, LocalIndication.current)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                    },
                    onTap = {
                        onClick()
                    },
                    onLongPress = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        offset = DpOffset(it.x.toDp(), it.y.toDp())
                        expanded = true
                    },
                )
            },
    ) {
        when (buttonBarType) {
            is ButtonBarType.FilterBar -> FilterBar(buttonBarType)
            is ButtonBarType.AllBar -> AllBar(buttonBarType)
            is ButtonBarType.GroupBar -> GroupBar(buttonBarType)
            is ButtonBarType.FeedBar -> FeedBar(buttonBarType)
        }
    }

    Menu(
        offset = offset,
        expanded = expanded,
        dismissFunction = { expanded = false },
    )
}

@Composable
fun FilterBar(
    buttonBarType: ButtonBarType.FilterBar,
) {
    AnimatedText(
        text = buttonBarType.title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    AnimatedText(
        text = buttonBarType.important.toString(),
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
fun AllBar(
    buttonBarType: ButtonBarType.AllBar,
) {
    AnimatedText(
        text = buttonBarType.title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    Icon(
        imageVector = buttonBarType.icon,
        contentDescription = "Expand More",
        tint = MaterialTheme.colorScheme.outline,
    )
}

@Composable
fun GroupBar(
    buttonBarType: ButtonBarType.GroupBar,
) {
    Row {
        Icon(
            imageVector = buttonBarType.icon,
            contentDescription = "icon",
            modifier = Modifier
                .padding(end = 4.dp)
                .clip(CircleShape)
                .clickable(onClick = buttonBarType.iconOnClick),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(end = 20.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = buttonBarType.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
    AnimatedText(
        text = buttonBarType.important.toString(),
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
fun FeedBar(
    buttonBarType: ButtonBarType.FeedBar,
) {
    Row {
        Surface(
            modifier = Modifier
                .padding(start = 28.dp, end = 4.dp)
                .size(24.dp),
            //.background(MaterialTheme.colorScheme.inversePrimary),
            color = if (buttonBarType.icon == null) {
                MaterialTheme.colorScheme.inversePrimary
            } else {
                Color.Unspecified
            }
        ) {
            if (buttonBarType.icon == null) {
                Icon(
                    painter = painterResource(id = R.drawable.default_folder),
                    contentDescription = "icon",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Image(
                    painter = buttonBarType.icon,
                    contentDescription = "icon",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            modifier = Modifier
                .padding(end = 20.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = buttonBarType.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
    AnimatedText(
        text = buttonBarType.important.toString(),
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.outline,
    )
}

sealed class ButtonBarType {
    data class FilterBar(
        val modifier: Modifier = Modifier,
        val title: String = "",
        val important: Int = 0,
    ) : ButtonBarType()

    data class AllBar(
        val title: String = "",
        val icon: ImageVector,
    ) : ButtonBarType()

    data class GroupBar(
        val title: String = "",
        val important: Int = 0,
        val icon: ImageVector,
        val iconOnClick: () -> Unit = {},
    ) : ButtonBarType()

    data class FeedBar(
        val title: String = "",
        val important: Int = 0,
        val icon: Painter? = null,
    ) : ButtonBarType()
}