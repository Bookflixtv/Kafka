package com.kafka.search

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafka.data.model.MediaType
import com.kafka.data.model.SearchFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kafka.analytics.logger.Analytics
import org.kafka.base.extensions.stateInDefault
import org.kafka.common.ObservableLoadingCounter
import org.kafka.common.collectStatus
import org.kafka.common.snackbar.SnackbarManager
import org.kafka.domain.interactors.AddRecentSearch
import org.kafka.domain.interactors.RemoveRecentSearch
import org.kafka.domain.interactors.SearchQueryItems
import org.kafka.domain.observers.ObserveRecentSearch
import org.kafka.domain.observers.ObserveSearchItems
import org.kafka.navigation.Navigator
import org.kafka.navigation.Screen
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    observeRecentSearch: ObserveRecentSearch,
    private val addRecentSearch: AddRecentSearch,
    private val removeRecentSearch: RemoveRecentSearch,
    private val searchQueryItems: SearchQueryItems,
    private val observeSearchItems: ObserveSearchItems,
    private val navigator: Navigator,
    private val analytics: Analytics,
    private val snackbarManager: SnackbarManager,
    private val loadingState: ObservableLoadingCounter,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val keywordInitialValue = savedStateHandle.get<String>(extraKeyword).orEmpty()
    internal val selectedMediaTypes = mutableStateListOf<MediaType>()
        .apply { addAll(MediaType.entries) }
    private val selectedFilters
        get() = savedStateHandle.get<String>(extraFilters) ?: SearchFilter.allString()

    val state: StateFlow<SearchViewState> = combine(
        savedStateHandle.getStateFlow(extraKeyword, ""),
        savedStateHandle.getStateFlow(extraFilters, SearchFilter.Name.name)
            .map { SearchFilter.from(it) },
        observeSearchItems.flow,
        observeRecentSearch.flow,
        loadingState.observable
    ) { keyword, filters, items, recentSearches, isLoading ->
        SearchViewState(
            keyword = keyword,
            selectedFilters = filters,
            items = items,
            recentSearches = recentSearches,
            isLoading = isLoading
        )
    }.stateInDefault(scope = viewModelScope, initialValue = SearchViewState())

    init {
        search(
            keyword = keywordInitialValue,
            filters = SearchFilter.from(selectedFilters),
            mediaTypes = selectedMediaTypes
        )

        observeRecentSearch(Unit)
    }

    fun search(
        keyword: String = state.value.keyword,
        filters: List<SearchFilter> = SearchFilter.from(selectedFilters),
        mediaTypes: List<MediaType> = selectedMediaTypes
    ) {
        observeSearchItems(ObserveSearchItems.Params(keyword, filters, mediaTypes.toList()))

        viewModelScope.launch {
            searchQueryItems(
                SearchQueryItems.Params(
                    keyword = keyword,
                    searchFilter = filters,
                    mediaTypes = mediaTypes.toList()
                )
            ).collectStatus(loadingState, snackbarManager)
        }

        if (keyword.isNotEmpty()) {
            analytics.log {
                searchQuery(
                    keyword = keyword,
                    filters = filters.map { it.name },
                    mediaTypes = mediaTypes.map { it.value }
                )
            }
            addRecentSearch(keyword)
        }
    }

    fun setQuery(query: String) {
        savedStateHandle[extraKeyword] = query
    }

    private fun addRecentSearch(keyword: String) {
        viewModelScope.launch {
            val selectedFilters = state.value.selectedFilters
            val params = AddRecentSearch.Params(
                searchTerm = keyword,
                filters = selectedFilters,
                mediaTypes = selectedMediaTypes.toList()
            )
            addRecentSearch.invoke(params).collect()
        }
    }

    fun toggleFilter(filter: SearchFilter) {
        val selectedFilters = state.value.selectedFilters.toMutableList()
        if (selectedFilters.contains(filter)) {
            selectedFilters.remove(filter)
        } else {
            selectedFilters.add(filter)
        }

        savedStateHandle[extraFilters] = SearchFilter.toString(selectedFilters)

        if (state.value.keyword.isNotEmpty()) {
            search(filters = selectedFilters)
        }
    }

    fun toggleMediaType(mediaType: MediaType) {
        if (selectedMediaTypes.contains(mediaType)) {
            if (selectedMediaTypes.size > 1) {
                selectedMediaTypes.remove(mediaType)
            }
        } else {
            selectedMediaTypes.add(mediaType)
        }

        if (state.value.keyword.isNotEmpty()) {
            search(mediaTypes = selectedMediaTypes)
        }
    }

    fun removeRecentSearch(keyword: String) {
        viewModelScope.launch {
            analytics.log { removeRecentSearch(keyword) }
            removeRecentSearch.invoke(keyword).collect()
        }
    }

    fun openItemDetail(itemId: String) {
        analytics.log { this.openItemDetail(itemId, "search") }
        navigator.navigate(Screen.ItemDetail.createRoute(navigator.currentRoot.value, itemId))
    }
}

private const val extraKeyword = "keyword"
private const val extraFilters = "filters"
