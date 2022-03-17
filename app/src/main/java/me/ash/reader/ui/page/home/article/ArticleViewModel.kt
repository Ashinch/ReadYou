package me.ash.reader.ui.page.home.article

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.data.article.ArticleWithFeed
import me.ash.reader.data.repository.RssRepository
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val rssRepository: RssRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ArticleViewState())
    val viewState: StateFlow<ArticleViewState> = _viewState.asStateFlow()

    fun dispatch(action: ArticleViewAction) {
        when (action) {
            is ArticleViewAction.FetchData -> fetchData(
                groupId = action.groupId,
                feedId = action.feedId,
                isStarred = action.isStarred,
                isUnread = action.isUnread,
            )
            is ArticleViewAction.ChangeRefreshing -> changeRefreshing(action.isRefreshing)
            is ArticleViewAction.ScrollToItem -> scrollToItem(action.index)
            is ArticleViewAction.PeekSyncWork -> peekSyncWork()
        }
    }

    private fun peekSyncWork() {
        _viewState.update {
            it.copy(
                syncWorkInfo = rssRepository.get().peekWork()
            )
        }
    }

    private fun fetchData(
        groupId: String? = null,
        feedId: String? = null,
        isStarred: Boolean,
        isUnread: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            rssRepository.get().pullImportant(isStarred, true)
                .collect { importantList ->
                    _viewState.update {
                        it.copy(
                            filterImportant = importantList.sumOf { it.important },
                        )
                    }
                }
        }
        _viewState.update {
            it.copy(
                pagingData = Pager(PagingConfig(pageSize = 10)) {
                    rssRepository.get().pullArticles(
                        groupId = groupId,
                        feedId = feedId,
                        isStarred = isStarred,
                        isUnread = isUnread,
                    )
                }.flow.cachedIn(viewModelScope)
            )
        }
    }

    private fun scrollToItem(index: Int) {
        viewModelScope.launch {
            _viewState.value.listState.scrollToItem(index)
        }
    }

    private fun changeRefreshing(isRefreshing: Boolean) {
        _viewState.update {
            it.copy(isRefreshing = isRefreshing)
        }
    }
}

data class ArticleViewState(
    val filterImportant: Int = 0,
    val listState: LazyListState = LazyListState(),
    val isRefreshing: Boolean = false,
    val pagingData: Flow<PagingData<ArticleWithFeed>>? = null,
    val syncWorkInfo: String = "",
)

sealed class ArticleViewAction {
    data class FetchData(
        val groupId: String? = null,
        val feedId: String? = null,
        val isStarred: Boolean,
        val isUnread: Boolean,
    ) : ArticleViewAction()

    data class ChangeRefreshing(
        val isRefreshing: Boolean
    ) : ArticleViewAction()

    data class ScrollToItem(
        val index: Int
    ) : ArticleViewAction()

    object PeekSyncWork : ArticleViewAction()
}