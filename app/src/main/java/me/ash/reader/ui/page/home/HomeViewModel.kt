package me.ash.reader.ui.page.home

import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Filter
import me.ash.reader.data.entity.Group
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.SyncWorker
import me.ash.reader.ui.ext.animateScrollToPage
import javax.inject.Inject

@OptIn(ExperimentalPagerApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _viewState = MutableStateFlow(HomeViewState())
    val viewState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    val syncWorkLiveData = workManager.getWorkInfoByIdLiveData(SyncWorker.UUID)

    fun dispatch(action: HomeViewAction) {
        when (action) {
            is HomeViewAction.Sync -> sync()
            is HomeViewAction.ChangeFilter -> changeFilter(action.filterState)
            is HomeViewAction.ScrollToPage -> scrollToPage(
                action.scope,
                action.targetPage,
                action.callback
            )
        }
    }

    private fun sync() {
        rssRepository.get().doSync()
    }

    private fun changeFilter(filterState: FilterState) {
        _filterState.update {
            it.copy(
                group = filterState.group,
                feed = filterState.feed,
                filter = filterState.filter,
            )
        }
    }

    private fun scrollToPage(scope: CoroutineScope, targetPage: Int, callback: () -> Unit = {}) {
        _viewState.value.pagerState.animateScrollToPage(scope, targetPage, callback)
    }
}

data class FilterState(
    val group: Group? = null,
    val feed: Feed? = null,
    val filter: Filter = Filter.All,
)

@OptIn(ExperimentalPagerApi::class)
data class HomeViewState(
    val pagerState: PagerState = PagerState(0),
)

sealed class HomeViewAction {
    object Sync : HomeViewAction()

    data class ChangeFilter(
        val filterState: FilterState
    ) : HomeViewAction()

    data class ScrollToPage(
        val scope: CoroutineScope,
        val targetPage: Int,
        val callback: () -> Unit = {},
    ) : HomeViewAction()
}