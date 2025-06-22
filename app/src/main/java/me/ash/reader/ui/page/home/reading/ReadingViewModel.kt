package me.ash.reader.ui.page.home.reading

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.data.ArticlePagingListUseCase
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.android.AndroidImageDownloader
import me.ash.reader.infrastructure.android.TextToSpeechManager
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.rss.ReaderCacheHelper
import java.util.Date

private const val TAG = "ReadingViewModel"

@HiltViewModel(assistedFactory = ReadingViewModel.ReadingViewModelFactory::class)
class ReadingViewModel @AssistedInject constructor(
    @Assisted private val initialArticleId: String,
    @Assisted private val initialListIndex: Int?,
    private val rssService: RssService,
    private val readerCacheHelper: ReaderCacheHelper,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
    val textToSpeechManager: TextToSpeechManager,
    private val imageDownloader: AndroidImageDownloader,
    private val diffMapHolder: DiffMapHolder,
    private val pagingListUseCase: ArticlePagingListUseCase,
) : ViewModel() {

    private val _readingUiState = MutableStateFlow(ReadingUiState())
    val readingUiState: StateFlow<ReadingUiState> = _readingUiState.asStateFlow()

    private val _readerState: MutableStateFlow<ReaderState> = MutableStateFlow(ReaderState())
    val readerStateStateFlow = _readerState.asStateFlow()

    private val currentArticle: Article?
        get() = readingUiState.value.articleWithFeed?.article
    private val currentFeed: Feed?
        get() = readingUiState.value.articleWithFeed?.feed

    init {
        initData(initialArticleId, initialListIndex)
    }

    fun initData(articleId: String, listIndex: Int? = null) {
        viewModelScope.launch {
            val snapshotList = pagingListUseCase.itemSnapshotList

            val itemByIndex =
                listIndex?.let { snapshotList.getOrNull(it) as? ArticleFlowItem.Article }

            val itemFromList =
                if (itemByIndex != null && itemByIndex.articleWithFeed.article.id != articleId) {
                    itemByIndex
                } else {
                    snapshotList.find { item ->
                        item is ArticleFlowItem.Article && item.articleWithFeed.article.id == articleId
                    } as? ArticleFlowItem.Article
                }

            val item =
                itemByIndex?.articleWithFeed ?: (itemFromList?.articleWithFeed ?: rssService.get()
                    .findArticleById(articleId)!!)

            item.run {
                diffMapHolder.updateDiff(this, isUnread = false)
                _readingUiState.update {
                    it.copy(
                        articleWithFeed = this, isStarred = article.isStarred, isUnread = false
                    )
                }
                _readerState.update {
                    it.copy(
                        articleId = article.id,
                        feedName = feed.name,
                        title = article.title,
                        author = article.author,
                        link = article.link,
                        publishedDate = article.date,
                    ).prefetchArticleId().renderContent(this)
                }
            }
        }
    }

    suspend fun ReaderState.renderContent(articleWithFeed: ArticleWithFeed): ReaderState {
        val contentState = if (articleWithFeed.feed.isFullContent) {
            val fullContent =
                readerCacheHelper.readFullContent(articleWithFeed.article.id).getOrNull()
            if (fullContent != null) ReaderState.FullContent(fullContent) else {
                renderFullContent()
                ReaderState.Loading
            }
        } else ReaderState.Description(articleWithFeed.article.rawDescription)

        return copy(content = contentState)
    }

    fun renderDescriptionContent() {
        _readerState.update {
            it.copy(
                content = ReaderState.Description(
                    content = currentArticle?.rawDescription ?: ""
                )
            )
        }
    }

    fun renderFullContent() {
        val fetchJob = viewModelScope.launch {
            readerCacheHelper.readOrFetchFullContent(
                currentArticle!!
            ).onSuccess { content ->
                _readerState.update { it.copy(content = ReaderState.FullContent(content = content)) }
            }.onFailure { th ->
                _readerState.update { it.copy(content = ReaderState.Error(th.message.toString())) }
            }
        }
        viewModelScope.launch {
            delay(100L)
            if (fetchJob.isActive) {
                setLoading()
            }
        }
    }

    fun updateReadStatus(isUnread: Boolean) {
        readingUiState.value.articleWithFeed?.let {
            diffMapHolder.updateDiff(
                it,
                isUnread = isUnread
            )
        }
        _readingUiState.update { it.copy(isUnread = diffMapHolder.checkIfUnread(it.articleWithFeed!!)) }
    }

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

    private fun setLoading() {
        _readerState.update {
            it.copy(content = ReaderState.Loading)
        }
    }

    fun ReaderState.prefetchArticleId(): ReaderState {
        val items = pagingListUseCase.itemSnapshotList
        val currentId = currentArticle?.id
        val index = items.indexOfFirst { item ->
            item is ArticleFlowItem.Article && item.articleWithFeed.article.id == currentId
        }
        var previousArticle: ReaderState.PrefetchResult? = null
        var nextArticle: ReaderState.PrefetchResult? = null

        if (index != -1 || currentId == null) {
            val prevIterator = items.listIterator(index)
            while (prevIterator.hasPrevious()) {
                val previousIndex = prevIterator.previousIndex()
                val prev = prevIterator.previous()
                if (prev is ArticleFlowItem.Article) {
                    previousArticle = ReaderState.PrefetchResult(
                        articleId = prev.articleWithFeed.article.id, index = previousIndex
                    )
                    break
                }
            }
            val nextIterator = items.listIterator(index + 1)
            while (nextIterator.hasNext()) {
                val nextIndex = nextIterator.nextIndex()
                val next = nextIterator.next()
                if (next is ArticleFlowItem.Article && next.articleWithFeed.article.id != currentId) {
                    nextArticle = ReaderState.PrefetchResult(
                        articleId = next.articleWithFeed.article.id, index = nextIndex
                    )
                    break
                }
            }
        }

        return copy(
            nextArticle = nextArticle, previousArticle = previousArticle, listIndex = index
        )
    }

    fun loadPrevious(): Boolean {
        val (articleId, listIndex) = readerStateStateFlow.value.previousArticle ?: return false
        initData(articleId, listIndex)
        return true
    }

    fun loadNext(): Boolean {
        val (articleId, listIndex) = readerStateStateFlow.value.nextArticle ?: return false
        initData(articleId, listIndex)
        return true
    }

    fun downloadImage(
        url: String, onSuccess: (Uri) -> Unit = {}, onFailure: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            imageDownloader.downloadImage(url).onSuccess(onSuccess).onFailure(onFailure)
        }
    }

    @AssistedFactory
    interface ReadingViewModelFactory {
        fun create(
            articleId: String, initialListIndex: Int?
        ): ReadingViewModel
    }
}

data class ReadingUiState(
    val articleWithFeed: ArticleWithFeed? = null,
    val isUnread: Boolean = false,
    val isStarred: Boolean = false,
)

data class ReaderState(
    val articleId: String? = null,
    val feedName: String = "",
    val title: String? = null,
    val author: String? = null,
    val link: String? = null,
    val publishedDate: Date = Date(0L),
    val content: ContentState = Loading,
    val listIndex: Int? = null,
    val nextArticle: PrefetchResult? = null,
    val previousArticle: PrefetchResult? = null
) {
    data class PrefetchResult(val articleId: String, val index: Int)

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

    data class FullContent(val content: String) : ContentState
    data class Description(val content: String) : ContentState
    data class Error(val message: String) : ContentState
    data object Loading : ContentState
}
