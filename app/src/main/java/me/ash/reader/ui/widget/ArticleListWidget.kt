package me.ash.reader.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.service.AccountService

class ArticleListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArticleListWidget()
}

class ArticleListWidget : GlanceAppWidget() {


    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint =
            EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val articles = withContext(Dispatchers.IO) {
            val articleDao = entryPoint.articleDao()
            val accountService = entryPoint.accountService()
            val currentAccountId = accountService.getCurrentAccountId()
            articleDao.queryLatestUnreadArticles(accountId = currentAccountId, limit = 10)
        }.map {
            Article(
                title = it.article.title,
                feedName = it.feed.name,
            )
        }

        provideContent {
            GlanceTheme {
                // create your AppWidget here
                WidgetContainer {
                    val theme = Theme.Serif
                    Spacer(modifier = GlanceModifier.height(24.dp))
                    Header(context.getString(R.string.unread), theme)
                    ArticleList(articles, theme)
                }
            }
        }
    }
}

@GlanceComposable
@Composable
fun WidgetContainer(modifier: GlanceModifier = GlanceModifier, content: @Composable () -> Unit) {
    Column(
        modifier.fillMaxSize().background(GlanceTheme.colors.surface),
        content = { content() },
    )
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
                    fontFamily = when (theme) {
                        Theme.Serif -> FontFamily.Serif
                        Theme.SansSerif -> FontFamily.SansSerif
                    }
                ),
            maxLines = 1,
        )
    }
}

@Composable
fun ArticleList(items: List<Article>, theme: Theme, modifier: GlanceModifier = GlanceModifier) {
    LazyColumn(modifier = modifier) {
        item { Spacer(modifier = GlanceModifier.height(4.dp)) }
        items(items) { ArticleItem(article = it, theme = theme) }
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
                    fontWeight = when (theme) {
                        Theme.Serif -> FontWeight.Bold
                        Theme.SansSerif -> FontWeight.Bold
                    },
                    color = GlanceTheme.colors.onSurface,
                    fontFamily = when (theme) {
                        Theme.Serif -> FontFamily.Serif
                        Theme.SansSerif -> FontFamily.SansSerif
                    }
                ),
            maxLines = 2,
        )
    }
}

private val previewArticles =
    listOf<Article>(
        Article(title = "5 Takeaways From Lorde’s New Album Virgin", feedName = "Pitchfork"),
        Article(
            title =
                "Big Thief’s Adrianne Lenker Announces Live Album, Shares Previously Unreleased Song “Happiness”",
            feedName = "Pitchfork",
        ),
        Article(title = "Haruomi Hosono on the Music That Made Him", feedName = "Pitchfork"),
        Article(title = "Faye Webster Announces Orchestral Tour Dates", feedName = "Pitchfork"),
//        Article(title = "研究人员发现玻璃瓶中的微塑料含量比塑料瓶中的还要高", feedName = "Pitchfork"),
        Article(
            title =
                "Big Thief’s Adrianne Lenker Announces Live Album, Shares Previously Unreleased Song “Happiness”",
            feedName = "Pitchfork",
        ),
        Article(title = "Faye Webster Announces Orchestral Tour Dates", feedName = "Pitchfork"),
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


sealed interface DataSource {}

sealed interface Theme {
    object Serif : Theme

    object SansSerif : Theme
}

data class Article(val title: String, val feedName: String)
