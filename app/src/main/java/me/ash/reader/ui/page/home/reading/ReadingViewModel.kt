package me.ash.reader.ui.page.home.reading

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ItemSnapshotList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.domain.service.RssService
import me.ash.reader.ui.page.home.HomeViewModel
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val rssService: RssService,
    private val rssHelper: RssHelper,
) : ViewModel() {

    private val _readingUiState = MutableStateFlow(ReadingUiState())
    val readingUiState: StateFlow<ReadingUiState> = _readingUiState.asStateFlow()

    fun trySwitchArticle(articleId: String) {
        if (articleId.isNotEmpty()) {
            viewModelScope.launch {
                initData(articleId)
            }
        }
    }
    
    fun initData(articleId: String) {
        showLoading()
        viewModelScope.launch {
            _readingUiState.update {
                it.copy(articleWithFeed = rssService.get().findArticleById(articleId))
            }
            _readingUiState.value.articleWithFeed?.let {
                if (it.feed.isFullContent) internalRenderFullContent()
                else renderDescriptionContent()
            }
            // java.lang.NullPointerException: Attempt to invoke virtual method
            // 'boolean androidx.compose.ui.node.LayoutNode.getNeedsOnPositionedDispatch$ui_release()'
            // on a null object reference
            if (_readingUiState.value.listState.firstVisibleItemIndex != 0) {
                _readingUiState.value.listState.scrollToItem(0)
            }
            hideLoading()
        }
    }

    fun renderDescriptionContent() {
        _readingUiState.update {
            it.copy(
                content = it.articleWithFeed?.article?.fullContent
                    ?: it.articleWithFeed?.article?.rawDescription ?: "",
                isFullContent = false
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
        try {
            _readingUiState.update {
                it.copy(
                    content = rssHelper.parseFullContent(
                        _readingUiState.value.articleWithFeed?.article?.link ?: "",
                        _readingUiState.value.articleWithFeed?.article?.title ?: ""
                    ),
                    isFullContent = true
                )
            }
        } catch (e: Exception) {
            Log.i("RLog", "renderFullContent: ${e.message}")
            _readingUiState.update { it.copy(content = e.message) }
        }
        hideLoading()
    }

    fun markUnread(isUnread: Boolean) {
        val articleWithFeed = _readingUiState.value.articleWithFeed ?: return
        viewModelScope.launch {
            _readingUiState.update {
                it.copy(
                    articleWithFeed = articleWithFeed.copy(
                        article = articleWithFeed.article.copy(
                            isUnread = isUnread
                        )
                    )
                )
            }
            rssService.get().markAsRead(
                groupId = null,
                feedId = null,
                articleId = _readingUiState.value.articleWithFeed!!.article.id,
                before = null,
                isUnread = isUnread,
            )
        }
    }

    fun markStarred(isStarred: Boolean) {
        val articleWithFeed = _readingUiState.value.articleWithFeed ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _readingUiState.update {
                it.copy(
                    articleWithFeed = articleWithFeed.copy(
                        article = articleWithFeed.article.copy(
                            isStarred = isStarred
                        )
                    )
                )
            }
            rssService.get().markAsStarred(
                articleId = articleWithFeed.article.id,
                isStarred = isStarred,
            )
        }
    }

    private fun showLoading() {
        _readingUiState.update {
            it.copy(isLoading = true)
        }
    }

    private fun hideLoading() {
        _readingUiState.update {
            it.copy(isLoading = false)
        }
    }

    fun recorderNextArticle(pagingItems: ItemSnapshotList<ArticleFlowItem>) {
        if (pagingItems.size > 0) {
            val cur = _readingUiState.value.articleWithFeed?.article
            if (cur != null) {
                var found = false
                var index = 0
                for (item in pagingItems) {
                    if (item is ArticleFlowItem.Article) {
                        val itemId = item.articleWithFeed.article.id
                        if (itemId == cur.id) {
                            found = true
                            _readingUiState.update {
                                it.copy(nextArticleId = "")
                            }
                        } else if (found) {
                            _readingUiState.update {
                                it.copy(nextArticleId = itemId)
                            }
                            break
                        }
                    }
                    index++
                }
            }
        }
    }

    fun recordCurrentReadingState(homeViewModel: HomeViewModel) {
        val curId = readingUiState.value.articleWithFeed?.article?.id
        val list = homeViewModel.readingProgressState.value.readingList
        if (curId != null && list.isNotEmpty()) {
            var previous = ""
            var next = ""
            var targetIndex = -1
            var matched = false

            var last = ""
            for (id in list) {
                if (id == curId) {
                    previous = last
                    matched = true
                } else if (last == curId) {
                    next = id
                    break
                }

                targetIndex++
                last = id
                }
            if (matched) {
                homeViewModel.recordReadingState(targetIndex, previous, curId, next)
            } else {
                homeViewModel.recordReadingState(-2, "", curId, "")
            }
        }
    }
}

data class ReadingUiState(
    val articleWithFeed: ArticleWithFeed? = null,
    val content: String? = null,
    val isFullContent: Boolean = false,
    val isLoading: Boolean = true,
    val listState: LazyListState = LazyListState(),
    val nextArticleId: String = "",
)
