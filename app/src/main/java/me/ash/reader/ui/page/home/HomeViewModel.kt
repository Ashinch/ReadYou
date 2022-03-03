package me.ash.reader.ui.page.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.constant.Filter
import javax.inject.Inject

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalFoundationApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rssRepository: RssRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(HomeViewState())
    val viewState: StateFlow<HomeViewState> = _viewState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    fun dispatch(action: HomeViewAction) {
        when (action) {
            is HomeViewAction.Sync -> sync(action.callback)
            is HomeViewAction.ChangeFilter -> changeFilter(action.filterState)
            is HomeViewAction.ScrollToPage -> scrollToPage(
                action.scope,
                action.targetPage,
                action.callback
            )
        }
    }

    private fun sync(callback: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            rssRepository.sync()
            callback()
        }
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
        scope.launch {
            _viewState.value.pagerState.animateScrollToPage(targetPage)
            callback()
        }
    }
}

data class FilterState(
    val group: Group? = null,
    val feed: Feed? = null,
    val filter: Filter = Filter.All,
)

@ExperimentalPagerApi
data class HomeViewState(
    val pagerState: PagerState = PagerState(1),
)

sealed class HomeViewAction {
    data class Sync(
        val callback: () -> Unit = {},
    ) : HomeViewAction()

    data class ChangeFilter(
        val filterState: FilterState
    ) : HomeViewAction()

    data class ScrollToPage(
        val scope: CoroutineScope,
        val targetPage: Int,
        val callback: () -> Unit = {},
    ) : HomeViewAction()
}