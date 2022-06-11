package me.ash.reader.ui.page.home.feeds.subscribe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.data.model.article.Article
import me.ash.reader.data.model.feed.Feed
import me.ash.reader.data.model.group.Group
import me.ash.reader.data.repository.OpmlRepository
import me.ash.reader.data.repository.RssHelper
import me.ash.reader.data.repository.RssRepository
import me.ash.reader.data.repository.StringsRepository
import me.ash.reader.ui.ext.formatUrl
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class SubscribeViewModel @Inject constructor(
    private val opmlRepository: OpmlRepository,
    private val rssRepository: RssRepository,
    private val rssHelper: RssHelper,
    private val stringsRepository: StringsRepository,
) : ViewModel() {

    private val _subscribeUiState = MutableStateFlow(SubscribeUiState())
    val subscribeUiState: StateFlow<SubscribeUiState> = _subscribeUiState.asStateFlow()
    private var searchJob: Job? = null

    fun init() {
        _subscribeUiState.update {
            it.copy(
                title = stringsRepository.getString(R.string.subscribe),
                groups = rssRepository.get().pullGroups(),
            )
        }
    }

    fun reset() {
        searchJob?.cancel()
        searchJob = null
        _subscribeUiState.update {
            SubscribeUiState().copy(title = stringsRepository.getString(R.string.subscribe))
        }
    }

    fun importFromInputStream(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                opmlRepository.saveToDatabase(inputStream)
                rssRepository.get().doSync()
            } catch (e: Exception) {
                Log.e("FeedsViewModel", "importFromInputStream: ", e)
            }
        }
    }

    fun subscribe() {
        val feed = _subscribeUiState.value.feed ?: return
        val articles = _subscribeUiState.value.articles
        viewModelScope.launch {
            rssRepository.get().subscribe(
                feed.copy(
                    groupId = _subscribeUiState.value.selectedGroupId,
                    isNotification = _subscribeUiState.value.allowNotificationPreset,
                    isFullContent = _subscribeUiState.value.parseFullContentPreset,
                ), articles
            )
            hideDrawer()
        }
    }

    fun selectedGroup(groupId: String) {
        _subscribeUiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun addNewGroup() {
        if (_subscribeUiState.value.newGroupContent.isNotBlank()) {
            viewModelScope.launch {
                selectedGroup(rssRepository.get().addGroup(_subscribeUiState.value.newGroupContent))
                hideNewGroupDialog()
                _subscribeUiState.update { it.copy(newGroupContent = "") }
            }
        }
    }

    fun changeParseFullContentPreset() {
        _subscribeUiState.update {
            it.copy(parseFullContentPreset = !_subscribeUiState.value.parseFullContentPreset)
        }
    }

    fun changeAllowNotificationPreset() {
        _subscribeUiState.update {
            it.copy(allowNotificationPreset = !_subscribeUiState.value.allowNotificationPreset)
        }
    }

    fun search() {
        searchJob?.cancel()
        viewModelScope.launch {
            try {
                _subscribeUiState.update {
                    it.copy(
                        errorMessage = "",
                    )
                }
                _subscribeUiState.value.linkContent.formatUrl().let { str ->
                    if (str != _subscribeUiState.value.linkContent) {
                        _subscribeUiState.update {
                            it.copy(
                                linkContent = str
                            )
                        }
                    }
                }
                _subscribeUiState.update {
                    it.copy(
                        title = stringsRepository.getString(R.string.searching),
                        lockLinkInput = true,
                    )
                }
                if (rssRepository.get().isFeedExist(_subscribeUiState.value.linkContent)) {
                    _subscribeUiState.update {
                        it.copy(
                            title = stringsRepository.getString(R.string.subscribe),
                            errorMessage = stringsRepository.getString(R.string.already_subscribed),
                            lockLinkInput = false,
                        )
                    }
                    return@launch
                }
                val feedWithArticle = rssHelper.searchFeed(_subscribeUiState.value.linkContent)
                _subscribeUiState.update {
                    it.copy(
                        feed = feedWithArticle.feed,
                        articles = feedWithArticle.articles,
                    )
                }
                switchPage(false)
            } catch (e: Exception) {
                e.printStackTrace()
                _subscribeUiState.update {
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

    fun inputLink(content: String) {
        _subscribeUiState.update {
            it.copy(
                linkContent = content,
                errorMessage = "",
            )
        }
    }

    fun inputNewGroup(content: String) {
        _subscribeUiState.update { it.copy(newGroupContent = content) }
    }

    fun showDrawer() {
        _subscribeUiState.update { it.copy(visible = true) }
    }

    fun hideDrawer() {
        _subscribeUiState.update { it.copy(visible = false) }
    }

    fun showNewGroupDialog() {
        _subscribeUiState.update { it.copy(newGroupDialogVisible = true) }
    }

    fun hideNewGroupDialog() {
        _subscribeUiState.update { it.copy(newGroupDialogVisible = false) }
    }

    fun switchPage(isSearchPage: Boolean) {
        _subscribeUiState.update { it.copy(isSearchPage = isSearchPage) }
    }
}

data class SubscribeUiState(
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
    val newGroupDialogVisible: Boolean = false,
    val newGroupContent: String = "",
    val groups: Flow<List<Group>> = emptyFlow(),
    val isSearchPage: Boolean = true,
)
