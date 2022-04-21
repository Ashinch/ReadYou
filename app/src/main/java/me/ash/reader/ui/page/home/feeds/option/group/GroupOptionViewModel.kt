package me.ash.reader.ui.page.home.feeds.option.group

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ash.reader.data.entity.Group
import me.ash.reader.data.repository.RssRepository
import javax.inject.Inject

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class
)
@HiltViewModel
class GroupOptionViewModel @Inject constructor(
    private val rssRepository: RssRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(GroupOptionViewState())
    val viewState: StateFlow<GroupOptionViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            rssRepository.get().pullGroups().collect { groups ->
                _viewState.update {
                    it.copy(
                        groups = groups
                    )
                }
            }
        }
    }

    fun dispatch(action: GroupOptionViewAction) {
        when (action) {
            is GroupOptionViewAction.Show -> show(action.scope, action.groupId)
            is GroupOptionViewAction.Hide -> hide(action.scope)
            is GroupOptionViewAction.ShowDeleteDialog -> changeDeleteDialogVisible(true)
            is GroupOptionViewAction.HideDeleteDialog -> changeDeleteDialogVisible(false)
            is GroupOptionViewAction.Delete -> delete(action.callback)

            is GroupOptionViewAction.ShowAllAllowNotificationDialog ->
                changeAllAllowNotificationDialogVisible(true)
            is GroupOptionViewAction.HideAllAllowNotificationDialog ->
                changeAllAllowNotificationDialogVisible(false)
            is GroupOptionViewAction.AllAllowNotification ->
                allAllowNotification(action.isNotification, action.callback)

            is GroupOptionViewAction.ShowAllParseFullContentDialog ->
                changeAllParseFullContentDialogVisible(true)
            is GroupOptionViewAction.HideAllParseFullContentDialog ->
                changeAllParseFullContentDialogVisible(false)
            is GroupOptionViewAction.AllParseFullContent ->
                allParseFullContent(action.isFullContent, action.callback)

            is GroupOptionViewAction.ShowAllMoveToGroupDialog ->
                changeAllMoveToGroupDialogVisible(action.targetGroup, true)
            is GroupOptionViewAction.HideAllMoveToGroupDialog ->
                changeAllMoveToGroupDialogVisible(visible = false)
            is GroupOptionViewAction.AllMoveToGroup ->
                allMoveToGroup(action.callback)

            is GroupOptionViewAction.InputNewName -> inputNewName(action.content)
            is GroupOptionViewAction.Rename -> rename()
            is GroupOptionViewAction.ShowRenameDialog -> changeRenameDialogVisible(true)
            is GroupOptionViewAction.HideRenameDialog -> changeRenameDialogVisible(false)
        }
    }

    private suspend fun fetchGroup(groupId: String) {
        val group = rssRepository.get().findGroupById(groupId)
        _viewState.update {
            it.copy(
                group = group,
            )
        }
    }

    private fun show(scope: CoroutineScope, groupId: String) {
        scope.launch {
            fetchGroup(groupId)
            _viewState.value.drawerState.show()
        }
    }

    private fun hide(scope: CoroutineScope) {
        scope.launch {
            _viewState.value.drawerState.hide()
        }
    }

    private fun allAllowNotification(isNotification: Boolean, callback: () -> Unit = {}) {
        _viewState.value.group?.let {
            viewModelScope.launch(Dispatchers.IO) {
                rssRepository.get().groupAllowNotification(it, isNotification)
                withContext(Dispatchers.Main) {
                    callback()
                }
            }
        }
    }

    private fun changeAllAllowNotificationDialogVisible(visible: Boolean) {
        _viewState.update {
            it.copy(
                allAllowNotificationDialogVisible = visible,
            )
        }
    }

    private fun allParseFullContent(isFullContent: Boolean, callback: () -> Unit = {}) {
        _viewState.value.group?.let {
            viewModelScope.launch(Dispatchers.IO) {
                rssRepository.get().groupParseFullContent(it, isFullContent)
                withContext(Dispatchers.Main) {
                    callback()
                }
            }
        }
    }

    private fun changeAllParseFullContentDialogVisible(visible: Boolean) {
        _viewState.update {
            it.copy(
                allParseFullContentDialogVisible = visible,
            )
        }
    }

    private fun delete(callback: () -> Unit = {}) {
        _viewState.value.group?.let {
            viewModelScope.launch(Dispatchers.IO) {
                rssRepository.get().deleteGroup(it)
                withContext(Dispatchers.Main) {
                    callback()
                }
            }
        }
    }

    private fun changeDeleteDialogVisible(visible: Boolean) {
        _viewState.update {
            it.copy(
                deleteDialogVisible = visible,
            )
        }
    }

    private fun allMoveToGroup(callback: () -> Unit) {
        _viewState.value.group?.let { group ->
            _viewState.value.targetGroup?.let { targetGroup ->
                viewModelScope.launch(Dispatchers.IO) {
                    rssRepository.get().groupMoveToTargetGroup(group, targetGroup)
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }
    }

    private fun changeAllMoveToGroupDialogVisible(targetGroup: Group? = null, visible: Boolean) {
        _viewState.update {
            it.copy(
                targetGroup = if (visible) targetGroup else null,
                allMoveToGroupDialogVisible = visible,
            )
        }
    }

    private fun rename() {
        _viewState.value.group?.let {
            viewModelScope.launch {
                rssRepository.get().updateGroup(
                    it.copy(
                        name = _viewState.value.newName
                    )
                )
                _viewState.update {
                    it.copy(
                        renameDialogVisible = false,
                    )
                }
            }
        }
    }

    private fun changeRenameDialogVisible(visible: Boolean) {
        _viewState.update {
            it.copy(
                renameDialogVisible = visible,
                newName = if (visible) _viewState.value.group?.name ?: "" else "",
            )
        }
    }

    private fun inputNewName(content: String) {
        _viewState.update {
            it.copy(
                newName = content
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
data class GroupOptionViewState(
    var drawerState: ModalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden),
    val group: Group? = null,
    val targetGroup: Group? = null,
    val groups: List<Group> = emptyList(),
    val allAllowNotificationDialogVisible: Boolean = false,
    val allParseFullContentDialogVisible: Boolean = false,
    val allMoveToGroupDialogVisible: Boolean = false,
    val deleteDialogVisible: Boolean = false,
    val newName: String = "",
    val renameDialogVisible: Boolean = false,
)

sealed class GroupOptionViewAction {
    data class Show(
        val scope: CoroutineScope,
        val groupId: String
    ) : GroupOptionViewAction()

    data class Hide(
        val scope: CoroutineScope,
    ) : GroupOptionViewAction()

    data class Delete(
        val callback: () -> Unit = {}
    ) : GroupOptionViewAction()

    object ShowDeleteDialog : GroupOptionViewAction()
    object HideDeleteDialog : GroupOptionViewAction()

    data class AllParseFullContent(
        val isFullContent: Boolean,
        val callback: () -> Unit = {}
    ) : GroupOptionViewAction()

    object ShowAllParseFullContentDialog : GroupOptionViewAction()
    object HideAllParseFullContentDialog : GroupOptionViewAction()

    data class AllAllowNotification(
        val isNotification: Boolean,
        val callback: () -> Unit = {}
    ) : GroupOptionViewAction()

    object ShowAllAllowNotificationDialog : GroupOptionViewAction()
    object HideAllAllowNotificationDialog : GroupOptionViewAction()

    data class AllMoveToGroup(
        val callback: () -> Unit = {}
    ) : GroupOptionViewAction()

    data class ShowAllMoveToGroupDialog(
        val targetGroup: Group
    ) : GroupOptionViewAction()

    object HideAllMoveToGroupDialog : GroupOptionViewAction()

    object ShowRenameDialog : GroupOptionViewAction()
    object HideRenameDialog : GroupOptionViewAction()
    object Rename : GroupOptionViewAction()
    data class InputNewName(
        val content: String
    ) : GroupOptionViewAction()
}
