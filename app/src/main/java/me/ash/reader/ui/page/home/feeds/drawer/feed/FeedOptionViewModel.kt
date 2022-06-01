package me.ash.reader.ui.page.home.feeds.drawer.feed

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ash.reader.data.entity.Feed
import me.ash.reader.data.entity.Group
import me.ash.reader.data.module.DispatcherIO
import me.ash.reader.data.module.DispatcherMain
import me.ash.reader.data.repository.RssRepository
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class FeedOptionViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    @DispatcherMain
    private val dispatcherMain: CoroutineDispatcher,
    @DispatcherIO
    private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    private val _feedOptionUiState = MutableStateFlow(FeedOptionUiState())
    val feedOptionUiState: StateFlow<FeedOptionUiState> = _feedOptionUiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcherIO) {
            rssRepository.get().pullGroups().collect { groups ->
                _feedOptionUiState.update {
                    it.copy(
                        groups = groups
                    )
                }
            }
        }
    }

    private suspend fun fetchFeed(feedId: String) {
        val feed = rssRepository.get().findFeedById(feedId)
        _feedOptionUiState.update {
            it.copy(
                feed = feed,
                selectedGroupId = feed?.groupId ?: "",
            )
        }
    }

    fun showDrawer(scope: CoroutineScope, feedId: String) {
        scope.launch {
            fetchFeed(feedId)
            _feedOptionUiState.value.drawerState.show()
        }
    }

    fun hideDrawer(scope: CoroutineScope) {
        scope.launch {
            _feedOptionUiState.value.drawerState.hide()
        }
    }

    fun showNewGroupDialog() {
        _feedOptionUiState.update {
            it.copy(
                newGroupDialogVisible = true,
                newGroupContent = "",
            )
        }
    }

    fun hideNewGroupDialog() {
        _feedOptionUiState.update {
            it.copy(
                newGroupDialogVisible = false,
                newGroupContent = "",
            )
        }
    }

    fun inputNewGroup(content: String) {
        _feedOptionUiState.update {
            it.copy(
                newGroupContent = content
            )
        }
    }

    fun addNewGroup() {
        if (_feedOptionUiState.value.newGroupContent.isNotBlank()) {
            viewModelScope.launch {
                selectedGroup(rssRepository.get().addGroup(_feedOptionUiState.value.newGroupContent))
                hideNewGroupDialog()
            }
        }
    }

    fun selectedGroup(groupId: String) {
        viewModelScope.launch(dispatcherIO) {
            _feedOptionUiState.value.feed?.let {
                rssRepository.get().updateFeed(
                    it.copy(
                        groupId = groupId
                    )
                )
                fetchFeed(it.id)
            }
        }
    }

    fun changeParseFullContentPreset() {
        viewModelScope.launch(dispatcherIO) {
            _feedOptionUiState.value.feed?.let {
                rssRepository.get().updateFeed(
                    it.copy(
                        isFullContent = !it.isFullContent
                    )
                )
                fetchFeed(it.id)
            }
        }
    }

    fun changeAllowNotificationPreset() {
        viewModelScope.launch(dispatcherIO) {
            _feedOptionUiState.value.feed?.let {
                rssRepository.get().updateFeed(
                    it.copy(
                        isNotification = !it.isNotification
                    )
                )
                fetchFeed(it.id)
            }
        }
    }

    fun delete(callback: () -> Unit = {}) {
        _feedOptionUiState.value.feed?.let {
            viewModelScope.launch(dispatcherIO) {
                rssRepository.get().deleteFeed(it)
                withContext(dispatcherMain) {
                    callback()
                }
            }
        }
    }

    fun hideDeleteDialog() {
        _feedOptionUiState.update {
            it.copy(
                deleteDialogVisible = false,
            )
        }
    }

    fun showDeleteDialog() {
        _feedOptionUiState.update {
            it.copy(
                deleteDialogVisible = true,
            )
        }
    }

    fun showClearDialog() {
        _feedOptionUiState.update {
            it.copy(
                clearDialogVisible = true,
            )
        }
    }

    fun hideClearDialog() {
        _feedOptionUiState.update {
            it.copy(
                clearDialogVisible = false,
            )
        }
    }

    fun clearFeed(callback: () -> Unit = {}) {
        _feedOptionUiState.value.feed?.let {
            viewModelScope.launch(dispatcherIO) {
                rssRepository.get().deleteArticles(feed = it)
                withContext(dispatcherMain) {
                    callback()
                }
            }
        }
    }

    fun renameFeed() {
        _feedOptionUiState.value.feed?.let {
            viewModelScope.launch {
                rssRepository.get().updateFeed(
                    it.copy(
                        name = _feedOptionUiState.value.newName
                    )
                )
                _feedOptionUiState.update {
                    it.copy(
                        renameDialogVisible = false,
                    )
                }
            }
        }
    }

    fun showRenameDialog() {
        _feedOptionUiState.update {
            it.copy(
                renameDialogVisible = true,
                newName = _feedOptionUiState.value.feed?.name ?: "",
            )
        }
    }

    fun hideRenameDialog() {
        _feedOptionUiState.update {
            it.copy(
                renameDialogVisible = false,
                newName = "",
            )
        }
    }

    fun inputNewName(content: String) {
        _feedOptionUiState.update {
            it.copy(
                newName = content
            )
        }
    }

    fun showFeedUrlDialog() {
        _feedOptionUiState.update {
            it.copy(
                changeUrlDialogVisible = true,
                newUrl = _feedOptionUiState.value.feed?.url ?: "",
            )
        }
    }

    fun hideFeedUrlDialog() {
        _feedOptionUiState.update {
            it.copy(
                changeUrlDialogVisible = false,
                newUrl = "",
            )
        }
    }

    fun inputNewUrl(content: String) {
        _feedOptionUiState.update {
            it.copy(
                newUrl = content
            )
        }
    }

    fun changeFeedUrl() {
        _feedOptionUiState.value.feed?.let {
            viewModelScope.launch {
                rssRepository.get().updateFeed(
                    it.copy(
                        url = _feedOptionUiState.value.newUrl
                    )
                )
                _feedOptionUiState.update {
                    it.copy(
                        changeUrlDialogVisible = false,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class FeedOptionUiState(
    var drawerState: ModalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden),
    val feed: Feed? = null,
    val selectedGroupId: String = "",
    val newGroupContent: String = "",
    val newGroupDialogVisible: Boolean = false,
    val groups: List<Group> = emptyList(),
    val deleteDialogVisible: Boolean = false,
    val clearDialogVisible: Boolean = false,
    val newName: String = "",
    val renameDialogVisible: Boolean = false,
    val newUrl: String = "",
    val changeUrlDialogVisible: Boolean = false,
)
