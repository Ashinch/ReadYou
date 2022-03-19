package me.ash.reader.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.DataStoreKeys
import me.ash.reader.data.article.Article
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.feed.FeedDao
import me.ash.reader.data.feed.FeedWithArticle
import me.ash.reader.data.source.RssNetworkDataSource
import me.ash.reader.dataStore
import me.ash.reader.get
import me.ash.reader.spacerDollar
import net.dankito.readability4j.Readability4J
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.*
import java.io.IOException
import java.util.*
import javax.inject.Inject

class RssHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val rssNetworkDataSource: RssNetworkDataSource,
) {
    @Throws(Exception::class)
    suspend fun searchFeed(feedLink: String): FeedWithArticle {
        val accountId = context.dataStore.get(DataStoreKeys.CurrentAccountId) ?: 0
        val parseRss = rssNetworkDataSource.parseRss(feedLink)
        val feed = Feed(
            id = accountId.spacerDollar(UUID.randomUUID().toString()),
            name = parseRss.title!!,
            url = feedLink,
            groupId = "",
            accountId = accountId,
        )
        val articles = mutableListOf<Article>()
        parseRss.items.forEach {
            articles.add(
                Article(
                    id = accountId.spacerDollar(UUID.randomUUID().toString()),
                    accountId = accountId,
                    feedId = feed.id,
                    date = Date(it.publishDate.toString()),
                    title = it.title.toString(),
                    author = it.author,
                    rawDescription = it.description.toString(),
                    shortDescription = (Readability4JExtended("", it.description.toString())
                        .parse().textContent ?: "").take(100).trim(),
                    link = it.link ?: "",
                )
            )
        }
        return FeedWithArticle(feed, articles)
    }

    fun parseDescriptionContent(link: String, content: String): String {
        val readability4J: Readability4J = Readability4JExtended(link, content)
        val article = readability4J.parse()
        val element = article.articleContent
        return element.toString()
    }

    fun parseFullContent(link: String, title: String, callback: (String) -> Unit) {
        OkHttpClient()
            .newCall(Request.Builder().url(link).build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val content = response.body?.string()
                    val readability4J: Readability4J =
                        Readability4JExtended(link, content ?: "")
                    val articleContent = readability4J.parse().articleContent
                    if (articleContent == null) {
                        callback("")
                    } else {
                        val h1Element = articleContent.selectFirst("h1")
                        if (h1Element != null && h1Element.hasText() && h1Element.text() == title) {
                            h1Element.remove()
                        }
                        callback(articleContent.toString())
                    }
                }
            })
    }

    suspend fun queryRssXml(
        rssNetworkDataSource: RssNetworkDataSource,
        accountId: Int,
        feed: Feed,
        latestTitle: String? = null,
    ): List<Article> {
        val a = mutableListOf<Article>()
        try {
            val parseRss = rssNetworkDataSource.parseRss(feed.url)
            parseRss.items.forEach {
                if (latestTitle != null && latestTitle == it.title) return a
                Log.i("RLog", "request rss ${feed.name}: ${it.title}")
                a.add(
                    Article(
                        id = accountId.spacerDollar(UUID.randomUUID().toString()),
                        accountId = accountId,
                        feedId = feed.id,
                        date = Date(it.publishDate.toString()),
                        title = it.title.toString(),
                        author = it.author,
                        rawDescription = it.description.toString(),
                        shortDescription = (Readability4JExtended("", it.description.toString())
                            .parse().textContent ?: "").take(100).trim(),
                        link = it.link ?: "",
                    )
                )
            }
            return a
        } catch (e: Exception) {
            Log.e("RLog", "error ${feed.name}: ${e.message}")
            return a
        }
    }

    suspend fun queryRssIcon(
        feedDao: FeedDao,
        feed: Feed,
        articleLink: String?,
    ) {
        try {
            if (articleLink == null) return
            val execute = OkHttpClient()
                .newCall(Request.Builder().url(articleLink).build())
                .execute()
            val content = execute.body?.string()
            val regex =
                Regex("""<link(.+?)rel="shortcut icon"(.+?)href="(.+?)"""")
            if (content != null) {
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
//                    saveRssIcon(feedDao, feed, "")
                }
            } else {
//                saveRssIcon(feedDao, feed, "")
            }
        } catch (e: Exception) {
            Log.e("RLog", "queryRssIcon: ${e.message}")
        }
    }

    private suspend fun saveRssIcon(feedDao: FeedDao, feed: Feed, iconLink: String) {
        val execute = OkHttpClient()
            .newCall(Request.Builder().url(iconLink).build())
            .execute()
        feedDao.update(
            feed.apply {
                icon = execute.body?.bytes()
            }
        )
    }
}