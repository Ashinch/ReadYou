package me.ash.reader.infrastructure.rss

import android.content.Context
import androidx.annotation.CheckResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.ash.reader.domain.model.article.Article
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.DataStoreKey.Companion.currentAccountId
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest
import javax.inject.Inject

class ReaderCacheHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val rssHelper: RssHelper,
    private val settingsProvider: SettingsProvider
) {
    private val cacheDir = context.cacheDir.resolve("readability")
    private val md = MessageDigest.getInstance("SHA-256")

    private val currentCacheDir: File
        get() = cacheDir.resolve(settingsProvider.getOrDefault<Int>(currentAccountId, 1).toString())

    @OptIn(ExperimentalStdlibApi::class)
    private fun getFileNameFor(articleId: String): String {
        val bytes = articleId.toByteArray()
        val digest = md.digest(bytes)
        return digest.toHexString() + ".html"
    }

    private suspend fun writeContentToCache(content: String, articleId: String): Boolean {
        return withContext(ioDispatcher) {
            runCatching {
                currentCacheDir.run {
                    mkdirs()
                    resolve(getFileNameFor(articleId)).run {
                        createNewFile()
                        writeText(content)
                    }
                }
            }.fold(onSuccess = { true }, onFailure = { false })
        }
    }

    @CheckResult
    private suspend fun readContentFromCache(articleId: String): Result<String> {
        return withContext(ioDispatcher) {
            runCatching {
                val file = currentCacheDir.resolve(getFileNameFor(articleId))
                if (!file.exists()) return@withContext Result.failure(FileNotFoundException())
                file.readText()
            }
        }
    }

    suspend fun fetchFullContent(article: Article): Result<String> {
        return withContext(ioDispatcher) {
            runCatching {
                val fullContent = rssHelper.parseFullContent(article.link, article.title)
                if (fullContent.isNotBlank()) {
                    writeContentToCache(fullContent, article.id)
                    fullContent
                } else return@withContext Result.failure(Exception())
            }
        }
    }

    @CheckResult
    suspend fun readOrFetchFullContent(article: Article): Result<String> {
        return withContext(ioDispatcher) {
            runCatching {
                val result = readContentFromCache(article.id)
                if (result.isSuccess) return@withContext result
                return@withContext fetchFullContent(article)
            }
        }
    }

    suspend fun deleteCacheFor(articleId: String): Boolean {
        return withContext(ioDispatcher) {
            runCatching {
                val file = currentCacheDir.resolve(getFileNameFor(articleId))
                if (!file.exists()) return@runCatching false
                return@runCatching file.delete()
            }.fold(onSuccess = { true }, onFailure = { false })
        }
    }

    suspend fun clearCache(): Boolean {
        return withContext(ioDispatcher) {
            runCatching {
                return@withContext currentCacheDir.deleteRecursively()
            }.fold(onSuccess = { true }, onFailure = { false })
        }
    }
}