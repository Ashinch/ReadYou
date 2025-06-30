package me.ash.reader.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import me.ash.reader.R

class ArticleCardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArticleCardWidget()
}

class ArticleCardWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            GlanceTheme {
                val backgroundColor = GlanceTheme.colors.surface.getColor(context)
                val onBackgroundColor = GlanceTheme.colors.onBackground.getColor(context)

                val textColor =
                    if (backgroundColor.luminance() > 0.5f) {
                        Color.White
                    } else onBackgroundColor

                // create your AppWidget here
                WidgetContainer {
                    Box(
                        contentAlignment = Alignment.BottomStart,
                        modifier = GlanceModifier.fillMaxSize(),
                    ) {
                        //                        Image(
                        //                            provider = ImageProvider(resId =
                        // R.drawable.animation),
                        //                            contentDescription = null,
                        //                            colorFilter = null,
                        //                            contentScale = ContentScale.Crop,
                        //                            modifier = GlanceModifier.fillMaxSize(),
                        //                        )
                        Spacer(
                            modifier =
                                GlanceModifier.fillMaxSize()
                                    .background(ImageProvider(R.drawable.scrim_gradient))
                        )
                        Column(GlanceModifier.padding(12.dp)) {
                            Text(
                                "Read You",
                                style =
                                    TextStyle(
                                        color = ColorProvider(textColor),
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                "Article of the Day",
                                style =
                                    TextStyle(
                                        color = ColorProvider(textColor),
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

