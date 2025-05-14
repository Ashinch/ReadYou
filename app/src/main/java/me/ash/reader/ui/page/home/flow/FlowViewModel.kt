package me.ash.reader.ui.page.home.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.LazyPagingItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.article.ArticleFlowItem
import me.ash.reader.domain.model.general.MarkAsReadConditions
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.cache.DiffMapHolder
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FlowViewModel @Inject constructor(
    private val rssService: RssService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val diffMapHolder: DiffMapHolder,
) : ViewModel() {

    private val _flowUiState = MutableStateFlow(FlowUiState())
    val flowUiState: StateFlow<FlowUiState> = _flowUiState.asStateFlow()

    fun sync() {
        applicationScope.launch(ioDispatcher) {
            rssService.get().doSyncOneTime()
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
        lazyPagingItems: LazyPagingItems<ArticleFlowItem>,
    ) {
        viewModelScope.launch(ioDispatcher) {
            lazyPagingItems.itemSnapshotList.asSequence()
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
}

data class FlowUiState(
    val lastReadIndex: Int? = null
)