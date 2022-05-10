package me.ash.reader.ui.page.home.read

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.data.entity.ArticleWithFeed
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
            is ReadViewAction.InitData -> bindArticleWithFeed(action.articleId)
            is ReadViewAction.RenderDescriptionContent -> renderDescriptionContent()
            is ReadViewAction.RenderFullContent -> renderFullContent()
            is ReadViewAction.MarkUnread -> markUnread(action.isUnread)
            is ReadViewAction.MarkStarred -> markStarred(action.isStarred)
            is ReadViewAction.ClearArticle -> clearArticle()
            is ReadViewAction.ChangeLoading -> changeLoading(action.isLoading)
        }
    }

    private fun bindArticleWithFeed(articleId: String) {
        changeLoading(true)
        viewModelScope.launch {
            _viewState.update {
                it.copy(articleWithFeed = rssRepository.get().findArticleById(articleId))
            }
            _viewState.value.articleWithFeed?.let {
                if (it.feed.isFullContent) internalRenderFullContent()
                else renderDescriptionContent()
            }
            changeLoading(false)
        }
    }

    private fun renderDescriptionContent() {
        _viewState.update {
            it.copy(
                content = it.articleWithFeed?.article?.fullContent
                    ?: it.articleWithFeed?.article?.rawDescription ?: "",
            )
        }
    }

    private fun renderFullContent() {
        viewModelScope.launch {
            internalRenderFullContent()
        }
    }

    private suspend fun internalRenderFullContent() {
        changeLoading(true)
        try {
            _viewState.update {
                it.copy(
                    content = rssHelper.parseFullContent(
                        _viewState.value.articleWithFeed?.article?.link ?: "",
                        _viewState.value.articleWithFeed?.article?.title ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            Log.i("RLog", "renderFullContent: ${e.message}")
            _viewState.update {
                it.copy(
                    content = e.message
                )
            }
        }
        changeLoading(false)
    }

    private fun markUnread(isUnread: Boolean) {
        val articleWithFeed = _viewState.value.articleWithFeed ?: return
        viewModelScope.launch {
            _viewState.update {
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
                articleId = _viewState.value.articleWithFeed!!.article.id,
                before = null,
                isUnread = isUnread,
            )
        }
    }

    private fun markStarred(isStarred: Boolean) {
        val articleWithFeed = _viewState.value.articleWithFeed ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _viewState.update {
                it.copy(
                    articleWithFeed = articleWithFeed.copy(
                        article = articleWithFeed.article.copy(
                            isStarred = isStarred
                        )
                    )
                )
            }
            rssRepository.get().updateArticleInfo(
                articleWithFeed.article.copy(
                    isStarred = isStarred
                )
            )
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
    val isLoading: Boolean = true,
    val scrollState: ScrollState = ScrollState(0),
)

sealed class ReadViewAction {
    data class InitData(
        val articleId: String,
    ) : ReadViewAction()

    object RenderDescriptionContent : ReadViewAction()

    object RenderFullContent : ReadViewAction()

    data class MarkUnread(
        val isUnread: Boolean,
    ) : ReadViewAction()

    data class MarkStarred(
        val isStarred: Boolean,
    ) : ReadViewAction()

    object ClearArticle : ReadViewAction()

    data class ChangeLoading(
        val isLoading: Boolean
    ) : ReadViewAction()
}