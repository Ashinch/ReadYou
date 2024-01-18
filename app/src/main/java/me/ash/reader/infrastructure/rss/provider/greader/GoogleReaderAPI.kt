package me.ash.reader.infrastructure.rss.provider.greader

import me.ash.reader.infrastructure.di.USER_AGENT_STRING
import me.ash.reader.infrastructure.rss.provider.ProviderAPI
import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
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

    data class AuthData(
        var clientLoginToken: String?,
        var actionToken: String?,
    )

    private val authData = AuthData(null, null)

    suspend fun validCredentials(): Boolean {
        reauthenticate()
        return authData.clientLoginToken?.isNotEmpty() ?: false
    }

    private suspend fun reauthenticate() {
        // Get client login token
        val clResponse = client.newCall(
            Request.Builder()
                .url("${serverUrl}accounts/ClientLogin")
                .post(FormBody.Builder()
                    .add("output", "json")
                    .add("Email", username)
                    .add("Passwd", password)
                    .add("client", USER_AGENT_STRING)
                    .add("accountType", "HOSTED_OR_GOOGLE")
                    .add("service", "reader")
                    .build())
                .build())
            .executeAsync()

        val clBody = clResponse.body.string()
        when (clResponse.code) {
            400 -> throw Exception("BadRequest for CL Token")
            401 -> throw Exception("Unauthorized for CL Token")
            !in 200..299 -> {
                throw Exception(clBody)
            }
        }

        authData.clientLoginToken = clBody
            .split("\n")
            .find { it.startsWith("Auth=") }
            ?.substring(5)
            ?: throw Exception("body format error for CL Token:\n$clBody")

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

    class RetryException(message: String) : Exception(message)

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
            reauthenticate()
        }

        val response = client.newCall(
            Request.Builder()
                .url("${serverUrl}${query}?output=json${params?.joinToString(separator = "") { "&${it.first}=${it.second}" } ?: ""}")
                .header("Authorization", "GoogleLogin auth=${authData.clientLoginToken}")
                .get()
                .build())
            .executeAsync()

        val body = response.body.string()
        when (response.code) {
            400 -> throw Exception("BadRequest")
            401 -> throw RetryException("Unauthorized")
            !in 200..299 -> {
                val gReaderError = try {
                    toDTO<GoogleReaderDTO.GReaderError>(body)
                } catch (ignore: Exception) {
                    GoogleReaderDTO.GReaderError(listOf(body))
                }
                throw Exception(gReaderError.errors.joinToString(";\n "))
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
            reauthenticate()
        }
        val response = client.newCall(
            Request.Builder()
                .url("${serverUrl}${query}?output=json${params?.joinToString(separator = "") { "&${it.first}=${it.second}" } ?: ""}")
                .headers(mapOf(
                    "Authorization" to "GoogleLogin auth=${authData.clientLoginToken}",
                    "Content-Type" to "application/x-www-form-urlencoded",
                ).toHeaders())
                .post(FormBody.Builder()
                    .apply {
                        form?.forEach { add(it.first, it.second) }
                        authData.actionToken?.let { add("T", it) }
                    }.build())
                .build())
            .executeAsync()

        val responseBody = response.body.string()
        when (response.code) {
            400 -> throw Exception("BadRequest")
            401 -> throw RetryException("Unauthorized")
            !in 200..299 -> {
                throw Exception(responseBody)
            }
        }

        return toDTO(responseBody)
    }

    suspend fun getUserInfo(): GoogleReaderDTO.User =
        retryableGetRequest<GoogleReaderDTO.User>("reader/api/0/user-info")

    suspend fun getSubscriptionList(): GoogleReaderDTO.SubscriptionList =
        retryableGetRequest<GoogleReaderDTO.SubscriptionList>("reader/api/0/subscription/list")

    suspend fun getReadItemIds(since: Long): GoogleReaderDTO.ItemIds =
        retryableGetRequest<GoogleReaderDTO.ItemIds>(
            query = "reader/api/0/stream/items/ids",
            params = listOf(
                Pair("s", "user/-/state/com.google/read"),
                Pair("ot", since.toString()),
                Pair("n", MAXIMUM_ITEMS_LIMIT),
            ))

    suspend fun getUnreadItemIds(): GoogleReaderDTO.ItemIds =
        retryableGetRequest<GoogleReaderDTO.ItemIds>(
            query = "reader/api/0/stream/items/ids",
            params = listOf(
                Pair("s", "user/-/state/com.google/reading-list"),
                Pair("xt", "user/-/state/com.google/read"),
                Pair("n", MAXIMUM_ITEMS_LIMIT),
            ))

    suspend fun getStarredItemIds(): GoogleReaderDTO.ItemIds =
        retryableGetRequest<GoogleReaderDTO.ItemIds>(
            query = "reader/api/0/stream/items/ids",
            params = listOf(
                Pair("s", "user/-/state/com.google/starred"),
                Pair("n", MAXIMUM_ITEMS_LIMIT),
            ))

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

    enum class subscriptionOperationType

    suspend fun editTag(categoryName: String): String =
        retryablePostRequest<String>(
            query = "reader/api/0/edit-tag",
            form = listOf(Pair("a", categoryName.ofCategoryIdToStreamId()))
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
            if (destFeedId != null) add(Pair("s", destFeedId.ofFeedIdToStreamId()))
            if (destCategoryId != null) add(Pair("a", destCategoryId.ofCategoryIdToStreamId()))
            if (originCategoryId != null) add(Pair("r", originCategoryId.ofCategoryIdToStreamId()))
            if (destFeedName?.isNotBlank() == true) add(Pair("t", destFeedName))
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

        fun String.ofCategoryStreamIdToId(): String {
            return replace("user/-/label/", "")
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
