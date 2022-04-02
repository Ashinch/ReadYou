package me.ash.reader.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieAnimation(
    modifier: Modifier = Modifier,
    url: String,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url(url)
    )

    LottieAnimation(
        composition = composition,
        modifier = modifier,
        isPlaying = true,
        restartOnPlay = true,
        iterations = Int.MAX_VALUE,
    )
}