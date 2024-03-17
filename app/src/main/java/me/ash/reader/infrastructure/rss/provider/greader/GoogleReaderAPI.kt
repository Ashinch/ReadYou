package me.ash.reader.infrastructure.rss.provider.greader

import me.ash.reader.infrastructure.di.USER_AGENT_STRING
import me.ash.reader.infrastructure.exception.GoogleReaderAPIException
import me.ash.reader.infrastructure.exception.RetryException
import me.ash.reader.infrastructure.rss.provider.ProviderAPI
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.executeAsync
import java.util.concurrent.ConcurrentHashMap

class GoogleReaderAPI private constructor(
    private val serverUrl: String,
    private val username: String,
    private val password: String,
    private val httpUsername: String? = null,
    private val httpPassword: String? = null,
) : ProviderAPI() {

    enum class Stream(val tag: String) {
        ALL_ITEMS("user/-/state/com.google/reading-list"),
        READ("user/-/state/com.google/read"),
        STARRED("user/-/state/com.google/starred"),
        LIKE("user/-/state/com.google/like"),
        BROADCAST("user/-/state/com.google/broadcast"),
        ;
    }

    private data class AuthData(
        var clientLoginToken: String?,
        var actionToken: String?,
    )

    private val authData = AuthData(null, null)

    suspend fun validCredentials(): Boolean {
        reAuthenticate()
        return authData.clientLoginToken?.isNotEmpty() ?: false
    }

    private suspend fun reAuthenticate() {
        // Get client login token
        val clResponse = client.newCall(
            Request.Builder()
                .url("${serverUrl}accounts/ClientLogin")
                .header("User-Agent", USER_AGENT_STRING)
                .post(FormBody.Builder()
                    .add("output", "json")
                    .add("Email", username)
                    .add("Passwd", password)
                    .add("client", "ReadYou")
                    .add("accountType", "HOSTED_OR_GOOGLE")
                    .add("service", "reader")
                    .build())
                .build())
            .executeAsync()

        val clBody = clResponse.body.string()
        when (clResponse.code) {
            400 -> throw GoogleReaderAPIException("BadRequest for CL Token")
            401 -> throw GoogleReaderAPIException("Unauthorized for CL Token")
            !in 200..299 -> {
                throw GoogleReaderAPIException(clBody)
            }
        }

        authData.clientLoginToken = try {
            toDTO<GoogleReaderDTO.MinifluxAuthData>(clBody).Auth
        } catch (ignore: Exception) {
            clBody
                .split("\n")
                .find { it.startsWith("Auth=") }
                ?.substring(5)
                ?: throw GoogleReaderAPIException("body format error for CL Token:\n$clBody")
        }

        // Get action token
        val actResponse = client.newCall(
            Request.Builder()
                .url("${serverUrl}reader/api/0/token")
                .header("Authorization", "GoogleLogin auth=${authData.clientLoginToken}")
                .get()
                .build())
            .executeAsync()
        val actBody = actResponse.body.string()
        if (actResponse.code !in 200..299) {
            // It's not used currently but may be used later the same way Google Reader uses it
            // (expires in 30 minutes, with "x-reader-google-bad-token: true" header set).
        }
        authData.actionToken = actBody
    }

    private suspend inline fun <reified T> retryableGetRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        return try {
            getRequest<T>(query, params)
        } catch (e: RetryException) {
            authData.clientLoginToken = null
            authData.actionToken = null
            getRequest<T>(query, params)
        }
    }

    private suspend inline fun <reified T> retryablePostRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
        form: List<Pair<String, String>>? = null,
    ): T {
        return try {
            postRequest<T>(query, params, form)
        } catch (e: RetryException) {
            authData.clientLoginToken = null
            authData.actionToken = null
            postRequest<T>(query, params, form)
        }
    }

    private suspend inline fun <reified T> getRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        if (authData.clientLoginToken.isNullOrEmpty()) {
            reAuthenticate()
        }

        val response = client.newCall(
            Request.Builder()
                .url("$serverUrl$query?output=json${params?.joinToString(separator = "") { "&${it.first}=${it.second}" } ?: ""}")
                .addHeader("Authorization", "GoogleLogin auth=${authData.clientLoginToken}")
                .addHeader("User-Agent", USER_AGENT_STRING)
                .get()
                .build())
            .executeAsync()

        val body = response.body.string()
        when (response.code) {
            400 -> throw GoogleReaderAPIException("BadRequest")
            401 -> throw RetryException("Unauthorized")
            !in 200..299 -> {
                val gReaderError = try {
                    toDTO<GoogleReaderDTO.GReaderError>(body)
                } catch (ignore: Exception) {
                    GoogleReaderDTO.GReaderError(listOf(body))
                }
                throw GoogleReaderAPIException(gReaderError.errors.joinToString(";\n "))
            }
        }

        return toDTO(body)
    }

    private suspend inline fun <reified T> postRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
        form: List<Pair<String, String>>? = null,
    ): T {
        if (authData.clientLoginToken.isNullOrEmpty()) {
            reAuthenticate()
        }
        val response = client.newCall(
            Request.Builder()
                .url("$serverUrl$query?output=json${params?.joinToString(separator = "") { "&${it.first}=${it.second}" } ?: ""}")
                .addHeader("Authorization", "GoogleLogin auth=${authData.clientLoginToken}")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", USER_AGENT_STRING)
                .post(FormBody.Builder()
                    .apply {
                        form?.forEach { add(it.first, it.second) }
                        authData.actionToken?.let { add("T", it) }
                    }.build())
                .build())
            .executeAsync()

        val responseBody = response.body.string()
        when (response.code) {
            400 -> throw GoogleReaderAPIException("BadRequest")
            401 -> throw RetryException("Unauthorized")
            !in 200..299 -> {
                throw GoogleReaderAPIException(responseBody)
            }
        }

        return toDTO(responseBody)
    }

    suspend fun getUserInfo(): GoogleReaderDTO.User =
        retryableGetRequest<GoogleReaderDTO.User>("reader/api/0/user-info")

    suspend fun getSubscriptionList(): GoogleReaderDTO.SubscriptionList =
        retryableGetRequest<GoogleReaderDTO.SubscriptionList>("reader/api/0/subscription/list")

    suspend fun getReadItemIds(
        since: Long,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds =
        retryableGetRequest<GoogleReaderDTO.ItemIds>(
            query = "reader/api/0/stream/items/ids",
            params = mutableListOf<Pair<String, String>>().apply {
                add(Pair("s", Stream.READ.tag))
                add(Pair("ot", since.toString()))
                limit?.let { add(Pair("n", limit)) }
                continuationId?.let { add(Pair("c", continuationId)) }
            }
        )

    suspend fun getUnreadItemIds(
        since: Long? = null,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds =
        retryableGetRequest<GoogleReaderDTO.ItemIds>(
            query = "reader/api/0/stream/items/ids",
            params = mutableListOf<Pair<String, String>>().apply {
                add(Pair("s", Stream.ALL_ITEMS.tag))
                add(Pair("xt", Stream.READ.tag))
                limit?.let { add(Pair("n", limit)) }
                since?.let { add(Pair("ot", since.toString())) }
                continuationId?.let { add(Pair("c", continuationId)) }
            }
        )

    suspend fun getStarredItemIds(
        since: Long? = null,
        limit: String? = MAXIMUM_ITEMS_LIMIT,
        continuationId: String? = null,
    ): GoogleReaderDTO.ItemIds =
        retryableGetRequest<GoogleReaderDTO.ItemIds>(
            query = "reader/api/0/stream/items/ids",
            params = mutableListOf<Pair<String, String>>().apply {
                add(Pair("s", Stream.STARRED.tag))
                limit?.let { add(Pair("n", limit)) }
                since?.let { add(Pair("ot", since.toString())) }
                continuationId?.let { add(Pair("c", continuationId)) }
            }
        )

    suspend fun getItemsContents(ids: List<String>?) =
        retryablePostRequest<GoogleReaderDTO.ItemsContents>(
            query = "reader/api/0/stream/items/contents",
            form = ids?.map {
                Pair("i", it.ofItemIdToHexId())
            }
        )

    suspend fun subscriptionQuickAdd(feedUrl: String): GoogleReaderDTO.QuickAddFeed =
        retryablePostRequest<GoogleReaderDTO.QuickAddFeed>(
            query = "reader/api/0/subscription/quickadd",
            params = listOf(Pair("quickadd", feedUrl)),
            form = listOf(Pair("quickadd", feedUrl))
        )

    suspend fun editTag(itemIds: List<String>, mark: String? = null, unmark: String? = null): String =
        retryablePostRequest<String>(
            query = "reader/api/0/edit-tag",
            form = mutableListOf<Pair<String, String>>().apply {
                itemIds.forEach { add(Pair("i", it.ofItemIdToHexId())) }
                mark?.let { add(Pair("a", mark)) }
                unmark?.let { add(Pair("r", unmark)) }
            }
        )

    suspend fun disableTag(categoryId: String): String =
        retryablePostRequest<String>(
            query = "reader/api/0/disable-tag",
            form = listOf(Pair("s", categoryId.ofCategoryIdToStreamId()))
        )

    suspend fun renameTag(categoryId: String, renameToName: String): String =
        retryablePostRequest<String>(
            query = "reader/api/0/rename-tag",
            form = listOf(
                Pair("s", categoryId.ofCategoryIdToStreamId()),
                Pair("dest", renameToName.ofCategoryIdToStreamId()),
            )
        )

    suspend fun subscriptionEdit(
        action: String = "edit", destFeedId: String? = null, destCategoryId: String? = null,
        originCategoryId: String? = null, destFeedName: String? = null,
    ): String = retryablePostRequest<String>(
        query = "reader/api/0/subscription/edit",
        form = mutableListOf(Pair("ac", action)).apply {
            destFeedId?.let { add(Pair("s", it.ofFeedIdToStreamId())) }
            destCategoryId?.let { add(Pair("a", it.ofCategoryIdToStreamId())) }
            originCategoryId?.let { add(Pair("r", it.ofCategoryIdToStreamId())) }
            destFeedName?.takeIf { it.isNotBlank() }?.let { add(Pair("t", destFeedName)) }
        }
    )

    // Not all services support it
    suspend fun markAllAsRead(streamId: String, sinceTimestamp: Long? = null): String =
        retryablePostRequest<String>(
            query = "reader/api/0/mark-all-as-read",
            form = mutableListOf(
                Pair("s", streamId),
            ).apply {
                sinceTimestamp?.let { add(Pair("ts", it.toString())) }
            }
        )

    companion object {

        const val MAXIMUM_ITEMS_LIMIT = "10000"

        fun String.ofItemIdToHexId(): String {
            return String.format("%016x", toLong())
        }

        fun String.ofItemHexIdToId(): String {
            return toLong(16).toString()
        }

        fun String.ofItemStreamIdToHexId(): String {
            return replace("tag:google.com,2005:reader/item/", "")
        }

        fun String.ofItemStreamIdToId(): String {
            return ofItemStreamIdToHexId().ofItemHexIdToId()
        }

        fun String.ofItemHexIdToStreamId(): String {
            return "tag:google.com,2005:reader/item/$this"
        }

        fun String.ofItemIdToStreamId(): String {
            return "tag:google.com,2005:reader/item/${ofItemIdToHexId()}"
        }

        fun String.ofFeedIdToStreamId(): String {
            return "feed/$this"
        }

        fun String.ofFeedStreamIdToId(): String {
            return replace("feed/", "")
        }

        fun String.ofCategoryIdToStreamId(): String {
            return "user/-/label/$this"
        }

        private val categoryStreamIdRegex = "user/[^/]+/label/".toRegex()

        fun String.ofCategoryStreamIdToId(): String {
            return replace(categoryStreamIdRegex, "")
        }

        private val instances: ConcurrentHashMap<String, GoogleReaderAPI> = ConcurrentHashMap()

        fun getInstance(
            serverUrl: String,
            username: String,
            password: String,
            httpUsername: String? = null,
            httpPassword: String? = null,
        ): GoogleReaderAPI = instances.getOrPut("$serverUrl$username$password$httpUsername$httpPassword") {
            GoogleReaderAPI(serverUrl, username, password, httpUsername, httpPassword)
        }

        fun clearInstance() {
            instances.clear()
        }
    }
}
