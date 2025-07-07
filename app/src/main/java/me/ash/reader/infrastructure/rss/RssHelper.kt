package me.ash.reader.infrastructure.rss

import android.content.Context
import android.util.Log
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndImageImpl
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.html.Readability
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.extractDomain
import me.ash.reader.ui.ext.isFuture
import me.ash.reader.ui.ext.spacerDollar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import okhttp3.internal.commonIsSuccessful
import okio.IOException
import org.jsoup.Jsoup

val enclosureRegex = """<enclosure\s+url="([^"]+)"\s+type=".*"\s*/>""".toRegex()
val imgRegex = """img.*?src=(["'])((?!data).*?)\1""".toRegex(RegexOption.DOT_MATCHES_ALL)

/** Some operations on RSS. */
class RssHelper
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
) {

    @Throws(Exception::class)
    suspend fun searchFeed(feedLink: String): SyndFeed {
        return withContext(ioDispatcher) {
            val response = response(okHttpClient, feedLink)
            val contentType = response.header("Content-Type")
            response.body.byteStream().use { inputStream ->
                SyndFeedInput().build(XmlReader(inputStream, contentType)).also {
                    it.icon = SyndImageImpl()
                    it.icon.link = queryRssIconLink(feedLink)
                    it.icon.url = it.icon.link
                }
            }
        }
    }

    @Throws(Exception::class)
    suspend fun parseFullContent(link: String, title: String): String {
        return withContext(ioDispatcher) {
            val response = response(okHttpClient, link)
            if (response.commonIsSuccessful) {
                val responseBody = response.body
                val charset = responseBody.contentType()?.charset()
                val content =
                    responseBody.source().use {
                        if (charset != null) {
                            return@use it.readString(charset)
                        }

                        val peekContent = it.peek().readString(Charsets.UTF_8)

                        val charsetFromMeta =
                            runCatching {
                                    val element =
                                        Jsoup.parse(peekContent, link)
                                            .selectFirst("meta[http-equiv=content-type]")
                                    return@runCatching if (element == null) Charsets.UTF_8
                                    else {
                                        element
                                            .attr("content")
                                            .substringAfter("charset=")
                                            .removeSurrounding("\"")
                                            .lowercase()
                                            .let { Charset.forName(it) }
                                    }
                                }
                                .getOrDefault(Charsets.UTF_8)

                        if (charsetFromMeta == Charsets.UTF_8) {
                            peekContent
                        } else {
                            it.readString(charsetFromMeta)
                        }
                    }

                val articleContent = Readability.parseToElement(content, link)
                articleContent?.let {
                    val h1Element = articleContent.selectFirst("h1")
                    if (h1Element != null && h1Element.hasText() && h1Element.text() == title) {
                        h1Element.remove()
                    }
                    articleContent.toString()
                } ?: throw IOException("articleContent is null")
            } else throw IOException(response.message)
        }
    }

    suspend fun queryRssXml(
        feed: Feed,
        latestLink: String?,
        preDate: Date = Date(),
    ): List<Article> =
        try {
            val accountId = context.currentAccountId
            val response = response(okHttpClient, feed.url)
            val contentType = response.header("Content-Type")
            response.body.byteStream().use { inputStream ->
                SyndFeedInput()
                    .apply { isPreserveWireFeed = true }
                    .build(XmlReader(inputStream, contentType))
                    .entries
                    .asSequence()
                    .takeWhile { latestLink == null || latestLink != it.link }
                    .map { buildArticleFromSyndEntry(feed, accountId, it, preDate) }
                    .toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "queryRssXml[${feed.name}]: ${e.message}")
            listOf()
        }

    fun buildArticleFromSyndEntry(
        feed: Feed,
        accountId: Int,
        syndEntry: SyndEntry,
        preDate: Date = Date(),
    ): Article {
        val desc = syndEntry.description?.value
        val content =
            syndEntry.contents
                .takeIf { it.isNotEmpty() }
                ?.let { it.joinToString("\n") { it.value } }
        //        Log.i(
        //            "RLog",
        //            "request rss:\n" +
        //                    "name: ${feed.name}\n" +
        //                    "feedUrl: ${feed.url}\n" +
        //                    "url: ${syndEntry.link}\n" +
        //                    "title: ${syndEntry.title}\n" +
        //                    "desc: ${desc}\n" +
        //                    "content: ${content}\n"
        //        )
        return Article(
            id = accountId.spacerDollar(UUID.randomUUID().toString()),
            accountId = accountId,
            feedId = feed.id,
            date =
                (syndEntry.publishedDate ?: syndEntry.updatedDate)?.takeIf { !it.isFuture(preDate) }
                    ?: preDate,
            title = syndEntry.title.decodeHTML() ?: feed.name,
            author = syndEntry.author,
            rawDescription = content ?: desc ?: "",
            shortDescription = Readability.parseToText(desc ?: content, syndEntry.link).take(280),
            //            fullContent = content,
            img = findThumbnail(syndEntry) ?: findThumbnail(content ?: desc),
            link = syndEntry.link ?: "",
            updateAt = preDate,
        )
    }

    fun findThumbnail(syndEntry: SyndEntry): String? {
        if (syndEntry.enclosures?.firstOrNull()?.url != null) {
            return syndEntry.enclosures.first().url
        }
        if (syndEntry.foreignMarkup.firstOrNull()?.name == "thumbnail") {
            return syndEntry.foreignMarkup
                .firstOrNull()
                ?.attributes
                ?.find { it.name == "url" }
                ?.value
        }
        return null
    }

    fun findThumbnail(text: String?): String? {
        text ?: return null
        val enclosure = enclosureRegex.find(text)?.groupValues?.get(1)
        if (enclosure?.isNotBlank() == true) {
            return enclosure
        }
        // From https://gitlab.com/spacecowboy/Feeder
        // Using negative lookahead to skip data: urls, being inline base64
        // And capturing original quote to use as ending quote
        // Base64 encoded images can be quite large - and crash database cursors
        return imgRegex.find(text)?.groupValues?.get(2)?.takeIf { !it.startsWith("data:") }
    }

    suspend fun queryRssIconLink(feedLink: String?): String? {
        if (feedLink.isNullOrEmpty()) return null
        val iconFinder = BestIconFinder(okHttpClient)
        val domain = feedLink.extractDomain()
        return iconFinder.findBestIcon(domain ?: feedLink).also {
            Log.i("RLog", "queryRssIconByLink: get $it from $domain")
        }
    }

    suspend fun saveRssIcon(feedDao: FeedDao, feed: Feed, iconLink: String) {
        feedDao.update(feed.copy(icon = iconLink))
    }

    private suspend fun response(client: OkHttpClient, url: String): okhttp3.Response =
        client.newCall(Request.Builder().url(url).build()).executeAsync()
}
