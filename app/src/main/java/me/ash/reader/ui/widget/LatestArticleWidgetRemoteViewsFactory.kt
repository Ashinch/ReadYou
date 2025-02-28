package me.ash.reader.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import kotlinx.coroutines.runBlocking
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.R
import me.ash.reader.infrastructure.db.AndroidDatabase

class LatestArticleWidgetRemoteViewsFactory(
    private val context: Context,
): RemoteViewsService.RemoteViewsFactory {

    private val articleDao: ArticleDao = AndroidDatabase.getInstance(context).articleDao()
    private var articles: List<ArticleWithFeed> = emptyList()
    private val feedIconLoader = FeedIconLoader(context)
    private val feedIconCache: MutableMap<String?, Bitmap?> = mutableMapOf()

    override fun onCreate() {}
    override fun getCount() = articles.size
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true
    override fun getViewTypeCount() = 1
    override fun onDestroy() {}

    override fun onDataSetChanged() {
        Log.d("LatestArticles", "onDataSetChanged called")
        runBlocking {
            articles = loadArticles(articleDao.queryArticleWithFeedWhenIsAll(context.currentAccountId), 20)
            for (awf in articles) {
                if (!feedIconCache.contains(awf.feed.icon)) {
                    feedIconCache[awf.feed.icon] = feedIconLoader.asyncLoadFeedIcon(
                        feedName = awf.feed.name,
                        iconUrl = awf.feed.icon
                    )
                }
            }
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        val awf = articles[position]
        val remoteViews = RemoteViews(context.packageName, R.layout.article).apply {
            setTextViewText(R.id.article_summary, styledArticleSummary(awf))
            setImageViewBitmap(R.id.article_feed_icon, feedIconCache[awf.feed.icon])
        }

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null
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

private fun styledArticleSummary(awf: ArticleWithFeed): SpannableString {
    val summary = SpannableString("${awf.article.title} ${awf.article.shortDescription}")
    val boldSpan = StyleSpan(Typeface.BOLD)
    summary.setSpan(boldSpan, 0, awf.article.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return summary
}