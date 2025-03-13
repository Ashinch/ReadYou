package me.ash.reader.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import kotlinx.coroutines.runBlocking
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.preference.widget.ReadArticleDisplayOption
import me.ash.reader.infrastructure.preference.widget.WidgetPreferencesManager
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.page.common.ExtraName

class LatestArticleWidgetRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent
): RemoteViewsService.RemoteViewsFactory {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private val preferencesManager = WidgetPreferencesManager.getInstance(context)
    private val articleDao: ArticleDao = AndroidDatabase.getInstance(context).articleDao()
    private var articles: MutableMap<Int, List<ArticleWithFeed>> = mutableMapOf()
    private val feedIconLoader = FeedIconLoader(context)
    private val feedIconCache: MutableMap<String?, Bitmap?> = mutableMapOf()

    override fun onCreate() {
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }
    override fun getCount() = articles[appWidgetId]?.size ?: 0
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true
    override fun getViewTypeCount() = 1
    override fun onDestroy() {}

    override fun onDataSetChanged() {
        appWidgetId = intent.data?.schemeSpecificPart?.toInt() ?: AppWidgetManager.INVALID_APPWIDGET_ID

        runBlocking {
            // Get appropriate database query (ie, are we querying all articles or just a particular
            // group, etc)
            val groupToDisplayPref = preferencesManager.groupToDisplay.get(appWidgetId)
            val readArticleDisplayPref = preferencesManager.readArticleDisplay.getOption(appWidgetId)
            val query = if (groupToDisplayPref.isEmpty()) {
                // Display all articles (not just a group)
                if (readArticleDisplayPref == ReadArticleDisplayOption.HARD_HIDE) {
                    // Don't show read articles
                    articleDao.queryArticleWithFeedWhenIsUnread(context.currentAccountId, true)
                } else {
                    articleDao.queryArticleWithFeedWhenIsAll(context.currentAccountId)
                }
            } else {
                // Display only articles from group
                if (readArticleDisplayPref == ReadArticleDisplayOption.HARD_HIDE) {
                    articleDao.queryArticleWithFeedByGroupIdWhenIsUnread(context.currentAccountId, groupToDisplayPref, true)
                } else {
                    articleDao.queryArticleWithFeedByGroupIdWhenIsAll(context.currentAccountId, groupToDisplayPref)

                }
            }

            // Fetch latest articles from database
            articles[appWidgetId] = loadArticles(
                query,
                preferencesManager.maxLatestArticleCount.get(appWidgetId)
            )

            // Load icons
            for (awf in articles.getOrPut(appWidgetId) { emptyList() }) {
                if (!feedIconCache.contains(awf.feed.icon)) {
                    feedIconCache[awf.feed.icon] = feedIconLoader.asyncLoadFeedIcon(
                        feedName = awf.feed.name,
                        iconUrl = awf.feed.icon,
                        widgetId = appWidgetId
                    )
                }
            }
        }
    }

    private fun getArticleRemoteViews(awf: ArticleWithFeed): RemoteViews {
        val fillInIntent = Intent().apply {
            putExtra(
                ExtraName.ARTICLE_ID,
                awf.article.id
            )
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val showIcon = preferencesManager.showFeedIcon.getCachedOrDefault(appWidgetId)
        val layout = if (showIcon) R.layout.article_with_icon else R.layout.article_without_icon
        val remoteViews = RemoteViews(context.packageName, layout).apply {
                setTextViewText(R.id.article_summary, styledArticleSummary(awf))
                setOnClickFillInIntent(R.id.article, fillInIntent)
        }
        if (showIcon) {
            remoteViews.setImageViewBitmap(R.id.article_feed_icon, feedIconCache[awf.feed.icon])
            remoteViews.setContentDescription(R.id.article_feed_icon, awf.feed.name)
        }
        return remoteViews
    }

    override fun getViewAt(position: Int): RemoteViews? {
        articles[appWidgetId]?.let {
            return getArticleRemoteViews(it[position])
        }
        return null
    }

    override fun getLoadingView(): RemoteViews? = null

    private fun styledArticleSummary(awf: ArticleWithFeed): SpannableString {
        var unstyled = "${awf.article.title} ${awf.article.shortDescription}"
        var boldLen = awf.article.title.length
        val showFeedName = runBlocking {
            preferencesManager.showFeedName.get(appWidgetId)
        }
        if (showFeedName) {
            unstyled = "(${awf.feed.name}) $unstyled"
            boldLen += awf.feed.name.length + 3
        }
        val summary = SpannableString(unstyled)
        summary.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            boldLen,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val readArticleDisplay = runBlocking {
            preferencesManager.readArticleDisplay.getOption(appWidgetId)
        }
        if (readArticleDisplay == ReadArticleDisplayOption.SOFT_HIDE && !awf.article.isUnread) {
            summary.setSpan(
                ForegroundColorSpan(Color.DKGRAY),
                0,
                unstyled.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

        }
        return summary
    }
}

private suspend fun loadArticles(
    pagingSource: PagingSource<Int, ArticleWithFeed>,
    count: Int
): List<ArticleWithFeed> {
    val result = mutableListOf<ArticleWithFeed>()
    var nextKey: Int? = 0

    while (nextKey != null) {
        val loadResult = pagingSource.load(
            LoadParams.Refresh(
                key = nextKey,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        when (loadResult) {
            is PagingSource.LoadResult.Page -> {
                result.addAll(loadResult.data)
                if (result.size >= count) {
                    return result.take(count)
                }
                nextKey = loadResult.nextKey // Set the next key to continue loading if available
            }
            else -> {
                // Handle the error if needed
                break
            }
        }
    }

    return result
}
