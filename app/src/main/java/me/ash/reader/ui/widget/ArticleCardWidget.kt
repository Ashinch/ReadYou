package me.ash.reader.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.infrastructure.android.MainActivity
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.common.ExtraName

@AndroidEntryPoint
class ArticleCardWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject lateinit var repository: WidgetRepository
    override val glanceAppWidget: GlanceAppWidget = ArticleCardWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        repository.clearConfig(appWidgetIds)
    }
}

class ArticleCardWidget() : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WidgetRepository.get(context)
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        val initialConfig = repository.getConfig(widgetId)

        val initialData =
            withContext(Dispatchers.IO) { repository.getData(initialConfig.dataSource).first() }

        val configFlow = repository.getConfigFlow(widgetId)

        val initialArticle =
            withContext(Dispatchers.IO) {
                initialData.articles.let {
                    it.firstOrNull { !it.imgUrl.isNullOrEmpty() } ?: it.firstOrNull()
                }
            } ?: return

        val initialBitmap = repository.fetchBitmap(initialArticle.imgUrl)

        provideContent {
            val (_, dataSource) = configFlow.collectAsStateValue(initialConfig)

            val articleFlow =
                remember(dataSource) { repository.getData(dataSource) }
                    .mapNotNull {
                        it.articles.let {
                            it.firstOrNull { !it.imgUrl.isNullOrEmpty() } ?: it.firstOrNull()
                        }
                    }

            val article = articleFlow.collectAsStateValue(initialArticle)

            val bitmap =
                articleFlow
                    .map { repository.fetchBitmap(it.imgUrl) }
                    .collectAsStateValue(initialBitmap)

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
                                                article.id
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
                                article.feedName,
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
                                article.title,
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
