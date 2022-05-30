package me.ash.reader.ui.page.home.feeds

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.entity.Account
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
    private val _feedsUiState = MutableStateFlow(FeedsUiState())
    val feedsUiState: StateFlow<FeedsUiState> = _feedsUiState.asStateFlow()

    fun fetchAccount() {
        viewModelScope.launch(dispatcherIO) {
            _feedsUiState.update {
                it.copy(
                    account = accountRepository.getCurrentAccount()
                )
            }
        }
    }

    fun exportAsOpml(callback: (String) -> Unit = {}) {
        viewModelScope.launch(dispatcherDefault) {
            try {
                callback(opmlRepository.saveToString())
            } catch (e: Exception) {
                Log.e("FeedsViewModel", "exportAsOpml: ", e)
            }
        }
    }

    fun fetchData(filterState: FilterState) {
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
        ) { groupWithFeedList, importantMap ->
            groupWithFeedList.fastForEach {
                var groupImportant = 0
                it.feeds.fastForEach {
                    it.important = importantMap[it.id]
                    groupImportant += it.important ?: 0
                }
                it.group.important = groupImportant
            }
            groupWithFeedList
        }.mapLatest { groupWithFeedList ->
            _feedsUiState.update {
                it.copy(
                    importantSum = groupWithFeedList.sumOf { it.group.important ?: 0 }.run {
                        when {
                            isStarred -> stringsRepository.getQuantityString(
                                R.plurals.starred_desc,
                                this,
                                this
                            )
                            isUnread -> stringsRepository.getQuantityString(
                                R.plurals.unread_desc,
                                this,
                                this
                            )
                            else -> stringsRepository.getQuantityString(
                                R.plurals.all_desc,
                                this,
                                this
                            )
                        }
                    },
                    groupWithFeedList = groupWithFeedList.map {
                        mutableListOf<GroupFeedsView>(GroupFeedsView.Group(it.group)).apply {
                            addAll(
                                it.feeds.map {
                                    GroupFeedsView.Feed(it)
                                }
                            )
                        }
                    }.flatten(),
                )
            }
        }.catch {
            Log.e("RLog", "catch in articleRepository.pullFeeds(): ${it.message}")
        }.flowOn(dispatcherDefault).collect()
    }
}

data class FeedsUiState(
    val account: Account? = null,
    val importantSum: String = "",
    val groupWithFeedList: List<GroupFeedsView> = emptyList(),
    val listState: LazyListState = LazyListState(),
    val groupsVisible: Boolean = true,
)

sealed class GroupFeedsView {
    class Group(val group: me.ash.reader.data.entity.Group) : GroupFeedsView()
    class Feed(val feed: me.ash.reader.data.entity.Feed) : GroupFeedsView()
}