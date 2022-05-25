package me.ash.reader.ui.page.home.feeds

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.entity.Account
import me.ash.reader.data.entity.GroupWithFeed
import me.ash.reader.data.module.DispatcherDefault
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.repository.AccountRepository
import me.ash.reader.data.repository.OpmlRepository
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.StringsRepository
import me.ash.reader.ui.page.home.FilterState
import javax.inject.Inject

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val rssRepository: RssRepository,
    private val opmlRepository: OpmlRepository,
    private val stringsRepository: StringsRepository,
    @DispatcherDefault
    private val dispatcherDefault: CoroutineDispatcher,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    private val _viewState = MutableStateFlow(FeedsViewState())
    val viewState: StateFlow<FeedsViewState> = _viewState.asStateFlow()

    fun dispatch(action: FeedsViewAction) {
        when (action) {
            is FeedsViewAction.FetchAccount -> fetchAccount()
            is FeedsViewAction.FetchData -> fetchData(action.filterState)
            is FeedsViewAction.ExportAsString -> exportAsOpml(action.callback)
            is FeedsViewAction.ScrollToItem -> scrollToItem(action.index)
        }
    }

    private fun fetchAccount() {
        viewModelScope.launch(dispatcherIO) {
            _viewState.update {
                it.copy(
                    account = accountRepository.getCurrentAccount()
                )
            }
        }
    }

    private fun exportAsOpml(callback: (String) -> Unit = {}) {
        viewModelScope.launch(dispatcherDefault) {
            try {
                callback(opmlRepository.saveToString())
            } catch (e: Exception) {
                Log.e("FeedsViewModel", "exportAsOpml: ", e)
            }
        }
    }

    private fun fetchData(filterState: FilterState) {
        viewModelScope.launch(dispatcherIO) {
            pullFeeds(
                isStarred = filterState.filter.isStarred(),
                isUnread = filterState.filter.isUnread(),
            )
        }
    }

    private suspend fun pullFeeds(isStarred: Boolean, isUnread: Boolean) {
        combine(
            rssRepository.get().pullFeeds(),
            rssRepository.get().pullImportant(isStarred, isUnread),
        ) { groupWithFeedList, importantList ->
            val groupImportantMap = mutableMapOf<String, Int>()
            val feedImportantMap = mutableMapOf<String, Int>()
            importantList.groupBy { it.groupId }.forEach { (i, list) ->
                var groupImportantSum = 0
                list.forEach {
                    feedImportantMap[it.feedId] = it.important
                    groupImportantSum += it.important
                }
                groupImportantMap[i] = groupImportantSum
            }
            val groupsIt = groupWithFeedList.iterator()
            while (groupsIt.hasNext()) {
                val groupWithFeed = groupsIt.next()
                val groupImportant = groupImportantMap[groupWithFeed.group.id]
                if (groupImportant == null && (isStarred || isUnread)) {
                    groupsIt.remove()
                } else {
                    groupWithFeed.group.important = groupImportant
                    val feedsIt = groupWithFeed.feeds.iterator()
                    while (feedsIt.hasNext()) {
                        val feed = feedsIt.next()
                        val feedImportant = feedImportantMap[feed.id]
                        if (feedImportant == null && (isStarred || isUnread)) {
                            feedsIt.remove()
                        } else {
                            feed.important = feedImportant
                        }
                    }
                }
            }
            groupWithFeedList
        }.onEach { groupWithFeedList ->
            _viewState.update {
                it.copy(
                    importantCount = groupWithFeedList.sumOf { it.group.important ?: 0 }.run {
                        when {
                            isStarred -> stringsRepository.getQuantityString(R.plurals.starred_desc, this, this)
                            isUnread -> stringsRepository.getQuantityString(R.plurals.unread_desc, this, this)
                            else -> stringsRepository.getQuantityString(R.plurals.all_desc, this, this)
                        }
                    },
                    groupWithFeedList = groupWithFeedList,
                    feedsVisible = List(groupWithFeedList.size, init = { true })
                )
            }
        }.catch {
            Log.e("RLog", "catch in articleRepository.pullFeeds(): ${it.message}")
        }.flowOn(dispatcherDefault).collect()
    }

    private fun scrollToItem(index: Int) {
        viewModelScope.launch {
            _viewState.value.listState.scrollToItem(index)
        }
    }
}

data class FeedsViewState(
    val account: Account? = null,
    val importantCount: String = "",
    val groupWithFeedList: List<GroupWithFeed> = emptyList(),
    val feedsVisible: List<Boolean> = emptyList(),
    val listState: LazyListState = LazyListState(),
    val groupsVisible: Boolean = true,
)

sealed class FeedsViewAction {
    data class FetchData(
        val filterState: FilterState,
    ) : FeedsViewAction()

    object FetchAccount : FeedsViewAction()

    data class ExportAsString(
        val callback: (String) -> Unit = {}
    ) : FeedsViewAction()

    data class ScrollToItem(
        val index: Int
    ) : FeedsViewAction()
}