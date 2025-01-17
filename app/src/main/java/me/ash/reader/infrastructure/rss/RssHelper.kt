package me.ash.reader.infrastructure.rss

import android.content.Context
import android.util.Log
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndImageImpl
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dagger.hilt.android.qualifiers.ApplicationContext
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
import me.ash.reader.ui.ext.htmlFromMarkdown
import me.ash.reader.ui.ext.isFuture
import me.ash.reader.ui.ext.isNostrUri
import me.ash.reader.ui.ext.spacerDollar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import rust.nostr.sdk.Alphabet
import rust.nostr.sdk.Client
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindEnum
import rust.nostr.sdk.SingleLetterTag
import rust.nostr.sdk.TagKind
import rust.nostr.sdk.use
import java.io.InputStream
import java.time.Instant
import java.util.*
import javax.inject.Inject

val enclosureRegex = """<enclosure\s+url="([^"]+)"\s+type=".*"\s*/>""".toRegex()
val imgRegex = """img.*?src=(["'])((?!data).*?)\1""".toRegex(RegexOption.DOT_MATCHES_ALL)

/**
 * Some operations on RSS.
 */
class RssHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
    private val nostrClient: Client
) {

    @Throws(Exception::class)
    suspend fun searchFeed(feedLink: String): FetchedFeed? {
        return withContext(ioDispatcher) {
            if(feedLink.isNostrUri()) {
                NostrFeed.fetchFeedFrom(feedLink, nostrClient)
            }
            else {
                val parsedSyndFeed = SyndFeedInput()
                    .build(XmlReader(inputStream(okHttpClient, feedLink)))
                    .also {
                        it.icon = SyndImageImpl()
                        it.icon.link = queryRssIconLink(feedLink)
                        it.icon.url = it.icon.link
                    }
                SyndFeedDelegate(parsedSyndFeed)
            }
        }
    }

    @Throws(Exception::class)
    suspend fun parseFullContent(link: String, title: String): String {
        return withContext(ioDispatcher) {
            val response = response(okHttpClient, link)
            val content = response.body.string()
            val articleContent = Readability.parseToElement(content, link)
            articleContent?.run {
                val h1Element = articleContent.selectFirst("h1")
                if (h1Element != null && h1Element.hasText() && h1Element.text() == title) {
                    h1Element.remove()
                }
                articleContent.toString()
            } ?: ""
        }
    }

    suspend fun queryRssXml(
        feed: Feed,
        latestLink: String?,
        preDate: Date = Date(),
    ): List<Article> =
        try {
            val accountId = context.currentAccountId
            inputStream(okHttpClient, feed.url).use {
                SyndFeedInput(true, Locale.getDefault()).apply { isPreserveWireFeed = true }
                    .build(XmlReader(it))
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
            date = (syndEntry.publishedDate ?: syndEntry.updatedDate)?.takeIf { !it.isFuture(preDate) } ?: preDate,
            title = syndEntry.title.decodeHTML() ?: feed.name,
            author = syndEntry.author,
            rawDescription = content ?: desc ?: "",
            shortDescription = Readability.parseToText(desc ?: content, syndEntry.link).take(110),
            fullContent = content,
            img = findThumbnail(syndEntry) ?: findThumbnail(content ?: desc),
            link = syndEntry.link ?: "",
            updateAt = preDate,
        )
    }

    suspend fun syncNostrFeed(
        feed: Feed,
        latestLink: String?,
        preDate: Date = Date()
    ): List<Article> =
        try {
            val accountId = context.currentAccountId
            Client().use {
                val updatedFeed = NostrFeed.fetchFeedFrom(feed.url, it)
                updatedFeed.getArticles()
                    .map { buildArticleFromNostrEvent(feed, accountId, it, updatedFeed.getFeedAuthor(), preDate) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "syncNostrFeedNew[${feed.name}]: ${e.message}")
            listOf()
        }

    fun buildArticleFromNostrEvent(
        feed: Feed,
        accountId: Int,
        articleEvent: Event,
        authorName: String,
//        imageUrl: String,
        preDate: Date = Date()
    ): Article {
        val articleTitle = articleEvent.tags().find(TagKind.Title)?.content()
        val articleImage = articleEvent.tags().find(TagKind.Image)?.content()
        val articleSummary = articleEvent.tags().find(TagKind.Summary)?.content()
        val timeStamp = articleEvent.tags().find(TagKind.PublishedAt)?.content()?.toLong()
            ?: Instant.EPOCH.epochSecond
        val articleDate = Date.from(Instant.ofEpochSecond(timeStamp)).takeIf { !it.isFuture(preDate) } ?: preDate
        val articleNostrAddress =
            Coordinate(
                Kind.fromEnum(KindEnum.LongFormTextNote),
                articleEvent.author(),
                articleEvent.tags().find(
                    TagKind.SingleLetter(
                        SingleLetterTag.lowercase(Alphabet.D),
                    ),
                )?.content().toString(),
            ).toBech32()
        // Highlighter is a service for reading Nostr articles on the web.
        //For the external link, we can still give it a value of nostr:<articleAddress>
        val externalLink = "nostr:$articleNostrAddress"//""https://highlighter.com/a/$articleNostrAddress"
        val articleContent = articleEvent.content()
        val parsedContent = htmlFromMarkdown(articleContent)
        val actualContent = Readability.parseToText(
            parsedContent,
            uri = null
        )

        Log.i(
            "RLog",
            "Nostr Feed:\n" +
                    "name: ${feed.name}\n" +
                    "feedUrl: ${feed.url}\n" +
                    "url: ${externalLink}\n" +
                    "title: ${articleTitle}\n" +
                    "desc: ${articleSummary}\n" +
                    "content: ${articleContent}\n"
        )

        return Article(
            id = accountId.spacerDollar(articleEvent.id().toBech32()),
            accountId = accountId,
            feedId = feed.id,
            date = articleDate,
            title = articleTitle ?: feed.name,
            author = authorName,
            rawDescription = parsedContent,
            shortDescription = articleSummary ?: actualContent.take(110),
            fullContent = parsedContent,
            img = articleImage,
            link = externalLink,
            updateAt = articleDate
        )
    }

    fun findThumbnail(syndEntry: SyndEntry): String? {
        if (syndEntry.enclosures?.firstOrNull()?.url != null) {
            return syndEntry.enclosures.first().url
        }
        if (syndEntry.foreignMarkup.firstOrNull()?.name == "thumbnail") {
            return syndEntry.foreignMarkup.firstOrNull()?.attributes?.find { it.name == "url" }?.value
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
