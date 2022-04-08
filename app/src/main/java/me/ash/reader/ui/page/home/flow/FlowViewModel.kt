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
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.ui.page.home.FilterState
import java.util.*
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
            is FlowViewAction.MarkAsRead -> markAsRead(
                action.groupId,
                action.feedId,
                action.articleId,
                action.markAsReadBefore,
            )
            is FlowViewAction.InputSearchContent -> inputSearchContent(action.content)
        }
    }

    private fun fetchData(filterState: FilterState? = null) {
//        viewModelScope.launch(Dispatchers.Default) {
//            rssRepository.get().pullImportant(filterState.filter.isStarred(), true)
//                .collect { importantList ->
//                    _viewState.update {
//                        it.copy(
//                            filterImportant = importantList.sumOf { it.important },
//                        )
//                    }
//                }
//        }
        if (_viewState.value.searchContent.isNotBlank()) {
            _viewState.update {
                it.copy(
                    filterState = filterState,
                    pagingData = Pager(PagingConfig(pageSize = 10)) {
                        rssRepository.get().searchArticles(
                            content = _viewState.value.searchContent.trim(),
                            groupId = _viewState.value.filterState?.group?.id,
                            feedId = _viewState.value.filterState?.feed?.id,
                            isStarred = _viewState.value.filterState?.filter?.isStarred() ?: false,
                            isUnread = _viewState.value.filterState?.filter?.isUnread() ?: false,
                        )
                    }.flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
                )
            }
        } else if (filterState != null) {
            _viewState.update {
                it.copy(
                    filterState = filterState,
                    pagingData = Pager(PagingConfig(pageSize = 10)) {
                        rssRepository.get().pullArticles(
                            groupId = filterState.group?.id,
                            feedId = filterState.feed?.id,
                            isStarred = filterState.filter.isStarred(),
                            isUnread = filterState.filter.isUnread(),
                        )
                    }.flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
                )
            }
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

    private fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        markAsReadBefore: MarkAsReadBefore
    ) {
        viewModelScope.launch {
            rssRepository.get().markAsRead(
                groupId = groupId,
                feedId = feedId,
                articleId = articleId,
                before = when (markAsReadBefore) {
                    MarkAsReadBefore.All -> null
                    MarkAsReadBefore.OneDay -> Calendar.getInstance().apply {
                        time = Date()
                        add(Calendar.DAY_OF_MONTH, -1)
                    }.time
                    MarkAsReadBefore.ThreeDays -> Calendar.getInstance().apply {
                        time = Date()
                        add(Calendar.DAY_OF_MONTH, -3)
                    }.time
                    MarkAsReadBefore.SevenDays -> Calendar.getInstance().apply {
                        time = Date()
                        add(Calendar.DAY_OF_MONTH, -7)
                    }.time
                },
                isUnread = false,
            )
        }
    }

    private fun inputSearchContent(content: String) {
        _viewState.update {
            it.copy(
                searchContent = content,
            )
        }
        fetchData(_viewState.value.filterState)
    }
}

data class ArticleViewState(
    val filterState: FilterState? = null,
    val filterImportant: Int = 0,
    val listState: LazyListState = LazyListState(),
    val isRefreshing: Boolean = false,
    val pagingData: Flow<PagingData<ArticleWithFeed>> = emptyFlow(),
    val syncWorkInfo: String = "",
    val searchContent: String = "",
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

    data class MarkAsRead(
        val groupId: String?,
        val feedId: String?,
        val articleId: String?,
        val markAsReadBefore: MarkAsReadBefore
    ) : FlowViewAction()

    data class InputSearchContent(
        val content: String,
    ) : FlowViewAction()
}

enum class MarkAsReadBefore {
    SevenDays,
    ThreeDays,
    OneDay,
    All,
}