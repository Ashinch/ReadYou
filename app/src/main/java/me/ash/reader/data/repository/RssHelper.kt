package me.ash.reader.data.repository

import android.content.Context
import android.text.Html
import android.util.Log
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.entity.Article
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.FeedWithArticle
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.spacerDollar
import net.dankito.readability4j.Readability4J
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RssHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
) {
    @Throws(Exception::class)
    suspend fun searchFeed(feedLink: String): FeedWithArticle {
        return withContext(dispatcherIO) {
            val accountId = context.currentAccountId
            val parseRss: SyndFeed = SyndFeedInput().build(XmlReader(URL(feedLink)))
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
            val response = okHttpClient
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
            val parseRss: SyndFeed = SyndFeedInput().build(XmlReader(URL(feed.url)))
            parseRss.entries.forEach {
                if (latestLink != null && latestLink == it.link) return@withContext a
                val desc = it.description?.value
                val content = it.contents
                    .takeIf { it.isNotEmpty() }
                    ?.let { it.joinToString("\n") { it.value } }
                Log.i(
                    "RLog",
                    "request rss:\n" +
                            "name: ${feed.name}\n" +
                            "feedUrl: ${feed.url}\n" +
                            "url: ${it.link}\n" +
                            "title: ${it.title}\n" +
                            "desc: ${desc}\n" +
                            "content: ${content}\n"
                )
                a.add(
                    Article(
                        id = accountId.spacerDollar(UUID.randomUUID().toString()),
                        accountId = accountId,
                        feedId = feed.id,
                        date = it.publishedDate ?: it.updatedDate ?: Date(),
                        title = Html.fromHtml(it.title.toString()).toString(),
                        author = it.author,
                        rawDescription = (desc ?: content) ?: "",
                        shortDescription = (Readability4JExtended("", desc ?: content ?: "")
                            .parse().textContent ?: "")
                            .take(100)
                            .trim(),
                        fullContent = content,
                        img = findImg((desc ?: content) ?: ""),
                        link = it.link ?: "",
                    )
                )
            }
            a
        }
    }

    private fun findImg(rawDescription: String): String? {
        // From: https://gitlab.com/spacecowboy/Feeder
        // Using negative lookahead to skip data: urls, being inline base64
        // And capturing original quote to use as ending quote
        val regex = """img.*?src=(["'])((?!data).*?)\1""".toRegex(RegexOption.DOT_MATCHES_ALL)
        // Base64 encoded images can be quite large - and crash database cursors
        return regex.find(rawDescription)?.groupValues?.get(2)?.takeIf { !it.startsWith("data:") }
    }

    @Throws(Exception::class)
    suspend fun queryRssIcon(
        feedDao: FeedDao,
        feed: Feed,
        articleLink: String,
    ) {
        withContext(dispatcherIO) {
            val domainRegex = Regex("(http|https)://(www.)?(\\w+(\\.)?)+")
            val request = OkHttpClient()
                .newCall(Request.Builder().url(articleLink).build())
                .execute()
            val content = request.body!!.string()
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
                    iconLink = "${domainRegex.find(articleLink)?.value}$iconLink"
                }
                saveRssIcon(feedDao, feed, iconLink)
            } else {
                domainRegex.find(articleLink)?.value?.let {
                    Log.i("RLog", "favicon: ${it}")
                    val request = OkHttpClient()
                        .newCall(Request.Builder().url("$it/favicon.ico").build())
                        .execute()
                    if (request.isSuccessful) {
                        saveRssIcon(feedDao, feed, it)
                    }
                }
            }
        }
    }

    private suspend fun saveRssIcon(feedDao: FeedDao, feed: Feed, iconLink: String) {
        feedDao.update(
            feed.apply {
                icon = iconLink
            }
        )
    }
}