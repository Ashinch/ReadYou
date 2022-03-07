package me.ash.reader.ui.page.home.feed

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.data.account.Account
import me.ash.reader.data.group.GroupWithFeed
import me.ash.reader.data.repository.AccountRepository
import me.ash.reader.data.repository.ArticleRepository
import me.ash.reader.data.repository.OpmlRepository
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val articleRepository: ArticleRepository,
    private val opmlRepository: OpmlRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(FeedViewState())
    val viewState: StateFlow<FeedViewState> = _viewState.asStateFlow()

    fun dispatch(action: FeedViewAction) {
        when (action) {
            is FeedViewAction.FetchAccount -> fetchAccount(action.callback)
            is FeedViewAction.FetchData -> fetchData(action.isStarred, action.isUnread)
            is FeedViewAction.AddFromFile -> addFromFile(action.inputStream)
            is FeedViewAction.ChangeFeedVisible -> changeFeedVisible(action.index)
            is FeedViewAction.ChangeGroupVisible -> changeGroupVisible(action.visible)
            is FeedViewAction.ScrollToItem -> scrollToItem(action.index)
        }
    }

    private fun fetchAccount(callback: () -> Unit = {}) {
        viewModelScope.launch {
            _viewState.update {
                it.copy(
                    account = accountRepository.getCurrentAccount()
                )
            }
            callback()
        }
    }

    private fun addFromFile(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            opmlRepository.saveToDatabase(inputStream)
            pullFeeds(isStarred = false, isUnread = false)
        }
    }

    private fun fetchData(isStarred: Boolean, isUnread: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            pullFeeds(isStarred, isUnread)
        }
    }

    private suspend fun pullFeeds(isStarred: Boolean, isUnread: Boolean) {
        combine(
            articleRepository.pullFeeds(),
            articleRepository.pullImportant(isStarred, isUnread),
        ) { groupWithFeedList, importantList ->
            val groupImportantMap = mutableMapOf<Int, Int>()
            val feedImportantMap = mutableMapOf<Int, Int>()
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
        }.onStart {

        }.onEach { groupWithFeedList ->
            _viewState.update {
                it.copy(
                    filterImportant = groupWithFeedList.sumOf { it.group.important ?: 0 },
                    groupWithFeedList = groupWithFeedList,
                    feedsVisible = List(groupWithFeedList.size, init = { true })
                )
            }
        }.catch {
            Log.e("RLog", "catch in articleRepository.pullFeeds(): $this")
        }.collect()
    }

    private fun changeFeedVisible(index: Int) {
        _viewState.update {
            it.copy(
                feedsVisible = _viewState.value.feedsVisible.toMutableList().apply {
                    this[index] = !this[index]
                }
            )
        }
    }

    private fun changeGroupVisible(visible: Boolean) {
        _viewState.update {
            it.copy(
                groupsVisible = visible
            )
        }
    }

    private fun scrollToItem(index: Int) {
        viewModelScope.launch {
            _viewState.value.listState.scrollToItem(index)
        }
    }
}

data class FeedViewState(
    val account: Account? = null,
    val filterImportant: Int = 0,
    val groupWithFeedList: List<GroupWithFeed> = emptyList(),
    val feedsVisible: List<Boolean> = emptyList(),
    val listState: LazyListState = LazyListState(),
    val groupsVisible: Boolean = true,
)

sealed class FeedViewAction {
    data class FetchData(
        val isStarred: Boolean,
        val isUnread: Boolean,
    ) : FeedViewAction()

    data class FetchAccount(
        val callback: () -> Unit = {},
    ) : FeedViewAction()

    data class AddFromFile(
        val inputStream: InputStream
    ) : FeedViewAction()

    data class ChangeFeedVisible(
        val index: Int
    ) : FeedViewAction()

    data class ChangeGroupVisible(
        val visible: Boolean
    ) : FeedViewAction()

    data class ScrollToItem(
        val index: Int
    ) : FeedViewAction()
}