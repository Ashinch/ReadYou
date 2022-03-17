package me.ash.reader.ui.page.home.feed.subscribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.data.article.Article
import me.ash.reader.data.constant.Symbol
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.repository.RssHelper
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.formatUrl
import me.ash.reader.ui.extension.animateScrollToPage
import javax.inject.Inject

@OptIn(ExperimentalPagerApi::class)
@HiltViewModel
class SubscribeViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val rssHelper: RssHelper,
) : ViewModel() {
    private val _viewState = MutableStateFlow(SubScribeViewState())
    val viewState: StateFlow<SubScribeViewState> = _viewState.asStateFlow()

    fun dispatch(action: SubscribeViewAction) {
        when (action) {
            is SubscribeViewAction.Init -> init()
            is SubscribeViewAction.Reset -> reset()
            is SubscribeViewAction.Show -> changeVisible(true)
            is SubscribeViewAction.Hide -> changeVisible(false)
            is SubscribeViewAction.Input -> inputLink(action.content)
            is SubscribeViewAction.Search -> search(action.scope)
            is SubscribeViewAction.ChangeNotificationPreset ->
                changeNotificationPreset()
            is SubscribeViewAction.ChangeFullContentParsePreset ->
                changeFullContentParsePreset()
            is SubscribeViewAction.SelectedGroup -> selectedGroup(action.groupId)
            is SubscribeViewAction.Subscribe -> subscribe()
        }
    }

    private fun init() {
        _viewState.update {
            it.copy(
                groups = rssRepository.get().pullGroups()
            )
        }
    }

    private fun reset() {
        _viewState.update {
            it.copy(
                visible = false,
                title = "订阅",
                errorMessage = "",
                inputContent = "",
                feed = null,
                articles = emptyList(),
                notificationPreset = false,
                fullContentParsePreset = false,
                selectedGroupId = "",
                groups = emptyFlow(),
            )
        }
    }

    private fun subscribe() {
        val feed = _viewState.value.feed ?: return
        val articles = _viewState.value.articles
        val groupId = _viewState.value.selectedGroupId
        viewModelScope.launch(Dispatchers.IO) {
            rssRepository.get().subscribe(
                feed.copy(
                    groupId = groupId,
                    isNotification = _viewState.value.notificationPreset,
                    isFullContent = _viewState.value.fullContentParsePreset,
                ), articles
            )
            changeVisible(false)
        }
    }

    private fun selectedGroup(groupId: String) {
        _viewState.update {
            it.copy(
                selectedGroupId = groupId,
            )
        }
    }

    private fun changeFullContentParsePreset() {
        _viewState.update {
            it.copy(
                fullContentParsePreset = !_viewState.value.fullContentParsePreset
            )
        }
    }

    private fun changeNotificationPreset() {
        _viewState.update {
            it.copy(
                notificationPreset = !_viewState.value.notificationPreset
            )
        }
    }

    private fun search(scope: CoroutineScope) {
        _viewState.value.inputContent.formatUrl().let { str ->
            if (str != _viewState.value.inputContent) {
                _viewState.update {
                    it.copy(
                        inputContent = str
                    )
                }
            }
        }
        _viewState.update {
            it.copy(
                title = "搜索中",
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val feedWithArticle = rssHelper.searchFeed(_viewState.value.inputContent)
                _viewState.update {
                    it.copy(
                        feed = feedWithArticle.feed,
                        articles = feedWithArticle.articles,
                    )
                }
                _viewState.value.pagerState.animateScrollToPage(scope, 1)
            } catch (e: Exception) {
                e.printStackTrace()
                _viewState.update {
                    it.copy(
                        title = "订阅",
                        errorMessage = e.message ?: Symbol.Unknown,
                    )
                }
            }
        }
    }

    private fun inputLink(content: String) {
        _viewState.update {
            it.copy(
                inputContent = content
            )
        }
    }

    private fun changeVisible(visible: Boolean) {
        _viewState.update {
            it.copy(
                visible = visible
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
data class SubScribeViewState(
    val visible: Boolean = false,
    val title: String = "订阅",
    val errorMessage: String = "",
    val inputContent: String = "",
    val feed: Feed? = null,
    val articles: List<Article> = emptyList(),
    val notificationPreset: Boolean = false,
    val fullContentParsePreset: Boolean = false,
    val selectedGroupId: String = "",
    val groups: Flow<List<Group>> = emptyFlow(),
    val pagerState: PagerState = PagerState(),
)

sealed class SubscribeViewAction {
    object Init : SubscribeViewAction()
    object Reset : SubscribeViewAction()

    object Show : SubscribeViewAction()
    object Hide : SubscribeViewAction()

    data class Input(
        val content: String
    ) : SubscribeViewAction()

    data class Search(
        val scope: CoroutineScope,
    ) : SubscribeViewAction()

    object ChangeNotificationPreset : SubscribeViewAction()
    object ChangeFullContentParsePreset : SubscribeViewAction()

    data class SelectedGroup(
        val groupId: String
    ) : SubscribeViewAction()

    object Subscribe : SubscribeViewAction()
}
