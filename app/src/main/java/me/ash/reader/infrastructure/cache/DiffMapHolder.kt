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

    fun checkIfUnread(articleWithFeed: ArticleWithFeed): Boolean {
        return diffMap[articleWithFeed.article.id]?.isUnread ?: articleWithFeed.article.isUnread
    }

    /**
     * Updates the diff map with changes to an article's read/unread status.
     *
     * This function manages a map (`diffMap`) that tracks pending changes (diffs) to the
     * read/unread status of articles. These changes are not immediately applied to the
     * underlying data store but are held in `diffMap` until a later commit operation.
     *
     * The function supports three modes of updating:
     *
     * 1. **Toggle:** If `isUnread` is `null`, the function toggles the current read/unread
     *    status of the article.  If the article is currently unread, it will be marked as read,
     *    and vice-versa.
     * 2. **Mark as Unread:** If `isUnread` is `true`, the article will be marked as unread,
     *    regardless of its current status.
     * 3. **Mark as Read:** If `isUnread` is `false`, the article will be marked as read,
     *    regardless of its current status.
     *
     * The function determines if a change needs to be tracked based on the current status and desired status:
     *  - If the requested change matches the article's current status, the diff is removed from the map, if it exists. (No change is needed.)
     *  - Otherwise, the diff is added to or updated in the map.
     *
     * @param articleWithFeed The article and its associated feed data. This is used to identify the article
     *                        and access its current read/unread state.
     * @param isUnread An optional boolean indicating the desired read/unread status of the article.
     *                 - `null`: Toggles the current read/unread status.
     *                 - `true`: Marks the article as unread.
     *                 - `false`: Marks the article as read.
     *
     * @see Diff
     */
    fun updateDiff(
        articleWithFeed: ArticleWithFeed,
        isUnread: Boolean? = null
    ) {
        val articleId = articleWithFeed.article.id
        val isArticleUnread = articleWithFeed.article.isUnread

        if (isUnread == null) {
            if (diffMap.remove(articleId) == null) {
                diffMap[articleId] = Diff(
                    isUnread = !isArticleUnread,
                    articleWithFeed = articleWithFeed
                )
            }
        } else if (isUnread == isArticleUnread) {
            diffMap.remove(articleId)
        } else {
            diffMap[articleId] = Diff(
                isUnread = isUnread,
                articleWithFeed = articleWithFeed
            )
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
