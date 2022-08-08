package me.ash.reader.ui.page.home.reading

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.ItemSnapshotList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.data.model.article.ArticleFlowItem
import me.ash.reader.data.model.article.ArticleWithFeed
import me.ash.reader.data.repository.RssHelper
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.ui.page.common.RouteName
import me.ash.reader.ui.page.home.HomeUiState
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val rssHelper: RssHelper,
) : ViewModel() {

    private val _readingUiState = MutableStateFlow(ReadingUiState())
    val readingUiState: StateFlow<ReadingUiState> = _readingUiState.asStateFlow()

    fun initData(articleId: String) {
        showLoading()
        viewModelScope.launch {
            _readingUiState.update {
                it.copy(articleWithFeed = rssRepository.get().findArticleById(articleId))
            }
            _readingUiState.value.articleWithFeed?.let {
                if (it.feed.isFullContent) internalRenderFullContent()
                else renderDescriptionContent()
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
            rssRepository.get().markAsRead(
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
            rssRepository.get().updateArticleInfo(
                articleWithFeed.article.copy(isStarred = isStarred)
            )
        }
    }

    fun nextArticle(navController: NavController, nextArticleId: String) {
        navController.popBackStack()
        if (nextArticleId.isNotBlank()) {
            navController.navigate("${RouteName.READING}/${nextArticleId}")
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

    fun recorderNextArticle(
        readingUiState: ReadingUiState, homeUiState: HomeUiState, pagingItems:
        ItemSnapshotList<ArticleFlowItem>
    ) {
        if (pagingItems.size > 0) {
            val cur = readingUiState.articleWithFeed?.article
            if (cur != null) {
                var found = false
                for (item in pagingItems) {
                    if (item is ArticleFlowItem.Article) {
                        val itemId = item.articleWithFeed.article.id
                        if (itemId == cur.id) {
                            found = true
                            homeUiState.nextArticleId = ""
                        } else if (found) {
                            homeUiState.nextArticleId = itemId
                            break
                        }
                    }
                }
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
)
