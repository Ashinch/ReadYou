package me.ash.reader.ui.page.home.drawer.feed

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.repository.RssRepository
import javax.inject.Inject

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class
)
@HiltViewModel
class FeedOptionViewModel @Inject constructor(
    private val rssRepository: RssRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(FeedOptionViewState())
    val viewState: StateFlow<FeedOptionViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            rssRepository.get().pullGroups().collect { groups ->
                _viewState.update {
                    it.copy(
                        groups = groups
                    )
                }
            }
        }
    }

    fun dispatch(action: FeedOptionViewAction) {
        when (action) {
            is FeedOptionViewAction.Show -> show(action.scope, action.feedId)
            is FeedOptionViewAction.Hide -> hide(action.scope)
            is FeedOptionViewAction.SelectedGroup -> selectedGroup(action.groupId)
            is FeedOptionViewAction.InputNewGroup -> inputNewGroup(action.content)
            is FeedOptionViewAction.SelectedNewGroup -> selectedNewGroup(action.selected)
            is FeedOptionViewAction.ChangeAllowNotificationPreset -> changeAllowNotificationPreset()
            is FeedOptionViewAction.ChangeParseFullContentPreset -> changeParseFullContentPreset()
            is FeedOptionViewAction.ShowDeleteDialog -> showDeleteDialog()
            is FeedOptionViewAction.HideDeleteDialog -> hideDeleteDialog()
            is FeedOptionViewAction.Delete -> delete(action.callback)
        }
    }

    private suspend fun fetchFeed(feedId: String) {
        val feed = rssRepository.get().findFeedById(feedId)
        _viewState.update {
            it.copy(
                feed = feed,
                selectedGroupId = feed?.groupId ?: "",
            )
        }
    }

    private fun show(scope: CoroutineScope, feedId: String) {
        scope.launch {
            fetchFeed(feedId)
            _viewState.value.drawerState.show()
        }
    }

    private fun hide(scope: CoroutineScope) {
        scope.launch {
            _viewState.value.drawerState.hide()
        }
    }

    private fun inputNewGroup(content: String) {
        _viewState.update {
            it.copy(
                newGroupContent = content
            )
        }
    }

    private fun selectedGroup(groupId: String) {
        viewModelScope.launch {
            _viewState.value.feed?.let {
                rssRepository.get().updateFeed(
                    it.copy(
                        groupId = groupId
                    )
                )
                fetchFeed(it.id)
            }
        }
    }

    private fun selectedNewGroup(selected: Boolean) {
        _viewState.update {
            it.copy(
                newGroupSelected = selected,
            )
        }
    }

    private fun changeParseFullContentPreset() {
        viewModelScope.launch {
            _viewState.value.feed?.let {
                rssRepository.get().updateFeed(
                    it.copy(
                        isFullContent = !it.isFullContent
                    )
                )
                fetchFeed(it.id)
            }
        }
    }

    private fun changeAllowNotificationPreset() {
        viewModelScope.launch {
            _viewState.value.feed?.let {
                rssRepository.get().updateFeed(
                    it.copy(
                        isNotification = !it.isNotification
                    )
                )
                fetchFeed(it.id)
            }
        }
    }

    private fun delete(callback: () -> Unit = {}) {
        _viewState.value.feed?.let {
            viewModelScope.launch {
                rssRepository.get().deleteFeed(it)
            }.invokeOnCompletion {
                callback()
            }
        }
    }

    private fun hideDeleteDialog() {
        _viewState.update {
            it.copy(
                deleteDialogVisible = false,
            )
        }
    }

    private fun showDeleteDialog() {
        _viewState.update {
            it.copy(
                deleteDialogVisible = true,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class FeedOptionViewState(
    var drawerState: ModalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden),
    val feed: Feed? = null,
    val selectedGroupId: String = "",
    val newGroupContent: String = "",
    val newGroupSelected: Boolean = false,
    val groups: List<Group> = emptyList(),
    val deleteDialogVisible: Boolean = false,
)

sealed class FeedOptionViewAction {
    data class Show(
        val scope: CoroutineScope,
        val feedId: String
    ) : FeedOptionViewAction()

    data class Hide(
        val scope: CoroutineScope,
    ) : FeedOptionViewAction()

    object ChangeAllowNotificationPreset : FeedOptionViewAction()
    object ChangeParseFullContentPreset : FeedOptionViewAction()

    data class SelectedGroup(
        val groupId: String
    ) : FeedOptionViewAction()

    data class InputNewGroup(
        val content: String
    ) : FeedOptionViewAction()

    data class SelectedNewGroup(
        val selected: Boolean
    ) : FeedOptionViewAction()

    data class Delete(
        val callback: () -> Unit = {}
    ) : FeedOptionViewAction()

    object ShowDeleteDialog: FeedOptionViewAction()
    object HideDeleteDialog: FeedOptionViewAction()
}
