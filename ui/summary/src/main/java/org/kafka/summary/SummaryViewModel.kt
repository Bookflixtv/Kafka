package org.kafka.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafka.data.entities.Summary
import com.kafka.data.model.SearchFilter.Creator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import org.kafka.analytics.logger.Analytics
import org.kafka.base.extensions.stateInDefault
import org.kafka.domain.observers.ObserveItemDetail
import org.kafka.domain.observers.summary.ObserveSummary
import org.kafka.navigation.Navigator
import org.kafka.navigation.RootScreen
import org.kafka.navigation.Screen.Search
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    observeSummary: ObserveSummary,
    observeItemDetail: ObserveItemDetail,
    savedStateHandle: SavedStateHandle,
    private val navigator: Navigator,
    private val analytics: Analytics
) : ViewModel() {
    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    val state = combine(
        observeSummary.flow.onStart { emit(null) },
        observeItemDetail.flow
    ) { summary, item ->
        SummaryState(
            summary = summary,
            title = item?.title.orEmpty(),
            creator = item?.creator
        )
    }.stateInDefault(viewModelScope, SummaryState())

    init {
        observeSummary(itemId)
        observeItemDetail(ObserveItemDetail.Param(itemId))
    }

    fun goToCreator(keyword: String?) {
        analytics.log { this.openCreator("summary") }
        navigator.navigate(Search.createRoute(RootScreen.Search, keyword, Creator.name))
    }
}

data class SummaryState(
    val summary: Summary? = null,
    val title: String = "",
    val creator: String? = null,
)
