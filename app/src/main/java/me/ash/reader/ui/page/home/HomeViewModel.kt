package me.ash.reader.ui.page.home

import androidx.lifecycle.ViewModel
import androidx.paging.*
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.model.Filter
import me.ash.reader.data.entity.Group
import me.ash.reader.data.module.ApplicationScope
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.StringsRepository
import me.ash.reader.data.repository.SyncWorker
import me.ash.reader.ui.page.home.flow.FlowItemView
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val stringsRepository: StringsRepository,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _viewState = MutableStateFlow(HomeViewState())
    val viewState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    val syncWorkLiveData = workManager.getWorkInfoByIdLiveData(SyncWorker.UUID)

    fun dispatch(action: HomeViewAction) {
        when (action) {
            is HomeViewAction.Sync -> sync()
            is HomeViewAction.ChangeFilter -> changeFilter(action.filterState)
            is HomeViewAction.FetchArticles -> fetchArticles()
            is HomeViewAction.InputSearchContent -> inputSearchContent(action.content)
        }
    }

    private fun sync() {
        rssRepository.get().doSync()
    }

    private fun changeFilter(filterState: FilterState) {
        _filterState.update {
            it.copy(
                group = filterState.group,
                feed = filterState.feed,
                filter = filterState.filter,
            )
        }
        fetchArticles()
    }

    private fun fetchArticles() {
        _viewState.update {
            it.copy(
                pagingData = Pager(PagingConfig(pageSize = 50)) {
                    if (_viewState.value.searchContent.isNotBlank()) {
                        rssRepository.get().searchArticles(
                            content = _viewState.value.searchContent.trim(),
                            groupId = _filterState.value.group?.id,
                            feedId = _filterState.value.feed?.id,
                            isStarred = _filterState.value.filter.isStarred(),
                            isUnread = _filterState.value.filter.isUnread(),
                        )
                    } else {
                        rssRepository.get().pullArticles(
                            groupId = _filterState.value.group?.id,
                            feedId = _filterState.value.feed?.id,
                            isStarred = _filterState.value.filter.isStarred(),
                            isUnread = _filterState.value.filter.isUnread(),
                        )
                    }
                }.flow.map {
                    it.map { FlowItemView.Article(it) }.insertSeparators { before, after ->
                        val beforeDate =
                            stringsRepository.formatAsString(before?.articleWithFeed?.article?.date)
                        val afterDate =
                            stringsRepository.formatAsString(after?.articleWithFeed?.article?.date)
                        if (beforeDate != afterDate) {
                            afterDate?.let { FlowItemView.Date(it, beforeDate != null) }
                        } else {
                            null
                        }
                    }
                }.cachedIn(applicationScope)
            )
        }
    }

    private fun inputSearchContent(content: String) {
        _viewState.update {
            it.copy(
                searchContent = content,
            )
        }
        fetchArticles()
    }
}

data class FilterState(
    val group: Group? = null,
    val feed: Feed? = null,
    val filter: Filter = Filter.All,
)

data class HomeViewState(
    val pagingData: Flow<PagingData<FlowItemView>> = emptyFlow(),
    val searchContent: String = "",
)

sealed class HomeViewAction {
    object Sync : HomeViewAction()

    data class ChangeFilter(
        val filterState: FilterState
    ) : HomeViewAction()

    object FetchArticles : HomeViewAction()

    data class InputSearchContent(
        val content: String,
    ) : HomeViewAction()
}