package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.article.Article
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.repository.RssHelper
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.StringsRepository
import me.ash.reader.formatUrl
import me.ash.reader.ui.extension.animateScrollToPage
import javax.inject.Inject

@OptIn(ExperimentalPagerApi::class)
@HiltViewModel
class SubscribeViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val rssHelper: RssHelper,
    private val stringsRepository: StringsRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(SubscribeViewState())
    val viewState: StateFlow<SubscribeViewState> = _viewState.asStateFlow()
    private var searchJob: Job? = null

    fun dispatch(action: SubscribeViewAction) {
        when (action) {
            is SubscribeViewAction.Init -> init()
            is SubscribeViewAction.Reset -> reset()
            is SubscribeViewAction.Show -> changeVisible(true)
            is SubscribeViewAction.Hide -> changeVisible(false)
            is SubscribeViewAction.InputLink -> inputLink(action.content)
            is SubscribeViewAction.Search -> search(action.scope)
            is SubscribeViewAction.ChangeAllowNotificationPreset ->
                changeAllowNotificationPreset()
            is SubscribeViewAction.ChangeParseFullContentPreset ->
                changeParseFullContentPreset()
            is SubscribeViewAction.SelectedGroup -> selectedGroup(action.groupId)
            is SubscribeViewAction.InputNewGroup -> inputNewGroup(action.content)
            is SubscribeViewAction.SelectedNewGroup -> selectedNewGroup(action.selected)
            is SubscribeViewAction.Subscribe -> subscribe()
        }
    }

    private fun init() {
        _viewState.update {
            it.copy(
                title = stringsRepository.getString(R.string.subscribe),
                groups = rssRepository.get().pullGroups(),
            )
        }
    }

    private fun reset() {
        searchJob?.cancel()
        searchJob = null
        _viewState.update {
            SubscribeViewState().copy(
                title = stringsRepository.getString(R.string.subscribe),
            )
        }
    }

    private fun subscribe() {
        val feed = _viewState.value.feed ?: return
        val articles = _viewState.value.articles
        viewModelScope.launch {
            val groupId = async {
                if (
                    _viewState.value.newGroupSelected &&
                    _viewState.value.newGroupContent.isNotBlank()
                ) {
                    rssRepository.get().addGroup(_viewState.value.newGroupContent)
                } else {
                    _viewState.value.selectedGroupId
                }
            }
            rssRepository.get().subscribe(
                feed.copy(
                    groupId = groupId.await(),
                    isNotification = _viewState.value.allowNotificationPreset,
                    isFullContent = _viewState.value.parseFullContentPreset,
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

    private fun selectedNewGroup(selected: Boolean) {
        _viewState.update {
            it.copy(
                newGroupSelected = selected,
            )
        }
    }

    private fun changeParseFullContentPreset() {
        _viewState.update {
            it.copy(
                parseFullContentPreset = !_viewState.value.parseFullContentPreset
            )
        }
    }

    private fun changeAllowNotificationPreset() {
        _viewState.update {
            it.copy(
                allowNotificationPreset = !_viewState.value.allowNotificationPreset
            )
        }
    }

    private fun search(scope: CoroutineScope) {
        searchJob?.cancel()
        viewModelScope.launch {
            try {
                _viewState.update {
                    it.copy(
                        errorMessage = "",
                    )
                }
                _viewState.value.linkContent.formatUrl().let { str ->
                    if (str != _viewState.value.linkContent) {
                        _viewState.update {
                            it.copy(
                                linkContent = str
                            )
                        }
                    }
                }
                _viewState.update {
                    it.copy(
                        title = stringsRepository.getString(R.string.searching),
                        lockLinkInput = true,
                    )
                }
                if (rssRepository.get().isExist(_viewState.value.linkContent)) {
                    _viewState.update {
                        it.copy(
                            title = stringsRepository.getString(R.string.subscribe),
                            errorMessage = stringsRepository.getString(R.string.already_subscribed),
                            lockLinkInput = false,
                        )
                    }
                    return@launch
                }
                val feedWithArticle = rssHelper.searchFeed(_viewState.value.linkContent)
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
                        title = stringsRepository.getString(R.string.subscribe),
                        errorMessage = e.message ?: stringsRepository.getString(R.string.unknown),
                        lockLinkInput = false,
                    )
                }
            }
        }.also {
            searchJob = it
        }
    }

    private fun inputLink(content: String) {
        _viewState.update {
            it.copy(
                linkContent = content
            )
        }
    }

    private fun inputNewGroup(content: String) {
        _viewState.update {
            it.copy(
                newGroupContent = content
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
data class SubscribeViewState(
    val visible: Boolean = false,
    val title: String = "",
    val errorMessage: String = "",
    val linkContent: String = "",
    val lockLinkInput: Boolean = false,
    val feed: Feed? = null,
    val articles: List<Article> = emptyList(),
    val allowNotificationPreset: Boolean = false,
    val parseFullContentPreset: Boolean = false,
    val selectedGroupId: String = "",
    val newGroupContent: String = "",
    val newGroupSelected: Boolean = false,
    val groups: Flow<List<Group>> = emptyFlow(),
    val pagerState: PagerState = PagerState(),
)

sealed class SubscribeViewAction {
    object Init : SubscribeViewAction()
    object Reset : SubscribeViewAction()

    object Show : SubscribeViewAction()
    object Hide : SubscribeViewAction()

    data class InputLink(
        val content: String
    ) : SubscribeViewAction()

    data class Search(
        val scope: CoroutineScope,
    ) : SubscribeViewAction()

    object ChangeAllowNotificationPreset : SubscribeViewAction()
    object ChangeParseFullContentPreset : SubscribeViewAction()

    data class SelectedGroup(
        val groupId: String
    ) : SubscribeViewAction()

    data class InputNewGroup(
        val content: String
    ) : SubscribeViewAction()

    data class SelectedNewGroup(
        val selected: Boolean
    ) : SubscribeViewAction()

    object Subscribe : SubscribeViewAction()
}
