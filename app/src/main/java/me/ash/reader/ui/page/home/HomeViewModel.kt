package me.ash.reader.ui.page.home

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.mapPagingFlowItem
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.service.RssService
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.page.home.flow.Diff
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rssService: RssService,
    private val androidStringsHelper: AndroidStringsHelper,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    private val workManager: WorkManager,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _filterUiState = MutableStateFlow(FilterState())
    val filterUiState = _filterUiState.asStateFlow()

    val syncWorkLiveData = workManager.getWorkInfosByTagLiveData(SyncWorker.WORK_TAG)

    val diffMap = mutableStateMapOf<String, Diff>()

    fun sync() {
        applicationScope.launch(ioDispatcher) {
            rssService.get().doSyncOneTime(null, null)
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
                        rssService.get().searchArticles(
                            content = _homeUiState.value.searchContent.trim(),
                            groupId = _filterUiState.value.group?.id,
                            feedId = _filterUiState.value.feed?.id,
                            isStarred = _filterUiState.value.filter.isStarred(),
                            isUnread = _filterUiState.value.filter.isUnread(),
                            sortAscending = settingsProvider.settings.flowSortUnreadArticles.value
                        )
                    } else {
                        rssService.get().pullArticles(
                            groupId = _filterUiState.value.group?.id,
                            feedId = _filterUiState.value.feed?.id,
                            isStarred = _filterUiState.value.filter.isStarred(),
                            isUnread = _filterUiState.value.filter.isUnread(),
                            sortAscending = settingsProvider.settings.flowSortUnreadArticles.value
                        )
                    }
                }.flow.map {
                    it.mapPagingFlowItem(androidStringsHelper)
                }.cachedIn(applicationScope)
            )
        }
    }

    fun inputSearchContent(content: String) {
        _homeUiState.update { it.copy(searchContent = content) }
        fetchArticles()
    }


    fun commitDiff() {
        applicationScope.launch(ioDispatcher) {
            val markAsReadArticles =
                diffMap.filter { !it.value.isUnread }.map { it.key }.toSet()
            val markAsUnreadArticles =
                diffMap.filter { it.value.isUnread }.map { it.key }.toSet()

            rssService.get()
                .batchMarkAsRead(articleIds = markAsReadArticles, isUnread = false)
            rssService.get()
                .batchMarkAsRead(articleIds = markAsUnreadArticles, isUnread = true)

        }.invokeOnCompletion { diffMap.clear() }
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
