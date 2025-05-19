package me.ash.reader.domain.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.model.group.Group
import me.ash.reader.infrastructure.preference.SettingsProvider
import javax.inject.Inject

class FilterStateUseCase @Inject constructor(settingsProvider: SettingsProvider) {

    private val _filterUiState =
        MutableStateFlow(FilterState(filter = settingsProvider.settings.initialFilter.toFilter()))
    val filterStateFlow = _filterUiState.asStateFlow()
    private val filterState get() = filterStateFlow.value

    fun updateFilterState(
        feed: Feed? = filterState.feed,
        group: Group? = filterState.group,
        filter: Filter = filterState.filter,
        searchContent: String? = filterState.searchContent,
    ) {
        _filterUiState.update {
            it.copy(
                feed = feed,
                group = group,
                searchContent = searchContent,
                filter = filter
            )
        }
    }

    fun updateFilterState(filterState: FilterState) {
        _filterUiState.update { filterState }
    }
}

data class FilterState(
    val group: Group? = null,
    val feed: Feed? = null,
    val filter: Filter = Filter.All,
    val searchContent: String? = null,
)