package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.getDefaultGroupId
import me.ash.reader.ui.page.home.FilterState
import javax.inject.Inject

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val rssService: RssService,
    private val androidStringsHelper: AndroidStringsHelper,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _feedsUiState = MutableStateFlow(FeedsUiState())
    val feedsUiState: StateFlow<FeedsUiState> = _feedsUiState.asStateFlow()

    fun fetchAccount() {
        viewModelScope.launch(ioDispatcher) {
            _feedsUiState.update { it.copy(account = accountService.getCurrentAccount()) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun pullFeeds(filterState: FilterState, hideEmptyGroups: Boolean) {
        val isStarred = filterState.filter.isStarred()
        val isUnread = filterState.filter.isUnread()
        _feedsUiState.update {
            val important = rssService.get().pullImportant(isStarred, isUnread)
            it.copy(
                importantSum = important
                    .mapLatest {
                        (it["sum"] ?: 0).run {
                            androidStringsHelper.getQuantityString(
                                when {
                                    isStarred -> R.plurals.starred_desc
                                    isUnread -> R.plurals.unread_desc
                                    else -> R.plurals.all_desc
                                },
                                this,
                                this
                            )
                        }
                    }.flowOn(defaultDispatcher),
                groupWithFeedList = combine(
                    important,
                    rssService.get().pullFeeds()
                ) { importantMap, groupWithFeedList ->
                    val groupIterator = groupWithFeedList.iterator()
                    while (groupIterator.hasNext()) {
                        val groupWithFeed = groupIterator.next()
                        val groupImportant = importantMap[groupWithFeed.group.id] ?: 0
                        if (hideEmptyGroups && (isStarred || isUnread) && groupImportant == 0) {
                            groupIterator.remove()
                            continue
                        }
                        groupWithFeed.group.important = groupImportant
                        val feedIterator = groupWithFeed.feeds.iterator()
                        while (feedIterator.hasNext()) {
                            val feed = feedIterator.next()
                            val feedImportant = importantMap[feed.id] ?: 0
                            groupWithFeed.group.feeds++
                            if (hideEmptyGroups && (isStarred || isUnread) && feedImportant == 0) {
                                feedIterator.remove()
                                continue
                            }
                            feed.important = feedImportant
                        }
                    }
                    groupWithFeedList
                }.mapLatest { list ->
                    list.filter { (group, feeds) ->
                        group.id != feedsUiState.value.account?.id?.getDefaultGroupId() || feeds.size > 0
                    }
                }.flowOn(defaultDispatcher),
            )
        }
    }
}

data class FeedsUiState(
    val account: Account? = null,
    val importantSum: Flow<String> = emptyFlow(),
    val groupWithFeedList: Flow<List<GroupWithFeed>> = emptyFlow(),
    val listState: LazyListState = LazyListState(),
    val groupsVisible: SnapshotStateMap<String, Boolean> = mutableStateMapOf(),
)
