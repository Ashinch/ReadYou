package me.ash.reader.ui.page.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.data.repository.AppRepository
import me.ash.reader.data.source.Download
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
            is UpdateViewAction.DownloadUpdate -> downloadUpdate(action.url)
        }
    }

    private fun checkUpdate(
        preProcessor: suspend () -> Unit = {},
        postProcessor: suspend (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            preProcessor()
            appRepository.checkUpdate().let {
                it?.let {
                    changeUpdateDialogVisible(it)
                    postProcessor(it)
                }
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

    private fun downloadUpdate(url: String) {
        viewModelScope.launch {
            _viewState.update {
                it.copy(
                    downloadFlow = flow { emit(Download.Progress(0)) }
                )
            }
            _viewState.update {
                it.copy(
                    downloadFlow = appRepository.downloadFile(url)
                )
            }
        }
    }
}

data class UpdateViewState(
    val updateDialogVisible: Boolean = false,
    val downloadFlow: Flow<Download> = emptyFlow(),
)

sealed class UpdateViewAction {
    object Show : UpdateViewAction()
    object Hide : UpdateViewAction()

    data class CheckUpdate(
        val preProcessor: suspend () -> Unit = {},
        val postProcessor: suspend (Boolean) -> Unit = {}
    ) : UpdateViewAction()

    data class DownloadUpdate(
        val url: String,
    ) : UpdateViewAction()
}