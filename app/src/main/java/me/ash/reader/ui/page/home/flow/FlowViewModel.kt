package me.ash.reader.ui.page.home.flow

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
import me.ash.reader.ui.page.home.FilterState
import javax.inject.Inject

@HiltViewModel
class FlowViewModel @Inject constructor(
    private val rssRepository: RssRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ArticleViewState())
    val viewState: StateFlow<ArticleViewState> = _viewState.asStateFlow()

    fun dispatch(action: FlowViewAction) {
        when (action) {
            is FlowViewAction.FetchData -> fetchData(action.filterState)
            is FlowViewAction.ChangeRefreshing -> changeRefreshing(action.isRefreshing)
            is FlowViewAction.ScrollToItem -> scrollToItem(action.index)
            is FlowViewAction.PeekSyncWork -> peekSyncWork()
        }
    }

    private fun peekSyncWork() {
        _viewState.update {
            it.copy(
                syncWorkInfo = rssRepository.get().peekWork()
            )
        }
    }

    private fun fetchData(filterState: FilterState) {
        viewModelScope.launch(Dispatchers.IO) {
            rssRepository.get().pullImportant(filterState.filter.isStarred(), true)
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
                        groupId = filterState.group?.id,
                        feedId = filterState.feed?.id,
                        isStarred = filterState.filter.isStarred(),
                        isUnread = filterState.filter.isUnread(),
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

sealed class FlowViewAction {
    data class FetchData(
        val filterState: FilterState,
    ) : FlowViewAction()

    data class ChangeRefreshing(
        val isRefreshing: Boolean
    ) : FlowViewAction()

    data class ScrollToItem(
        val index: Int
    ) : FlowViewAction()

    object PeekSyncWork : FlowViewAction()
}