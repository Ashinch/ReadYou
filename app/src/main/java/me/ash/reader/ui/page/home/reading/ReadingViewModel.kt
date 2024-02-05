package me.ash.reader.ui.page.home.reading

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ItemSnapshotList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.rss.RssHelper
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val rssService: RssService,
    private val rssHelper: RssHelper,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val _readingUiState = MutableStateFlow(ReadingUiState())
    val readingUiState: StateFlow<ReadingUiState> = _readingUiState.asStateFlow()

    private val _readerState: MutableStateFlow<ReaderState> = MutableStateFlow(ReaderState())
    val readerStateStateFlow = _readerState.asStateFlow()

    private val currentArticle: Article?
        get() = readingUiState.value.articleWithFeed?.article
    private val currentFeed: Feed?
        get() = readingUiState.value.articleWithFeed?.feed

    fun initData(articleId: String) {
        showLoading()
        viewModelScope.launch(ioDispatcher) {
            rssService.get().findArticleById(articleId)?.run {
                _readingUiState.update {
                    it.copy(
                        articleWithFeed = this,
                        articleId = article.id,
                        isStarred = article.isStarred,
                        isUnread = article.isUnread
                    )
                }
                _readerState.update {
                    it.copy(
                        feedName = feed.name,
                        title = article.title,
                        author = article.author,
                        link = article.link,
                        publishedDate = article.date,
                    )
                }
            }
            currentFeed?.let {
                if (it.isFullContent) internalRenderFullContent()
                else renderDescriptionContent()
            }
            // java.lang.NullPointerException: Attempt to invoke virtual method
            // 'boolean androidx.compose.ui.node.LayoutNode.getNeedsOnPositionedDispatch$ui_release()'
            // on a null object reference
            if (_readingUiState.value.listState.firstVisibleItemIndex != 0) {
                _readingUiState.value.listState.scrollToItem(0)
            }
        }
    }

    fun renderDescriptionContent() {
        _readerState.update {
            it.copy(
                content = ReaderState.Description(
                    content = currentArticle?.fullContent
                        ?: currentArticle?.rawDescription ?: ""
                )
            )
        }
    }

    fun renderFullContent() {
        viewModelScope.launch {
            internalRenderFullContent()
        }
    }

    private suspend fun internalRenderFullContent() {
        showLoading()
        runCatching {
            rssHelper.parseFullContent(
                currentArticle?.link ?: "",
                currentArticle?.title ?: ""
            )
        }.onSuccess { content ->
            _readerState.update { it.copy(content = ReaderState.FullContent(content = content)) }
        }.onFailure { th ->
            Log.i("RLog", "renderFullContent: ${th.message}")
            _readerState.update { it.copy(content = ReaderState.Error(th.message)) }
        }
    }

    fun updateReadStatus(isUnread: Boolean) {
        currentArticle?.run {
            applicationScope.launch(ioDispatcher) {
                _readingUiState.update { it.copy(isUnread = isUnread) }
                rssService.get().markAsRead(
                    groupId = null,
                    feedId = null,
                    articleId = id,
                    before = null,
                    isUnread = isUnread,
                )
            }
        }
    }

    fun markAsRead() = updateReadStatus(isUnread = false)

    fun markAsUnread() = updateReadStatus(isUnread = true)

    fun updateStarredStatus(isStarred: Boolean) {
        applicationScope.launch(ioDispatcher) {
            _readingUiState.update { it.copy(isStarred = isStarred) }
            currentArticle?.let {
                rssService.get().markAsStarred(
                    articleId = it.id,
                    isStarred = isStarred,
                )
            }
        }
    }

    private fun showLoading() {
        _readerState.update {
            it.copy(content = ReaderState.Loading)
        }
    }

    fun updateNextArticleId(pagingItems: ItemSnapshotList<ArticleFlowItem>) {
        val items = pagingItems.items
        val index = items.indexOfFirst { item ->
            item is ArticleFlowItem.Article && item.articleWithFeed.article.id == currentArticle?.id
        }
        items.subList(index + 1, items.size).forEach { item ->
            if (item is ArticleFlowItem.Article) {
                _readingUiState.update { it.copy(nextArticleId = item.articleWithFeed.article.id) }
                return
            }
        }
        _readingUiState.update { it.copy(nextArticleId = null) }
    }
}

data class ReadingUiState(
    val articleWithFeed: ArticleWithFeed? = null,
    val articleId: String? = null,
    val isUnread: Boolean = false,
    val isStarred: Boolean = false,
    val listState: LazyListState = LazyListState(),
    val nextArticleId: String? = null,
)

data class ReaderState(
    val feedName: String = "",
    val title: String? = null,
    val author: String? = null,
    val link: String? = null,
    val publishedDate: Date = Date(0L),
    val content: ContentState = Description(null)
) {
    sealed interface ContentState {
        val text: String?
            get() {
                return when (this) {
                    is Description -> content
                    is Error -> message
                    is FullContent -> content
                    Loading -> null
                }
            }
    }

    data class FullContent(val content: String?) : ContentState
    data class Description(val content: String?) : ContentState
    data class Error(val message: String?) : ContentState

    object Loading: ContentState
}
