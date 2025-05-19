package me.ash.reader.ui.page.home.feeds

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.getDefaultGroupId
import javax.inject.Inject

private const val TAG = "FeedsViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val rssService: RssService,
    private val androidStringsHelper: AndroidStringsHelper,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider,
    private val diffMapHolder: DiffMapHolder,
) : ViewModel() {

    private val _feedsUiState =
        MutableStateFlow(FeedsUiState(filter = settingsProvider.settings.initialFilter.toFilter()))
    val feedsUiState: StateFlow<FeedsUiState> = _feedsUiState.asStateFlow()

    private val _groupWithFeedsListFlow: MutableStateFlow<List<GroupWithFeed>> =
        MutableStateFlow<List<GroupWithFeed>>(emptyList())
    val groupWithFeedListFlow: StateFlow<List<GroupWithFeed>> = _groupWithFeedsListFlow

    private val hideEmptyGroups get() = settingsProvider.settings.hideEmptyGroups.value
    private val defaultGroupId get() = feedsUiState.value.account?.id?.getDefaultGroupId()
    private val feedsFlow = rssService.get().pullFeeds()

    var currentJob: Job? = null

    fun fetchAccount() {
        viewModelScope.launch(ioDispatcher) {
            _feedsUiState.update { it.copy(account = accountService.getCurrentAccount()) }
        }
    }

    fun switchFilter(filter: Filter) {
        viewModelScope.launch {
            _feedsUiState.update { it.copy(filter = filter) }
        }
    }

    init {
        viewModelScope.launch {
            feedsUiState.mapLatest { it.filter }.distinctUntilChanged().collect {
                Log.d(TAG, "Recollect!")
                currentJob?.cancel()
                currentJob = when (it) {
                    Filter.Unread -> pullUnreadFeeds()
                    Filter.Starred -> pullStarredFeeds()
                    else -> pullAllFeeds()
                }
            }
        }
    }

    private fun pullAllFeeds(): Job {
        val articleCountMapFlow =
            rssService.get().pullImportant(isStarred = false, isUnread = false)

        return viewModelScope.launch {
            launch {
                articleCountMapFlow.mapLatest {
                    val sum = it["sum"] ?: 0
                    androidStringsHelper.getQuantityString(R.plurals.all_desc, sum, sum)
                }.flowOn(defaultDispatcher).collect { text ->
                    _feedsUiState.update { it.copy(importantSum = text) }
                }
            }

            launch {
                feedsFlow.combine(articleCountMapFlow) { groupWithFeedsList, articleCountMap ->
                    val result = mutableListOf<GroupWithFeed>()
                    for (groupItem in groupWithFeedsList) {

                        val groupCount = articleCountMap[groupItem.group.id] ?: 0
                        groupItem.group.important = groupCount
                        groupItem.group.feeds = groupItem.feeds.size

                        for (feed in groupItem.feeds) {
                            feed.important = articleCountMap[feed.id] ?: 0
                        }

                        if (groupItem.group.id != defaultGroupId || groupItem.feeds.isNotEmpty()) {
                            result.add(groupItem)
                        }
                    }
                    result
                }.flowOn(defaultDispatcher).collect { _groupWithFeedsListFlow.value = it }
            }
        }
    }

    private fun pullStarredFeeds(): Job {
        val starredCountMap = rssService.get().pullImportant(isStarred = true, isUnread = false)

        return viewModelScope.launch {
            launch {
                starredCountMap.mapLatest {
                    val sum = it["sum"] ?: 0
                    androidStringsHelper.getQuantityString(R.plurals.starred_desc, sum, sum)
                }.flowOn(defaultDispatcher).collect { text ->
                    _feedsUiState.update { it.copy(importantSum = text) }
                }
            }

            launch {
                feedsFlow.combine(starredCountMap) { groupWithFeedsList, starredCountMap ->
                    val result = mutableListOf<GroupWithFeed>()
                    for (groupItem in groupWithFeedsList) {
                        val groupItem = groupItem.copy()

                        val groupCount = starredCountMap[groupItem.group.id] ?: 0

                        if (!hideEmptyGroups) {
                            groupItem.feeds.forEach { feed ->
                                val feedCount = (starredCountMap[feed.id] ?: 0)
                                feed.important = feedCount
                            }
                            groupItem.group.feeds = groupItem.feeds.size
                            groupItem.group.important = groupCount
                        } else if (groupCount > 0) {
                            groupItem.feeds.removeAll { feed ->
                                val feedCount = (starredCountMap[feed.id] ?: 0)
                                if (feedCount != 0) feed.important = feedCount
                                return@removeAll feedCount == 0
                            }
                            groupItem.group.feeds = groupItem.feeds.size
                            groupItem.group.important = groupCount
                        } else {
                            continue
                        }

                        if (groupItem.group.id != defaultGroupId || groupItem.feeds.isNotEmpty()) {
                            result.add(groupItem)
                        }
                    }
                    result
                }.flowOn(defaultDispatcher).collect {
                    _groupWithFeedsListFlow.value = it
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun pullUnreadFeeds(): Job {
        val unreadCountMapFlow = rssService.get().pullImportant(isStarred = false, isUnread = true)

        return viewModelScope.launch {
            launch {
                diffMapHolder.diffMapSnapshotFlow
                    .combine(
                        unreadCountMapFlow
                    ) { diffMap, unreadCountMap ->
                        val sum = unreadCountMap["sum"] ?: 0
                        val combinedSum =
                            sum + diffMap.values.sumOf { if (it.isUnread) 1.toInt() else -1 } // KT-46360
                        androidStringsHelper.getQuantityString(
                            R.plurals.unread_desc,
                            combinedSum,
                            combinedSum
                        )
                    }.debounce(200L).flowOn(defaultDispatcher).collect { text ->
                        _feedsUiState.update { it.copy(importantSum = text) }
                    }
            }

            launch {
                combine(
                    feedsFlow,
                    unreadCountMapFlow,
                    diffMapHolder.diffMapSnapshotFlow
                ) { groupWithFeedsList, unreadCountMap, diffMap ->
                    val result = mutableListOf<GroupWithFeed>()
                    val unreadDiffs = diffMap.values.filter { it.isUnread }
                    val readDiffs = diffMap.values.filterNot { it.isUnread }

                    for (groupItem in groupWithFeedsList) {
                        val groupItem = groupItem.copy()

                        val groupId = groupItem.group.id

                        val groupCount = unreadCountMap[groupId] ?: 0
                        val combinedGroupCount =
                            groupCount + unreadDiffs.count { it.groupId == groupId } - readDiffs.count { it.groupId == groupId }

                        if (!hideEmptyGroups) {
                            for (feed in groupItem.feeds) {
                                val feedId = feed.id
                                val feedCount = unreadCountMap[feedId] ?: 0
                                val combinedFeedCount =
                                    feedCount + unreadDiffs.count { it.feedId == feedId } - readDiffs.count { it.feedId == feedId }
                                check(combinedFeedCount >= 0)
                                feed.important = combinedFeedCount
                            }

                            groupItem.group.feeds = groupItem.feeds.size
                            groupItem.group.important = combinedGroupCount
                        } else if (combinedGroupCount > 0) {
                            groupItem.feeds.removeAll { feed ->
                                val feedId = feed.id
                                val feedCount = unreadCountMap[feedId] ?: 0
                                val combinedFeedCount =
                                    feedCount + unreadDiffs.count { it.feedId == feedId } - readDiffs.count { it.feedId == feedId }
                                feed.important = combinedFeedCount
                                check(combinedFeedCount >= 0)
                                if (combinedFeedCount > 0) {
                                    feed.important = combinedFeedCount
                                }
                                combinedFeedCount == 0
                            }

                            groupItem.group.important = combinedGroupCount
                            groupItem.group.feeds = groupItem.feeds.size
                        } else {
                            continue
                        }
                        if (groupItem.group.id != defaultGroupId || groupItem.feeds.isNotEmpty()) {
                            result.add(groupItem)
                        }
                    }
                    result
                }.debounce(200L).flowOn(defaultDispatcher)
                    .collect { _groupWithFeedsListFlow.value = it }
            }
        }
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun pullFeeds(filterState: FilterState, hideEmptyGroups: Boolean) {
//        val isStarred = filterState.filter.isStarred()
//        val isUnread = filterState.filter.isUnread()
//        _feedsUiState.update {
//            val important = rssService.get().pullImportant(isStarred, isUnread)
//            it.copy(
////                importantSum = important
////                    .mapLatest {
////                        (it["sum"] ?: 0).run {
////                            androidStringsHelper.getQuantityString(
////                                when {
////                                    isStarred -> R.plurals.starred_desc
////                                    isUnread -> R.plurals.unread_desc
////                                    else -> R.plurals.all_desc
////                                },
////                                this,
////                                this
////                            )
////                        }
////                    }.flowOn(defaultDispatcher),
//                groupWithFeedList = combine(
//                    important,
//                    rssService.get().pullFeeds()
//                ) { importantMap, groupWithFeedList ->
//                    val groupIterator = groupWithFeedList.iterator()
//                    while (groupIterator.hasNext()) {
//                        val groupWithFeed = groupIterator.next()
//                        val groupImportant = importantMap[groupWithFeed.group.id] ?: 0
//                        if (hideEmptyGroups && (isStarred || isUnread) && groupImportant == 0) {
//                            groupIterator.remove()
//                            continue
//                        }
//                        groupWithFeed.group.important = groupImportant
//                        val feedIterator = groupWithFeed.feeds.iterator()
//                        while (feedIterator.hasNext()) {
//                            val feed = feedIterator.next()
//                            val feedImportant = importantMap[feed.id] ?: 0
//                            groupWithFeed.group.feeds++
//                            if (hideEmptyGroups && (isStarred || isUnread) && feedImportant == 0) {
//                                feedIterator.remove()
//                                continue
//                            }
//                            feed.important = feedImportant
//                        }
//                    }
//                    groupWithFeedList
//                }.mapLatest { list ->
//                    list.filter { (group, feeds) ->
//                        group.id != feedsUiState.value.account?.id?.getDefaultGroupId() || feeds.isNotEmpty()
//                    }
//                }.flowOn(defaultDispatcher),
//            )
//        }
//    }
}

data class FeedsUiState(
    val filter: Filter = Filter.All,
    val account: Account? = null,
    val importantSum: String = "",
    val listState: LazyListState = LazyListState(),
    val groupsVisible: SnapshotStateMap<String, Boolean> = mutableStateMapOf(),
)
