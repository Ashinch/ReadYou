package me.ash.reader.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.infrastructure.android.MainActivity
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.common.ExtraName
import timber.log.Timber

@AndroidEntryPoint
class ArticleListWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject lateinit var repository: WidgetRepository

    override val glanceAppWidget: GlanceAppWidget = ArticleListWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        repository.clearConfig(appWidgetIds)
    }
}

class ArticleListWidget() : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*>?
        get() =
            object : GlanceStateDefinition<Preferences> {
                override fun getLocation(context: Context, fileKey: String): File {
                    TODO("Not yet implemented")
                }

                override suspend fun getDataStore(
                    context: Context,
                    fileKey: String,
                ): DataStore<Preferences> {
                    return context.widgetDataStore
                }
            }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WidgetRepository.get(context)
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val initialConfig = repository.getConfig(widgetId)
        val configFlow = repository.getConfigFlow(widgetId)

        val initialData =
            withContext(Dispatchers.IO) { repository.getData(initialConfig.dataSource).first() }

        provideContent {
            val config = configFlow.collectAsStateValue(initialConfig)
            val (theme, dataSource) = config

            val data =
                remember(dataSource) { repository.getData(dataSource) }
                    .collectAsStateValue(initialData)

            val (title, articles) = data

            GlanceTheme {
                WidgetContainer {
                    Spacer(modifier = GlanceModifier.height(24.dp))
                    Header(title, theme)
                    ArticleList(articles, theme)
                }
            }
        }
    }
}

@GlanceComposable
@Composable
fun WidgetContainer(modifier: GlanceModifier = GlanceModifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxSize().background(GlanceTheme.colors.surface), content = { content() })
}

@GlanceComposable
@Composable
fun Header(text: String, theme: Theme, modifier: GlanceModifier = GlanceModifier) {
    val widgetHeight = LocalSize.current.height
    val widgetWidth = LocalSize.current.width

    val fontSize =
        if (widgetHeight > 240.dp && widgetWidth > 300.dp) {
            24.sp
        } else {
            18.sp
        }
    Column(modifier = modifier.padding(bottom = 8.dp).padding(horizontal = 12.dp)) {
        Text(
            text = text,
            style =
                TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface,
                    fontFamily =
                        when (theme) {
                            Theme.Serif -> FontFamily.Serif
                            Theme.SansSerif -> FontFamily.SansSerif
                        },
                ),
            maxLines = 1,
        )
    }
}

@Composable
fun ArticleList(items: List<Article>, theme: Theme, modifier: GlanceModifier = GlanceModifier) {
    LazyColumn(modifier = modifier) {
        item { Spacer(modifier = GlanceModifier.height(4.dp)) }
        items(items) {
            ArticleItem(
                article = it,
                theme = theme,
                modifier =
                    GlanceModifier.clickable(
                        actionStartActivity<MainActivity>(
                            actionParametersOf(
                                ActionParameters.Key<String>(ExtraName.ARTICLE_ID) to it.id
                            )
                        )
                    ),
            )
        }
        item { Spacer(modifier = GlanceModifier.height(12.dp)) }
    }
}

@GlanceComposable
@Composable
fun ArticleItem(article: Article, theme: Theme, modifier: GlanceModifier = GlanceModifier) {
    Column(modifier = modifier.padding(bottom = 8.dp).padding(horizontal = 12.dp)) {
        Text(
            text = article.feedName,
            style =
                TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                    fontFamily = FontFamily.SansSerif,
                ),
            maxLines = 1,
        )
        Text(
            text = article.title,
            style =
                TextStyle(
                    fontSize = 16.sp,
                    fontWeight =
                        when (theme) {
                            Theme.Serif -> FontWeight.Bold
                            Theme.SansSerif -> FontWeight.Bold
                        },
                    color = GlanceTheme.colors.onSurface,
                    fontFamily =
                        when (theme) {
                            Theme.Serif -> FontFamily.Serif
                            Theme.SansSerif -> FontFamily.SansSerif
                        },
                ),
            maxLines = 2,
        )
    }
}

private val previewArticles =
    listOf<Article>(
        Article(
            title = "5 Takeaways From Lorde’s New Album Virgin",
            feedName = "Pitchfork",
            id = "",
        ),
        Article(
            title =
                "Big Thief’s Adrianne Lenker Announces Live Album, Shares Previously Unreleased Song “Happiness”",
            feedName = "Pitchfork",
            id = "",
        ),
        Article(
            title = "Haruomi Hosono on the Music That Made Him",
            feedName = "Pitchfork",
            id = "",
        ),
        Article(
            title = "Faye Webster Announces Orchestral Tour Dates",
            feedName = "Pitchfork",
            id = "",
        ),
        Article(
            title =
                "Big Thief’s Adrianne Lenker Announces Live Album, Shares Previously Unreleased Song “Happiness”",
            feedName = "Pitchfork",
            id = "",
        ),
        Article(
            title = "Faye Webster Announces Orchestral Tour Dates",
            feedName = "Pitchfork",
            id = "",
        ),
    )

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@GlanceComposable
@Preview(widthDp = 200, heightDp = 200)
@Preview(widthDp = 240, heightDp = 300)
@Preview(widthDp = 360, heightDp = 360)
private fun PreviewArticleList() {

    GlanceTheme {
        // create your AppWidget here
        WidgetContainer {
            Spacer(modifier = GlanceModifier.height(24.dp))
            Header("Media", Theme.SansSerif)
            ArticleList(previewArticles, Theme.SansSerif)
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@GlanceComposable
@Preview(widthDp = 200, heightDp = 200)
@Preview(widthDp = 240, heightDp = 300)
@Preview(widthDp = 360, heightDp = 360)
private fun PreviewArticleListSerif() {

    GlanceTheme {
        // create your AppWidget here
        WidgetContainer {
            Spacer(modifier = GlanceModifier.height(24.dp))
            Header("Media", Theme.Serif)
            ArticleList(previewArticles, Theme.Serif)
        }
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@GlanceComposable
@Preview(widthDp = 120, heightDp = 120)
@Preview(widthDp = 150, heightDp = 150)
private fun PreviewArticleCard() {
    GlanceTheme {
        // create your AppWidget here
        WidgetContainer {
            Box(contentAlignment = Alignment.BottomStart, modifier = GlanceModifier.fillMaxSize()) {
                Image(
                    provider = ImageProvider(resId = R.drawable.animation),
                    contentDescription = null,
                    colorFilter = null,
                    contentScale = ContentScale.Fit,
                    modifier = GlanceModifier.fillMaxSize(),
                )
                Spacer(
                    modifier =
                        GlanceModifier.fillMaxSize()
                            .background(ImageProvider(R.drawable.scrim_gradient))
                )
                Column(GlanceModifier.padding(8.dp)) {
                    Text(
                        "Read You",
                        style =
                            TextStyle(
                                color = ColorProvider(Color.White),
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
                                color = ColorProvider(Color.White),
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
