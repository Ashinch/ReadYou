package me.ash.reader.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.ash.reader.R
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.domain.service.AccountService
import timber.log.Timber

internal val Context.widgetDataStore by preferencesDataStore("widgets")

@Singleton
class WidgetRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val groupDao: GroupDao,
    private val accountService: AccountService,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun Context.getConfig(widgetId: Int): FeedWidgetConfig? =
        withContext(Dispatchers.IO) {
            val string = widgetDataStore.data.first()[stringPreferencesKey(widgetId.toString())]
            if (string == null) return@withContext null
            return@withContext runCatching { json.decodeFromString<FeedWidgetConfig>(string) }
                .onFailure { Timber.e(it) }
                .getOrNull()
        }

    private suspend fun Context.writeConfig(widgetId: Int, config: FeedWidgetConfig) =
        withContext(Dispatchers.IO) {
            widgetDataStore.edit { preferences ->
                val configString =
                    runCatching { json.encodeToString(config) }.getOrNull() ?: return@edit
                preferences[stringPreferencesKey(widgetId.toString())] = configString
            }
        }

    suspend fun getConfig(widgetId: Int): FeedWidgetConfig =
        context.getConfig(widgetId)
            ?: FeedWidgetConfig.default(accountService.getCurrentAccountId())

    private suspend fun getArticles(dataSource: DataSource): List<Article> =
        when (dataSource) {
            is DataSource.Account ->
                articleDao.queryLatestUnreadArticles(accountId = dataSource.accountId)
            is DataSource.Feed ->
                articleDao.queryLatestUnreadArticlesFromFeed(feedId = dataSource.feedId)
            is DataSource.Group ->
                articleDao.queryLatestUnreadArticlesFromGroup(groupId = dataSource.groupId)
        }.map {
            Article(
                title = it.article.title,
                imgUrl = it.article.img,
                feedName = it.feed.name,
                id = it.article.id,
            )
        }

    suspend fun getData(dataSource: DataSource): WidgetData {
        val title =
            when (dataSource) {
                is DataSource.Account -> context.getString(R.string.unread)
                is DataSource.Feed ->
                    feedDao.queryById(dataSource.feedId)?.name ?: context.getString(R.string.unread)
                is DataSource.Group ->
                    groupDao.queryById(dataSource.groupId)?.name
                        ?: context.getString(R.string.unread)
            }
        val articles = getArticles(dataSource)
        return WidgetData(title, articles)
    }
}

data class WidgetData(val title: String, val articles: List<Article>)

data class Article(
    val id: String,
    val title: String,
    val imgUrl: String? = null,
    val feedName: String,
)
