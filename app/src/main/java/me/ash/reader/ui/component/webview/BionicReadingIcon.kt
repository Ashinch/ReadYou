package me.ash.reader.ui.component.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BionicReadingIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Black,
    filled: Boolean = false,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Row {
            Text(
                text = "B",
                fontFamily = FontFamily.SansSerif,
                fontSize = (size.value * 0.65F).sp,
                fontWeight = if (filled) FontWeight.W900 else FontWeight.W700,
                color = if (filled) tint else tint.copy(alpha = 0.6F),
                textDecoration = if (filled) TextDecoration.Underline else TextDecoration.None
            )
            Text(
                text = "R",
                fontFamily = FontFamily.SansSerif,
                fontSize = (size.value * 0.65F).sp,
                fontWeight = FontWeight.W300,
                color = if (filled) tint else tint.copy(alpha = 0.6F),
                textDecoration = if (filled) TextDecoration.Underline else TextDecoration.None
            )
        }
    }
}

@Preview(backgroundColor = 0xFFFFFF)
@Composable
private fun BionicReadingIconPreview() {
    Column {
        BionicReadingIcon()
        BionicReadingIcon(filled = true)
    }
}
