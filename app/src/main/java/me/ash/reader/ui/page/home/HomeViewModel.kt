package me.ash.reader.ui.page.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.data.model.article.ArticleFlowItem
import me.ash.reader.data.model.article.mapPagingFlowItem
import me.ash.reader.data.model.feed.Feed
import me.ash.reader.data.model.general.Filter
import me.ash.reader.data.model.group.Group
import me.ash.reader.data.module.ApplicationScope
import me.ash.reader.data.module.IODispatcher
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.StringsRepository
import me.ash.reader.data.repository.SyncWorker
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val stringsRepository: StringsRepository,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    private val workManager: WorkManager,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _filterUiState = MutableStateFlow(FilterState())
    val filterUiState = _filterUiState.asStateFlow()

    fun syncWorkLiveData(): LiveData<WorkInfo>? =
        SyncWorker.uuid?.let {
            workManager.getWorkInfoByIdLiveData(it)
        }

    fun sync() {
        viewModelScope.launch(ioDispatcher) {
            rssRepository.get().doSync()
        }
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
                pagingData = Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        enablePlaceholders = false,
                    )
                ) {
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
                    it.mapPagingFlowItem(stringsRepository)
                }.cachedIn(applicationScope)
            )
        }
    }

    fun inputSearchContent(content: String) {
        _homeUiState.update { it.copy(searchContent = content) }
        fetchArticles()
    }
}

data class FilterState(
    val group: Group? = null,
    val feed: Feed? = null,
    val filter: Filter = Filter.All,
)

data class HomeUiState(
    val pagingData: Flow<PagingData<ArticleFlowItem>> = emptyFlow(),
    val searchContent: String = "",
)
