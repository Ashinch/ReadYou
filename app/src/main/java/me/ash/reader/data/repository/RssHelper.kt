package me.ash.reader.data.repository

import android.content.Context
import android.text.Html
import android.util.Log
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.data.dao.FeedDao
import me.ash.reader.data.model.article.Article
import me.ash.reader.data.model.feed.Feed
import me.ash.reader.data.model.feed.FeedWithArticle
import me.ash.reader.data.module.IODispatcher
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.spacerDollar
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import java.io.InputStream
import java.util.*
import javax.inject.Inject

/**
 * Some operations on RSS.
 */
class RssHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
) {

    @Throws(Exception::class)
    suspend fun searchFeed(feedLink: String): FeedWithArticle {
        return withContext(ioDispatcher) {
            val accountId = context.currentAccountId
            val syndFeed = SyndFeedInput().build(XmlReader(inputStream(okHttpClient, feedLink)))
            val feed = Feed(
                id = accountId.spacerDollar(UUID.randomUUID().toString()),
                name = syndFeed.title!!,
                url = feedLink,
                groupId = "",
                accountId = accountId,
            )
            val list = syndFeed.entries.map { article(feed, context.currentAccountId, it) }
            FeedWithArticle(feed, list)
        }
    }

    @Throws(Exception::class)
    suspend fun parseFullContent(link: String, title: String): String {
        return withContext(ioDispatcher) {
            val response = response(okHttpClient, link)
            val content = response.body.string()
            val readability4J = Readability4JExtended(link, content)
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

    suspend fun queryRssXml(
        feed: Feed,
        latestLink: String?,
    ): List<Article> =
        try {
            val accountId = context.currentAccountId
            inputStream(okHttpClient, feed.url).use {
                SyndFeedInput().apply { isPreserveWireFeed = true }
                    .build(XmlReader(it))
                    .entries
                    .asSequence()
                    .takeWhile { latestLink == null || latestLink != it.link }
                    .map { article(feed, accountId, it) }
                    .toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "queryRssXml[${feed.name}]: ${e.message}")
            listOf()
        }

    private fun article(
        feed: Feed,
        accountId: Int,
        syndEntry: SyndEntry,
    ): Article {
        val desc = syndEntry.description?.value
        val content = syndEntry.contents
            .takeIf { it.isNotEmpty() }
            ?.let { it.joinToString("\n") { it.value } }
        Log.i(
            "RLog",
            "request rss:\n" +
                    "name: ${feed.name}\n" +
                    "feedUrl: ${feed.url}\n" +
                    "url: ${syndEntry.link}\n" +
                    "title: ${syndEntry.title}\n" +
                    "desc: ${desc}\n" +
                    "content: ${content}\n"
        )
        return Article(
            id = accountId.spacerDollar(UUID.randomUUID().toString()),
            accountId = accountId,
            feedId = feed.id,
            date = syndEntry.publishedDate ?: syndEntry.updatedDate ?: Date(),
            title = Html.fromHtml(syndEntry.title.toString()).toString(),
            author = syndEntry.author,
            rawDescription = (content ?: desc) ?: "",
            shortDescription = (Readability4JExtended("", desc ?: content ?: "")
                .parse().textContent ?: "")
                .take(110)
                .trim(),
            fullContent = content,
            img = findImg((content ?: desc) ?: ""),
            link = syndEntry.link ?: "",
            updateAt = Date(),
        )
    }

    fun findImg(rawDescription: String): String? {
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
        withContext(ioDispatcher) {
            val domainRegex = Regex("(http|https)://(www.)?(\\w+(\\.)?)+")
            val request = response(okHttpClient, articleLink)
            val content = request.body.string()
            val regex = Regex("""<link(.+?)rel="shortcut icon"(.+?)href="(.+?)"""")
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
                    if (response(okHttpClient, "$it/favicon.ico").isSuccessful) {
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

    private suspend fun inputStream(
        client: OkHttpClient,
        url: String,
    ): InputStream = response(client, url).body.byteStream()

    private suspend fun response(
        client: OkHttpClient,
        url: String,
    ) = client.newCall(Request.Builder().url(url).build()).executeAsync()
}
