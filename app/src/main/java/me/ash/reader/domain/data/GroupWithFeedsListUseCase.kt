package me.ash.reader.domain.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.DataStoreKey.Companion.currentAccountId
import me.ash.reader.ui.ext.getDefaultGroupId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GroupWithFeedsListUseCase @Inject constructor(
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider,
    private val rssService: RssService,
    private val filterStateUseCase: FilterStateUseCase,
    private val diffMapHolder: DiffMapHolder,
) {

    private var currentJob: Job? = null

    init {
        applicationScope.launch {
            filterStateUseCase.filterStateFlow.mapLatest { it.filter }.distinctUntilChanged()
                .collect {
                    currentJob?.cancel()
                    currentJob = when (it) {
                        Filter.Unread -> pullUnreadFeeds()
                        Filter.Starred -> pullStarredFeeds()
                        else -> pullAllFeeds()
                    }
                }
        }
    }

    private val _groupWithFeedsListFlow: MutableStateFlow<List<GroupWithFeed>> =
        MutableStateFlow<List<GroupWithFeed>>(emptyList())
    val groupWithFeedListFlow: StateFlow<List<GroupWithFeed>> = _groupWithFeedsListFlow

    private val feedsFlow = rssService.get().pullFeeds()

    private val defaultGroupId =
        settingsProvider.getOrDefault<Int>(currentAccountId, 1).getDefaultGroupId()

    private val hideEmptyGroups get() = settingsProvider.settings.hideEmptyGroups.value

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pullAllFeeds(): Job {
        val articleCountMapFlow =
            rssService.get().pullImportant(isStarred = false, isUnread = false)

        return applicationScope.launch {
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
            }.flowOn(ioDispatcher).collect { _groupWithFeedsListFlow.value = it }

        }
    }

    private fun pullStarredFeeds(): Job {
        val starredCountMap = rssService.get().pullImportant(isStarred = true, isUnread = false)

        return applicationScope.launch {

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
            }.flowOn(ioDispatcher).collect {
                _groupWithFeedsListFlow.value = it
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun pullUnreadFeeds(): Job {
        val unreadCountMapFlow = rssService.get().pullImportant(isStarred = false, isUnread = true)
        return applicationScope.launch {
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
            }.debounce(200L).flowOn(ioDispatcher)
                .collect { _groupWithFeedsListFlow.value = it }
        }
    }

}