package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.StringsRepository
import me.ash.reader.ui.page.home.FilterState
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FlowViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val stringsRepository: StringsRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ArticleViewState())
    val viewState: StateFlow<ArticleViewState> = _viewState.asStateFlow()

    fun dispatch(action: FlowViewAction) {
        when (action) {
            is FlowViewAction.Sync -> sync()
            is FlowViewAction.FetchData -> fetchData(action.filterState)
            is FlowViewAction.ChangeIsBack -> changeIsBack(action.isBack)
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

    private fun sync() {
        rssRepository.get().doSync()
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
                    }.flow.map {
                        it.map {
                            FlowItemView.Article(it)
                        }.insertSeparators { before, after ->
                            val beforeDate =
                                stringsRepository.formatAsString(before?.articleWithFeed?.article?.date)
                            val afterDate =
                                stringsRepository.formatAsString(after?.articleWithFeed?.article?.date)
                            if (beforeDate != afterDate) {
                                afterDate?.let { FlowItemView.Date(it) }
                            } else {
                                null
                            }
                        }
                    }.cachedIn(viewModelScope)
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
                    }.flow.map {
                        it.map {
                            FlowItemView.Article(it)
                        }.insertSeparators { before, after ->
                            val beforeDate =
                                stringsRepository.formatAsString(before?.articleWithFeed?.article?.date)
                            val afterDate =
                                stringsRepository.formatAsString(after?.articleWithFeed?.article?.date)
                            if (beforeDate != afterDate) {
                                afterDate?.let { FlowItemView.Date(it) }
                            } else {
                                null
                            }
                        }
                    }.cachedIn(viewModelScope)
                )
            }
        }
    }

    private fun scrollToItem(index: Int) {
        viewModelScope.launch {
            _viewState.value.listState.scrollToItem(index)
        }
    }

    private fun changeIsBack(isBack: Boolean) {
        _viewState.update {
            it.copy(isBack = isBack)
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
    val isBack: Boolean = false,
    val pagingData: Flow<PagingData<FlowItemView>> = emptyFlow(),
    val syncWorkInfo: String = "",
    val searchContent: String = "",
)

sealed class FlowViewAction {
    object Sync : FlowViewAction()

    data class FetchData(
        val filterState: FilterState,
    ) : FlowViewAction()

    data class ChangeIsBack(
        val isBack: Boolean
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

sealed class FlowItemView {
    class Article(val articleWithFeed: ArticleWithFeed) : FlowItemView()
    class Date(val date: String) : FlowItemView()
}