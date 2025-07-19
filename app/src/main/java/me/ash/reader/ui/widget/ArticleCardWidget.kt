package me.ash.reader.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
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
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Dimension
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.infrastructure.android.MainActivity
import me.ash.reader.ui.page.common.ExtraName
import timber.log.Timber

class ArticleCardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArticleCardWidget()
}

class ArticleCardWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val article =
            withContext(Dispatchers.IO) {
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
                val accountService = entryPoint.accountService()
                val articleDao = entryPoint.articleDao()
                val currentAccountId = accountService.getCurrentAccountId()
                articleDao.queryLatestUnreadArticles(accountId = currentAccountId, limit = 10).let {
                    it.firstOrNull { !it.article.img.isNullOrEmpty() } ?: it.firstOrNull()
                }
            } ?: return

        val bitmap =
            withContext(Dispatchers.IO) {
                val link = article.article.img
                val imageLoader = context.imageLoader
                imageLoader
                    .execute(
                        ImageRequest.Builder(context)
                            .data(link)
                            .size(width = Dimension.Pixels(600), height = Dimension.Undefined)
                            .build()
                    )
                    .drawable
                    ?.toBitmapOrNull()
            }

        provideContent {
            GlanceTheme {
                val titleColor =
                    if (bitmap != null) Color.White
                    else GlanceTheme.colors.onSurface.getColor(context)

                val feedColor =
                    if (bitmap != null) Color.White
                    else GlanceTheme.colors.primary.getColor(context)

                // create your AppWidget here
                WidgetContainer {
                    Box(
                        contentAlignment = Alignment.BottomStart,
                        modifier =
                            GlanceModifier.fillMaxSize()
                                .clickable(
                                    actionStartActivity<MainActivity>(
                                        actionParametersOf(
                                            ActionParameters.Key<String>(ExtraName.ARTICLE_ID) to
                                                article.article.id
                                        )
                                    )
                                ),
                    ) {
                        //                        Image(
                        //                            provider = ImageProvider(resId =
                        // R.drawable.animation),
                        //                            contentDescription = null,
                        //                            colorFilter = null,
                        //                            contentScale = ContentScale.Crop,
                        //                            modifier = GlanceModifier.fillMaxSize(),
                        //                        )
                        if (bitmap != null) {
                            Spacer(
                                modifier =
                                    GlanceModifier.fillMaxSize()
                                        .background(
                                            imageProvider = ImageProvider(bitmap),
                                            contentScale = ContentScale.Crop,
                                        )
                            )
                            Spacer(
                                modifier =
                                    GlanceModifier.fillMaxSize()
                                        .background(
                                            imageProvider = ImageProvider(R.drawable.scrim_gradient)
                                        )
                            )
                        }

                        Column(GlanceModifier.padding(12.dp)) {
                            Text(
                                article.feed.name,
                                style =
                                    TextStyle(
                                        color = ColorProvider(feedColor),
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                maxLines = 1,
                            )
                            Text(
                                article.article.title,
                                style =
                                    TextStyle(
                                        color = ColorProvider(titleColor),
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
