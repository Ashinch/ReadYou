package me.ash.reader.ui.page.home

import androidx.lifecycle.ViewModel
import androidx.paging.*
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Group
import me.ash.reader.data.model.Filter
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
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _filterUiState = MutableStateFlow(FilterState())
    val filterUiState = _filterUiState.asStateFlow()

    val syncWorkLiveData = workManager.getWorkInfoByIdLiveData(SyncWorker.UUID)

    fun sync() {
        rssRepository.get().doSync()
    }

    fun changeFilter(filterState: FilterState) {
        _filterUiState.update {
            it.copy(
                group = filterState.group,
                feed = filterState.feed,
                filter = filterState.filter,
            )
        }
        fetchArticles()
    }

    fun fetchArticles() {
        _homeUiState.update {
            it.copy(
                pagingData = Pager(PagingConfig(pageSize = 50)) {
                    if (_homeUiState.value.searchContent.isNotBlank()) {
                        rssRepository.get().searchArticles(
                            content = _homeUiState.value.searchContent.trim(),
                            groupId = _filterUiState.value.group?.id,
                            feedId = _filterUiState.value.feed?.id,
                            isStarred = _filterUiState.value.filter.isStarred(),
                            isUnread = _filterUiState.value.filter.isUnread(),
                        )
                    } else {
                        rssRepository.get().pullArticles(
                            groupId = _filterUiState.value.group?.id,
                            feedId = _filterUiState.value.feed?.id,
                            isStarred = _filterUiState.value.filter.isStarred(),
                            isUnread = _filterUiState.value.filter.isUnread(),
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

    fun inputSearchContent(content: String) {
        _homeUiState.update {
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

data class HomeUiState(
    val pagingData: Flow<PagingData<FlowItemView>> = emptyFlow(),
    val searchContent: String = "",
)