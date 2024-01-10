package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.model.account.Account
import me.ash.reader.data.module.DefaultDispatcher
import me.ash.reader.data.module.IODispatcher
import me.ash.reader.data.repository.AccountRepository
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.StringsRepository
import me.ash.reader.ui.page.home.FilterState
import javax.inject.Inject

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val rssRepository: RssRepository,
    private val stringsRepository: StringsRepository,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _feedsUiState = MutableStateFlow(FeedsUiState())
    val feedsUiState: StateFlow<FeedsUiState> = _feedsUiState.asStateFlow()

    fun fetchAccount() {
        viewModelScope.launch(ioDispatcher) {
            _feedsUiState.update { it.copy(account = accountRepository.getCurrentAccount()) }
        }
    }

    fun pullFeeds(filterState: FilterState) {
        val isStarred = filterState.filter.isStarred()
        val isUnread = filterState.filter.isUnread()
        _feedsUiState.update {
            it.copy(
                importantSum = rssRepository.get().pullImportant(isStarred, isUnread)
                    .mapLatest {
                        (it["sum"] ?: 0).run {
                            stringsRepository.getQuantityString(
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
                    rssRepository.get().pullImportant(isStarred, isUnread),
                    rssRepository.get().pullFeeds()
                ) { importantMap, groupWithFeedList ->
                    val groupIterator = groupWithFeedList.iterator()
                    while (groupIterator.hasNext()) {
                        val groupWithFeed = groupIterator.next()
                        val groupImportant = importantMap[groupWithFeed.group.id] ?: 0
                        if ((isStarred || isUnread) && groupImportant == 0) {
                            groupIterator.remove()
                            continue
                        }
                        groupWithFeed.group.important = groupImportant
                        val feedIterator = groupWithFeed.feeds.iterator()
                        while (feedIterator.hasNext()) {
                            val feed = feedIterator.next()
                            val feedImportant = importantMap[feed.id] ?: 0
                            if ((isStarred || isUnread) && feedImportant == 0) {
                                feedIterator.remove()
                                continue
                            }
                            feed.important = feedImportant
                        }
                    }
                    groupWithFeedList
                }.mapLatest { groupWithFeedList ->
                    groupWithFeedList.map {
                        mutableListOf<GroupFeedsView>(GroupFeedsView.Group(it.group)).apply {
                            addAll(
                                it.feeds.map {
                                    GroupFeedsView.Feed(it)
                                }
                            )
                        }
                    }.flatten()
                }.flowOn(defaultDispatcher),
            )
        }
    }
}

data class FeedsUiState(
    val account: Account? = null,
    val importantSum: Flow<String> = emptyFlow(),
    val groupWithFeedList: Flow<List<GroupFeedsView>> = emptyFlow(),
    val listState: LazyListState = LazyListState(),
    val groupsVisible: SnapshotStateMap<String, Boolean> = mutableStateMapOf(),
)

sealed class GroupFeedsView {
    class Group(val group: me.ash.reader.data.model.group.Group) : GroupFeedsView()
    class Feed(val feed: me.ash.reader.data.model.feed.Feed) : GroupFeedsView()
}
