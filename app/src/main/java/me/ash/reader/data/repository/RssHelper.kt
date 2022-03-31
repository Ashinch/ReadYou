package me.ash.reader.data.repository

import android.content.Context
import android.text.Html
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.currentAccountId
import me.ash.reader.data.article.Article
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.feed.FeedWithArticle
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.source.RssNetworkDataSource
import me.ash.reader.spacerDollar
import net.dankito.readability4j.Readability4J
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RssHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val rssNetworkDataSource: RssNetworkDataSource,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
) {
    @Throws(Exception::class)
    suspend fun searchFeed(feedLink: String): FeedWithArticle {
        return withContext(dispatcherIO) {
            val accountId = context.currentAccountId
            val parseRss = rssNetworkDataSource.parseRss(feedLink)
            val feed = Feed(
                id = accountId.spacerDollar(UUID.randomUUID().toString()),
                name = parseRss.title!!,
                url = feedLink,
                groupId = "",
                accountId = accountId,
            )
            FeedWithArticle(feed, queryRssXml(feed))
        }
    }

    fun parseDescriptionContent(link: String, content: String): String {
        val readability4J: Readability4J = Readability4JExtended(link, content)
        val article = readability4J.parse()
        val element = article.articleContent
        return element.toString()
    }

    @Throws(Exception::class)
    suspend fun parseFullContent(link: String, title: String): String {
        return withContext(dispatcherIO) {
            val response = OkHttpClient()
                .newCall(Request.Builder().url(link).build())
                .execute()
            val content = response.body!!.string()
            val readability4J: Readability4J =
                Readability4JExtended(link, content)
            val articleContent = readability4J.parse().articleContent
            if (articleContent == null) {
                ""
            } else {
                val h1Element = articleContent.selectFirst("h1")
                if (h1Element != null && h1Element.hasText() && h1Element.text() == title) {
                    h1Element.remove()
                }
                articleContent.toString()
            }
        }
    }

    @Throws(Exception::class)
    suspend fun queryRssXml(
        feed: Feed,
        latestLink: String? = null,
    ): List<Article> {
        return withContext(dispatcherIO) {
            val a = mutableListOf<Article>()
            val accountId = context.currentAccountId
            val parseRss = rssNetworkDataSource.parseRss(feed.url)
            parseRss.items.forEach {
                if (latestLink != null && latestLink == it.link) return@withContext a
                Log.i("RLog", "request rss ${feed.name}: ${it.title}")
                a.add(
                    Article(
                        id = accountId.spacerDollar(UUID.randomUUID().toString()),
                        accountId = accountId,
                        feedId = feed.id,
                        date = it.publishDate.toString().let {
                            try {
                                Date(it)
                            } catch (e: IllegalArgumentException) {
                                parseDate(it) ?: Date()
                            }
                        },
                        title = Html.fromHtml(it.title.toString()).toString(),
                        author = it.author,
                        rawDescription = it.description.toString(),
                        shortDescription = (Readability4JExtended("", it.description.toString())
                            .parse().textContent ?: "").take(100).trim(),
                        link = it.link ?: "",
                    )
                )
            }
            a
        }
    }

    @Throws(Exception::class)
    suspend fun queryRssIcon(
        feedDao: FeedDao,
        feed: Feed,
        articleLink: String,
    ) {
        withContext(dispatcherIO) {
            val execute = OkHttpClient()
                .newCall(Request.Builder().url(articleLink).build())
                .execute()
            val content = execute.body!!.string()
            val regex =
                Regex("""<link(.+?)rel="shortcut icon"(.+?)href="(.+?)"""")
            var iconLink = regex
                .find(content)
                ?.groups?.get(3)
                ?.value
            Log.i("rlog", "queryRssIcon: $iconLink")
            if (iconLink != null) {
                if (iconLink.startsWith("//")) {
                    iconLink = "http:$iconLink"
                }
                if (iconLink.startsWith("/")) {
                    val domainRegex =
                        Regex("""http(s)?://(([\w-]+\.)+\w+(:\d{1,5})?)""")
                    iconLink =
                        "http://${domainRegex.find(articleLink)?.groups?.get(2)?.value}$iconLink"
                }
                saveRssIcon(feedDao, feed, iconLink)
            } else {
//                saveRssIcon(feedDao, feed, "")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun saveRssIcon(feedDao: FeedDao, feed: Feed, iconLink: String) {
        withContext(dispatcherIO) {
            val response = OkHttpClient()
                .newCall(Request.Builder().url(iconLink).build())
                .execute()
            feedDao.update(
                feed.apply {
                    icon = response.body!!.bytes()
                }
            )
        }
    }

    private fun parseDate(
        inputDate: String, patterns: Array<String?> = arrayOf(
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "yyyyMMdd",
            "yyyy/MM/dd",
            "yyyy年MM月dd日",
            "yyyy MM dd"
        )
    ): Date? {
        val df = SimpleDateFormat()
        for (pattern in patterns) {
            df.applyPattern(pattern)
            df.isLenient = false
            val date = df.parse(inputDate, ParsePosition(0))
            if (date != null) {
                return date
            }
        }
        return null
    }
}