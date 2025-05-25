package me.ash.reader.ui.page.home.flow

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.LazyPagingItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.data.ArticlePagingListUseCase
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.general.MarkAsReadConditions
import me.ash.reader.domain.service.RssService
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterState
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import java.util.Date
import javax.inject.Inject

private const val TAG = "FlowViewModel"

@HiltViewModel
class FlowViewModel @Inject constructor(
    private val rssService: RssService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val diffMapHolder: DiffMapHolder,
    private val articlePagingListUseCase: ArticlePagingListUseCase,
    private val filterStateUseCase: FilterStateUseCase,
    private val groupWithFeedsListUseCase: GroupWithFeedsListUseCase,
    private val settingsProvider: SettingsProvider,
) : ViewModel() {

    private val _flowUiState = MutableStateFlow(FlowUiState())
    val flowUiState: StateFlow<FlowUiState> = _flowUiState.asStateFlow()

    init {
        viewModelScope.launch {

            filterStateUseCase.filterStateFlow.combine(groupWithFeedsListUseCase.groupWithFeedListFlow) { filterState, groupWithFeedsList ->
                var nextFilterState: FilterState? = null
                if (filterState.group != null) {
                    val groupList = groupWithFeedsList.map { it.group }
                    val index =
                        groupList.indexOfFirst { it.id == filterState.group.id }
                    if (index != -1) {
                        val nextGroup = groupList.getOrNull(index + 1)
                        if (nextGroup != null) {
                            nextFilterState = filterState.copy(group = nextGroup)
                        }
                    }
                } else if (filterState.feed != null) {
                    val feedList = groupWithFeedsList.flatMap { it.feeds }
                    val index = feedList.indexOfFirst { it.id == filterState.feed.id }
                    if (index != -1) {
                        val nextFeed = feedList.getOrNull(index + 1)
                        if (nextFeed != null) {
                            nextFilterState = filterState.copy(feed = nextFeed)
                        }
                    }
                }
                nextFilterState
            }
                .collect { filterState -> _flowUiState.update { it.copy(nextFilterState = filterState) } }
        }
    }

    fun updateReadStatus(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        conditions: MarkAsReadConditions,
        isUnread: Boolean,
    ) {
        applicationScope.launch(ioDispatcher) {
            rssService.get().markAsRead(
                groupId = groupId,
                feedId = feedId,
                articleId = articleId,
                before = conditions.toDate(),
                isUnread = isUnread,
            )
        }
    }

    fun updateStarredStatus(
        articleId: String?,
        isStarred: Boolean,
    ) {
        applicationScope.launch(ioDispatcher) {
            if (articleId != null) {
                rssService.get().markAsStarred(
                    articleId = articleId,
                    isStarred = isStarred,
                )
            }
        }
    }

    fun markAsReadFromListByDate(
        date: Date,
        isBefore: Boolean,
    ) {
        viewModelScope.launch(ioDispatcher) {
            articlePagingListUseCase.itemSnapshotList.asSequence()
                .filterIsInstance<ArticleFlowItem.Article>().map { it.articleWithFeed }
                .filter {
                    if (isBefore) {
                        date > it.article.date && it.article.isUnread
                    } else {
                        date < it.article.date && it.article.isUnread
                    }
                }.distinctBy { it.article.id }.forEach { articleWithFeed ->
                    diffMapHolder.updateDiff(articleWithFeed = articleWithFeed, isUnread = false)
                }
        }
    }

    fun updateLastReadIndex(index: Int?) {
        _flowUiState.update { it.copy(lastReadIndex = index) }
    }

    fun loadNextFeedOrGroup() {
        viewModelScope.launch {
            if (settingsProvider.settings.markAsReadOnScroll.value) {
                articlePagingListUseCase.itemSnapshotList.items.forEach {
                    if (it is ArticleFlowItem.Article) {
                        diffMapHolder.updateDiff(
                            articleWithFeed = it.articleWithFeed,
                            isUnread = false
                        )
                    }
                }
            }
            flowUiState.value.nextFilterState?.let { filterStateUseCase.updateFilterState(it) }
        }
    }
}

data class FlowUiState(
    val lastReadIndex: Int? = null,
    val nextFilterState: FilterState? = null
)