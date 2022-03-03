package me.ash.reader.ui.widget

import android.graphics.BitmapFactory
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.ui.util.paddingFixedHorizontal
import me.ash.reader.ui.util.roundClick

@ExperimentalAnimationApi
@Composable
fun BarButton(
    barButtonType: BarButtonType,
    iconOnClickListener: () -> Unit = {},
    onClickListener: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .paddingFixedHorizontal()
            .roundClick(onClick = onClickListener),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(
                    start = 10.dp,
                    end = if (barButtonType is FirstExpandType) 2.dp else 10.dp
                )
        ) {
            when (barButtonType) {
                is SecondExpandType -> {
                    Icon(
                        imageVector = barButtonType.img,
                        contentDescription = "icon",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(CircleShape)
                            .clickable(onClick = iconOnClickListener),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                is ItemType -> {
                    val modifier = Modifier
                    Row(
                        modifier = modifier
                            .padding(start = 28.dp, end = 4.dp)
                            .size(24.dp)
                            .background(
                                if (barButtonType.icon != null) Color.Unspecified
                                else MaterialTheme.colorScheme.inversePrimary
                            ),
//                                .background(MaterialTheme.colorScheme.inversePrimary),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (barButtonType.icon == null) {
                            Icon(
                                painter = painterResource(id = R.drawable.default_folder),
                                contentDescription = "icon",
                                modifier = modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        } else {
                            Image(
                                painter = barButtonType.icon,
                                contentDescription = "icon",
                                modifier = modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
            when (barButtonType) {
                is ButtonType -> {
                    AnimatedText(
                        modifier = Modifier.weight(1f),
                        text = barButtonType.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                else -> {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 20.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = barButtonType.text,
                        fontSize = if (barButtonType is FirstExpandType) 22.sp else 18.sp,
                        fontWeight = if (barButtonType is FirstExpandType) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (barButtonType is FirstExpandType)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            when (barButtonType) {
                is ButtonType, is ItemType, is SecondExpandType -> {
                    AnimatedText(
                        text = barButtonType.additional.toString(),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                is FirstExpandType -> {
                    Icon(
                        imageVector = barButtonType.additional as ImageVector,
                        contentDescription = "Expand More",
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }
}

interface BarButtonType {
    val img: Any?
    val text: String
    val additional: Any?
}

class ButtonType(
    private val content: String,
    private val important: Int,
) : BarButtonType {
    override val img: ImageVector?
        get() = null
    override val text: String
        get() = content
    override val additional: Any
        get() = important
}

class FirstExpandType(
    private val content: String,
    private val icon: ImageVector,
) : BarButtonType {
    override val img: ImageVector?
        get() = null
    override val text: String
        get() = content
    override val additional: Any
        get() = icon
}

class SecondExpandType(
    private val icon: ImageVector,
    private val content: String,
    private val important: Int,
) : BarButtonType {
    override val img: ImageVector
        get() = icon
    override val text: String
        get() = content
    override val additional: Any
        get() = important
}

class ItemType(
//    private val icon: String,
    val icon: BitmapPainter?,
    private val content: String,
    private val important: Int,
) : BarButtonType {
    //    override val img: String
    override val img: Painter
        get() = icon ?: BitmapPainter(
            BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0).asImageBitmap()
        )
    override val text: String
        get() = content
    override val additional: Any
        get() = important
}
