package me.ash.reader.ui.page.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.data.repository.AppRepository
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val appRepository: AppRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(UpdateViewState())
    val viewState: StateFlow<UpdateViewState> = _viewState.asStateFlow()

    fun dispatch(action: UpdateViewAction) {
        when (action) {
            is UpdateViewAction.Show -> changeUpdateDialogVisible(true)
            is UpdateViewAction.Hide -> changeUpdateDialogVisible(false)
            is UpdateViewAction.CheckUpdate -> checkUpdate(
                action.preProcessor,
                action.postProcessor
            )
        }
    }

    private fun checkUpdate(
        preProcessor: suspend () -> Unit = {},
        postProcessor: suspend (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            preProcessor()
            appRepository.checkUpdate().let {
                if (it) changeUpdateDialogVisible(true)
                postProcessor(it)
            }
        }
    }

    private fun changeUpdateDialogVisible(visible: Boolean) {
        _viewState.update {
            it.copy(
                updateDialogVisible = visible
            )
        }
    }
}

data class UpdateViewState(
    val updateDialogVisible: Boolean = false,
)

sealed class UpdateViewAction {
    object Show : UpdateViewAction()
    object Hide : UpdateViewAction()

    data class CheckUpdate(
        val preProcessor: suspend () -> Unit = {},
        val postProcessor: suspend (Boolean) -> Unit = {}
    ) : UpdateViewAction()
}