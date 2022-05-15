package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.data.entity.ArticleWithFeed
import me.ash.reader.data.repository.RssRepository
import java.util.*
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class FlowViewModel @Inject constructor(
    private val rssRepository: RssRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ArticleViewState())
    val viewState: StateFlow<ArticleViewState> = _viewState.asStateFlow()

    fun dispatch(action: FlowViewAction) {
        when (action) {
            is FlowViewAction.Sync -> sync()
            is FlowViewAction.ChangeIsBack -> changeIsBack(action.isBack)
            is FlowViewAction.ScrollToItem -> scrollToItem(action.index)
            is FlowViewAction.MarkAsRead -> markAsRead(
                action.groupId,
                action.feedId,
                action.articleId,
                action.markAsReadBefore,
            )
        }
    }

    private fun sync() {
        rssRepository.get().doSync()
    }

    private fun scrollToItem(index: Int) {
        viewModelScope.launch {
            _viewState.value.listState.scrollToItem(index)
        }
    }

    private fun changeIsBack(isBack: Boolean) {
        _viewState.update {
            it.copy(isBack = isBack)
        }
    }

    private fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        markAsReadBefore: MarkAsReadBefore
    ) {
        viewModelScope.launch {
            rssRepository.get().markAsRead(
                groupId = groupId,
                feedId = feedId,
                articleId = articleId,
                before = when (markAsReadBefore) {
                    MarkAsReadBefore.All -> null
                    MarkAsReadBefore.OneDay -> Calendar.getInstance().apply {
                        time = Date()
                        add(Calendar.DAY_OF_MONTH, -1)
                    }.time
                    MarkAsReadBefore.ThreeDays -> Calendar.getInstance().apply {
                        time = Date()
                        add(Calendar.DAY_OF_MONTH, -3)
                    }.time
                    MarkAsReadBefore.SevenDays -> Calendar.getInstance().apply {
                        time = Date()
                        add(Calendar.DAY_OF_MONTH, -7)
                    }.time
                },
                isUnread = false,
            )
        }
    }
}

data class ArticleViewState(
    val filterImportant: Int = 0,
    val listState: LazyListState = LazyListState(),
    val isBack: Boolean = false,
    val syncWorkInfo: String = "",
)

sealed class FlowViewAction {
    object Sync : FlowViewAction()

    data class ChangeIsBack(
        val isBack: Boolean
    ) : FlowViewAction()

    data class ScrollToItem(
        val index: Int
    ) : FlowViewAction()

    data class MarkAsRead(
        val groupId: String?,
        val feedId: String?,
        val articleId: String?,
        val markAsReadBefore: MarkAsReadBefore
    ) : FlowViewAction()
}

enum class MarkAsReadBefore {
    SevenDays,
    ThreeDays,
    OneDay,
    All,
}

@Immutable
sealed class FlowItemView {
    @Immutable
    class Article(val articleWithFeed: ArticleWithFeed) : FlowItemView()
    @Immutable
    class Date(val date: String, val showSpacer: Boolean) : FlowItemView()
}