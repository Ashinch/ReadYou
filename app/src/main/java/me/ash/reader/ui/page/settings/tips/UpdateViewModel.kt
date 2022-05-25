package me.ash.reader.ui.page.settings.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.data.repository.AppRepository
import me.ash.reader.data.source.Download
import me.ash.reader.ui.ext.notFdroid
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val appRepository: AppRepository,
) : ViewModel() {
    private val _updateUiState = MutableStateFlow(UpdateUiState())
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    fun checkUpdate(
        preProcessor: suspend () -> Unit = {},
        postProcessor: suspend (Boolean) -> Unit = {}
    ) {
        if (notFdroid) {
            viewModelScope.launch {
                preProcessor()
                appRepository.checkUpdate().let {
                    it?.let {
                        if (it) {
                            showDialog()
                        } else {
                            hideDialog()
                        }
                        postProcessor(it)
                    }
                }
            }
        }
    }

    fun showDialog() {
        _updateUiState.update {
            it.copy(
                updateDialogVisible = true
            )
        }
    }

    fun hideDialog() {
        _updateUiState.update {
            it.copy(
                updateDialogVisible = false
            )
        }
    }

    fun downloadUpdate(url: String) {
        viewModelScope.launch {
            _updateUiState.update {
                it.copy(
                    downloadFlow = flow { emit(Download.Progress(0)) }
                )
            }
            _updateUiState.update {
                it.copy(
                    downloadFlow = appRepository.downloadFile(url)
                )
            }
        }
    }
}

data class UpdateUiState(
    val updateDialogVisible: Boolean = false,
    val downloadFlow: Flow<Download> = emptyFlow(),
)
