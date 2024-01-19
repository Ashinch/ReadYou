package me.ash.reader.infrastructure.rss.provider.greader

import com.google.gson.annotations.SerializedName

object GoogleReaderDTO {

    data class GReaderError(
        @SerializedName("errors") val errors: List<String>,
    )

    /**
     * @sample
     *{
     *     "SID": "demo/718*********************************7fa",
     *     "LSID": "demo/718*********************************7fa",
     *     "Auth": "demo/718*********************************7fa"
     * }
     */
    data class MinifluxAuthData(
        val SID: String?,
        val LSID: String?,
        val Auth: String?,
    )

    /**
     * @link reader/api/0/user-info?output=json
     * @sample
     * {
     *     "userId": "demo",
     *     "userName": "demo",
     *     "userProfileId": "demo",
     *     "userEmail": ""
     * }
     */
    data class User(
        val userId: String?,
        val userName: String?,
        val userProfileId: String?,
        val userEmail: String?,
    )

    /**
     * @link reader/api/0/subscription/list?output=json
     * @sample
     * {
     *     "subscriptions": [
     *         {
     *             "id": "feed/3",
     *             "title": "Fedora Magazine",
     *             "categories": [
     *                 {
     *                     "id": "user/-/label/Blogs",
     *                     "label": "Blogs"
     *                 }
     *             ],
     *             "url": "http://fedoramagazine.org/feed/",
     *             "htmlUrl": "http://fedoramagazine.org/",
     *             "iconUrl": "https://demo.freshrss.org/f.php?f2b1439b"
     *         }
     *     ]
     * }
     */
    data class SubscriptionList(
        val subscriptions: List<Feed>,
    )

    data class Feed(
        val id: String?,
        val title: String?,
        val categories: List<Category>?,
        val url: String?,
        val htmlUrl: String?,
        val iconUrl: String?,
        val sortid: String?,
    )

    data class Category(
        val id: String?,
        val label: String?,
    )

    /**
     * @link reader/api/0/subscription/quickadd?quickadd=https%3A%2F%2Fblog.com%2Ffeed
     * @sample
     * {
     *     "numResults": 1,
     *     "query": "https://blog.com/feed",
     *     "streamId": "feed/10",
     *     "streamName": "blog"
     * }
     *
     */
    data class QuickAddFeed(
        val numResults: Long?,
        val query: String?,
        val streamId: String?,
        val streamName: String?,
    )

    /**
     * @link reader/api/0/stream/items/ids?s=user/-/state/com.google/starred&output=json
     * @sample
     * {
     *     "itemRefs": [
     *         {
     *             "id": "1705042807944418"
     *         }
     *     ]
     * }
     */
    data class ItemIds(
        val itemRefs: List<Item>?,
    )

    /**
     * @link reader/api/0/stream/items/contents
     * @sample
     * {
     *     "id": "user/-/state/com.google/reading-list",
     *     "updated": 1705045799,
     *     "items": [
     *         {
     *             "id": "tag:google.com,2005:reader/item/00060eba36e4f4e1",
     *             "crawlTimeMsec": "1705042807944",
     *             "timestampUsec": "1705042807944417",
     *             "published": 1704982200,
     *             "title": "Andy Wingo: micro macro story time",
     *             "canonical": [
     *                 {
     *                     "href": "https://wingolog.org/archives/2024/01/11/micro-macro-story-time"
     *                 }
     *             ],
     *             "alternate": [
     *                 {
     *                     "href": "https://wingolog.org/archives/2024/01/11/micro-macro-story-time"
     *                 }
     *             ],
     *             "categories": [
     *                 "user/-/state/com.google/reading-list",
     *                 "user/-/label/Blogs",
     *                 "user/-/state/com.google/read"
     *             ],
     *             "origin": {
     *                 "streamId": "feed/2",
     *                 "htmlUrl": "https://planet.gnome.org/",
     *                 "title": "Planet GNOME"
     *             },
     *             "summary": {
     *                 "content": "<div><p>Today, a tiny tale</p></div>",
     *                 "expand": "\ndoesn’t sound fancy enough.  In a way it’s similar to the original SSA\ndevelopers <a href=\"https://wingolog.org/archives/2023/05/20/approaching-cps-soup\">thinking that ",
     *                 "phony functions": " wouldn’t get\npublished</a>.</p><p>So Dybvig calls the expansion function ",
     *                 "χ": ", because the Greek chi looks\nlike the X in ",
     *                 "expand": ".  Fine for the paper, whatever paper that might\nbe, but then in <tt>psyntax</tt>, there are all these functions named\n<tt>chi</tt> and <tt>chi-lambda</tt> and all sorts of nonsense.</p><p>In early years I was often confused by these names; I wasn’t in on the\npun, and I didn’t feel like I had enough responsibility for this code to\nthink what the name should be.  I finally broke down and changed all\ninstances of ",
     *                 "chi": " to ",
     *                 "expand": " back in 2011, and never looked back.</p><p>Anyway, this is a story with a very specific moral: don’t name your\nfunctions <tt>chi</tt>.</p></div>"
     *             }
     *         }
     *     ]
     * }
     */
    data class ItemsContents(
        val id: String? = null,
        val updated: Long? = null,
        val items: List<Item>? = null,
    )

    data class Item(
        val id: String? = null,
        val crawlTimeMsec: String? = null,
        val timestampUsec: String? = null,
        val published: Long? = null,
        val title: String? = null,
        val summary: Summary? = null,
        val categories: List<String>? = null,
        val origin: OriginItem? = null,
        val author: String? = null,
        val canonical: List<CanonicalItem>? = null,
        val alternate: List<CanonicalItem>? = null,
    )

    data class Summary(
        val content: String? = null,
    )

    data class CanonicalItem(
        val href: String? = null,
    )

    data class OriginItem(
        val streamId: String? = null,
        val htmlUrl: String? = null,
        val title: String? = null,
    )
}
