package me.ash.reader.infrastructure.cache

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import javax.inject.Inject

private const val TAG = "DiffMapHolder"

@OptIn(FlowPreview::class)
class DiffMapHolder @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val rssService: RssService,
) {
    val diffMap = mutableStateMapOf<String, Diff>()
    val diffMapSnapshotFlow = snapshotFlow { diffMap.toMap() }.stateIn(
        applicationScope,
        SharingStarted.Eagerly,
        emptyMap()
    )
    private val gson = Gson()
    private val cacheFile = context.cacheDir.resolve("diff_map.json")

    init {
        commitDiffsFromCache()
        applicationScope.launch(ioDispatcher) {
            diffMapSnapshotFlow.debounce(2_000).collect {
                if (it.isNotEmpty()) {
                    writeDiffsToCache()
                }
            }
        }
    }

    /**
     * Updates the diff map with changes related to an article's read/unread status.
     *
     * This function manages a map (`diffMap`) that tracks changes (diffs) to the
     * read/unread status of articles. It allows specifying a direct update of
     * the unread status or toggling it based on the current state.
     *
     * @param articleWithFeed The article and its associated feed data. This is used to identify the article
     *                        and access its current read/unread state.
     * @param isUnread An optional boolean indicating the desired read/unread status of the article.
     *                 - If `null`, the function toggles the current status of the article (unread becomes read, read becomes unread).
     *                 - If `true`, the article is marked as unread.
     *                 - If `false`, the article is marked as read.
     */
    fun updateDiff(
        articleWithFeed: ArticleWithFeed,
        isUnread: Boolean? = null
    ) {
        val articleId = articleWithFeed.article.id
        if (isUnread != null) {
            diffMap[articleId] = Diff(
                isUnread = isUnread,
                articleWithFeed = articleWithFeed
            )
        } else {
            val diff = diffMap[articleId]
            if (diff != null) {
                diffMap.remove(articleId)
            } else {
                diffMap[articleId] = Diff(
                    isUnread = !articleWithFeed.article.isUnread,
                    articleWithFeed = articleWithFeed
                )
            }
        }
    }

    fun commitDiffs() {
        applicationScope.launch(ioDispatcher) {
            val markAsReadArticles =
                diffMap.filter { !it.value.isUnread }.map { it.key }.toSet()
            val markAsUnreadArticles =
                diffMap.filter { it.value.isUnread }.map { it.key }.toSet()

            rssService.get()
                .batchMarkAsRead(articleIds = markAsReadArticles, isUnread = false)
            rssService.get()
                .batchMarkAsRead(articleIds = markAsUnreadArticles, isUnread = true)

        }.invokeOnCompletion {
            clearDiffs()
        }
    }

    private fun writeDiffsToCache() {
        applicationScope.launch(ioDispatcher) {
            val tmpJson = gson.toJson(diffMap)
            cacheFile.createNewFile()
            if (cacheFile.exists() && cacheFile.canWrite()) {
                cacheFile.writeText(tmpJson)
            }
        }
    }

    private fun commitDiffsFromCache() {
        applicationScope.launch(ioDispatcher) {
            if (cacheFile.exists() && cacheFile.canRead()) {
                val tmpJson = cacheFile.readText()
                val mapType = object :
                    TypeToken<Map<String, Diff>>() {}.type
                val diffMapFromCache = gson.fromJson<Map<String, Diff>>(
                    tmpJson,
                    mapType
                )
                diffMapFromCache?.let {
                    diffMap.clear()
                    diffMap.putAll(it)
                }
            }
        }.invokeOnCompletion {
            commitDiffs()
        }
    }

    private fun clearDiffs() {
        applicationScope.launch(ioDispatcher) {
            if (cacheFile.exists() && cacheFile.canWrite()) {
                cacheFile.delete()
            }
            diffMap.clear()
        }
    }
}

data class Diff(
    val isUnread: Boolean,
    val articleId: String,
    val feedId: String,
    val groupId: String
) {
    constructor(isUnread: Boolean, articleWithFeed: ArticleWithFeed) : this(
        isUnread = isUnread,
        articleId = articleWithFeed.article.id,
        feedId = articleWithFeed.feed.id,
        groupId = articleWithFeed.feed.groupId
    )
}
