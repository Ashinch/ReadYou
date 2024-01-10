package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.ash.reader.data.model.general.MarkAsReadConditions
import me.ash.reader.data.module.IODispatcher
import me.ash.reader.data.repository.RssRepository
import javax.inject.Inject

@HiltViewModel
class FlowViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _flowUiState = MutableStateFlow(FlowUiState())
    val flowUiState: StateFlow<FlowUiState> = _flowUiState.asStateFlow()

    fun sync() {
        viewModelScope.launch(ioDispatcher) {
            rssRepository.get().doSync()
        }
    }

    fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        conditions: MarkAsReadConditions,
    ) {
        viewModelScope.launch {
            rssRepository.get().markAsRead(
                groupId = groupId,
                feedId = feedId,
                articleId = articleId,
                before = conditions.toDate(),
                isUnread = false,
            )
        }
    }
}

data class FlowUiState(
    val filterImportant: Int = 0,
    val listState: LazyListState = LazyListState(),
    val isBack: Boolean = false,
    val syncWorkInfo: String = "",
)
