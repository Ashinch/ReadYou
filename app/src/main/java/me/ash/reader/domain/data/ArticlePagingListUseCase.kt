package me.ash.reader.domain.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.preferencesOf
import androidx.paging.ItemSnapshotList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.article.mapPagingFlowItem
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import javax.inject.Inject
import kotlin.text.trim

class ArticlePagingListUseCase @Inject constructor(
    private val rssService: RssService,
    private val androidStringsHelper: AndroidStringsHelper,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider,
    val diffMapHolder: DiffMapHolder,
) {

    private val mutablePagerFlow = MutableStateFlow<Flow<PagingData<ArticleFlowItem>>>(emptyFlow())
    val pagerFlow: StateFlow<Flow<PagingData<ArticleFlowItem>>> = mutablePagerFlow

    var itemSnapshotList by mutableStateOf(
        ItemSnapshotList<ArticleFlowItem>(
            placeholdersBefore = 0,
            placeholdersAfter = 0,
            items = emptyList()
        )
    )
        private set


    val pagingDataPresenter = object : PagingDataPresenter<ArticleFlowItem>() {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<ArticleFlowItem>) {
            itemSnapshotList = snapshot()
        }
    }

    private val _filterUiState =
        MutableStateFlow(FilterState(filter = settingsProvider.settings.initialFilter.toFilter()))
    val filterStateFlow = _filterUiState.asStateFlow()
    private val filterState get() = filterStateFlow.value

    fun updateFilterState(
        feed: Feed? = filterState.feed,
        group: Group? = filterState.group,
        filter: Filter = filterState.filter,
        searchContent: String? = filterState.searchContent,
    ) {
        _filterUiState.update {
            it.copy(
                feed = feed,
                group = group,
                searchContent = searchContent,
                filter = filter
            )
        }
    }

    init {
        applicationScope.launch(ioDispatcher) {
            filterStateFlow.collect { filterState ->
                val searchContent = filterState.searchContent

                mutablePagerFlow.value = Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        enablePlaceholders = false,
                    )
                ) {
                    if (!searchContent.isNullOrBlank()) {
                        rssService.get().searchArticles(
                            content = searchContent.trim(),
                            groupId = filterState.group?.id,
                            feedId = filterState.feed?.id,
                            isStarred = filterState.filter.isStarred(),
                            isUnread = filterState.filter.isUnread(),
                            sortAscending = settingsProvider.settings.flowSortUnreadArticles.value
                        )
                    } else {
                        rssService.get().pullArticles(
                            groupId = filterState.group?.id,
                            feedId = filterState.feed?.id,
                            isStarred = filterState.filter.isStarred(),
                            isUnread = filterState.filter.isUnread(),
                            sortAscending = settingsProvider.settings.flowSortUnreadArticles.value
                        )
                    }
                }.flow.map { it.mapPagingFlowItem(androidStringsHelper) }
                    .cachedIn(applicationScope)
            }
        }
        applicationScope.launch {
            pagerFlow.collectLatest {
                it.collectLatest {
                    pagingDataPresenter.collectFrom(it)
                }
            }
        }
    }
}

data class FilterState(
    val group: Group? = null,
    val feed: Feed? = null,
    val filter: Filter = Filter.All,
    val searchContent: String? = null,
)