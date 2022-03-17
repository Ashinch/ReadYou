package me.ash.reader.ui.page.home.read

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.data.repository.RssHelper
import me.ash.reader.data.repository.RssRepository
import javax.inject.Inject

@HiltViewModel
class ReadViewModel @Inject constructor(
    val rssRepository: RssRepository,
    private val rssHelper: RssHelper,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ReadViewState())
    val viewState: StateFlow<ReadViewState> = _viewState.asStateFlow()

    fun dispatch(action: ReadViewAction) {
        when (action) {
            is ReadViewAction.InitData -> bindArticleWithFeed(action.articleWithFeed)
            is ReadViewAction.RenderDescriptionContent -> renderDescriptionContent()
            is ReadViewAction.RenderFullContent -> renderFullContent()
            is ReadViewAction.MarkUnread -> markUnread(action.isUnread)
            is ReadViewAction.MarkStarred -> markStarred(action.isStarred)
            is ReadViewAction.ScrollToItem -> scrollToItem(action.index)
            is ReadViewAction.ClearArticle -> clearArticle()
            is ReadViewAction.ChangeLoading -> changeLoading(action.isLoading)
        }
    }

    private fun bindArticleWithFeed(articleWithFeed: ArticleWithFeed) {
        _viewState.update {
            it.copy(articleWithFeed = articleWithFeed)
        }
    }

    private fun renderDescriptionContent() {
        _viewState.update {
            it.copy(
                content = rssHelper.parseDescriptionContent(
                    link = it.articleWithFeed?.article?.link ?: "",
                    content = it.articleWithFeed?.article?.rawDescription ?: "",
                )
            )
        }
    }

    private fun renderFullContent() {
        changeLoading(true)
        rssHelper.parseFullContent(
            _viewState.value.articleWithFeed?.article?.link ?: "",
            _viewState.value.articleWithFeed?.article?.title ?: ""
        ) { content ->
            _viewState.update {
                it.copy(content = content)
            }
        }
    }

    private fun markUnread(isUnread: Boolean) {
        _viewState.value.articleWithFeed?.let {
            _viewState.update {
                it.copy(
                    articleWithFeed = it.articleWithFeed?.copy(
                        article = it.articleWithFeed.article.copy(
                            isUnread = isUnread
                        )
                    )
                )
            }
            viewModelScope.launch {
                rssRepository.get().updateArticleInfo(
                    it.article.copy(
                        isUnread = isUnread
                    )
                )
            }
        }
    }

    private fun markStarred(isStarred: Boolean) {
        _viewState.value.articleWithFeed?.let {
            _viewState.update {
                it.copy(
                    articleWithFeed = it.articleWithFeed?.copy(
                        article = it.articleWithFeed.article.copy(
                            isStarred = isStarred
                        )
                    )
                )
            }
            viewModelScope.launch {
                rssRepository.get().updateArticleInfo(
                    it.article.copy(
                        isStarred = isStarred
                    )
                )
            }
        }
    }

    private fun scrollToItem(index: Int) {
        viewModelScope.launch {
            _viewState.value.listState.scrollToItem(index)
        }
    }

    private fun clearArticle() {
        _viewState.update {
            it.copy(articleWithFeed = null)
        }
    }

    private fun changeLoading(isLoading: Boolean) {
        _viewState.update {
            it.copy(isLoading = isLoading)
        }
    }
}

data class ReadViewState(
    val articleWithFeed: ArticleWithFeed? = null,
    val content: String? = null,
    val isLoading: Boolean = false,
    val listState: LazyListState = LazyListState(),
)

sealed class ReadViewAction {
    data class InitData(
        val articleWithFeed: ArticleWithFeed,
    ) : ReadViewAction()

    object RenderDescriptionContent : ReadViewAction()

    object RenderFullContent : ReadViewAction()

    data class MarkUnread(
        val isUnread: Boolean,
    ) : ReadViewAction()

    data class MarkStarred(
        val isStarred: Boolean,
    ) : ReadViewAction()

    data class ScrollToItem(
        val index: Int
    ) : ReadViewAction()

    object ClearArticle : ReadViewAction()

    data class ChangeLoading(
        val isLoading: Boolean
    ) : ReadViewAction()
}